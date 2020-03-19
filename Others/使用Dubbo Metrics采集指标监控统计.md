# Dubbo Metrics

> `Dubbo metrics`全面支持了从操作系统，`JVM`，中间件，再到应用层面的各级指标
>
> 并且对统一了各种命名指标，可以做到开箱即用，并支持通过配置随时开启和关闭某类指标的收集

## 内置指标

> 以采集部分`JVM`指标为例

- 初始化指标注册器

  ```java
  MetricRegistry metricRegistry = MetricManager.getIMetricManager().getMetricRegistryByGroup("GROUP_NAME");
  ```

- 注册内置指标采集器

  ```java
  // JVM内存指标
  metricRegistry.register(MetricName.build("jvm.mem").level(level), new MemoryUsageGaugeSet());
  // JVM GC相关指标
  metricRegistry.register(MetricName.build("jvm.gc ").level(level), new GarbageCollectorMetricSet());
  // JVM堆外内存指标
  metricRegistry.register(MetricName.build("jvm.buffer_pool").level(level), new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
  // JVM线程相关指标
  metricRegistry.register(MetricName.build("jvm.thread").level(level), new ThreadStatesGaugeSet());
  ```

- 初始化`Reporter`输出指标信息

  >  `Reporter`可以自定义，常见的是继承`ScheduledReporter`实现`report`逻辑
  >
  > 通过`Registry`获取指标的时候，可以通过`MetricFilter`对采集的指标进行过滤
  
  ```java
  public class JvmMonitorReporter extends ScheduledReporter {
      public static final String JVM_MONITOR_REPORTER = "jvm-monitor-reporter";
  
      public JvmMonitorReporter(MetricRegistry registry, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
          super(registry, JVM_MONITOR_REPORTER, filter, rateUnit, durationUnit);
      }
  
      @Override
      public void report(SortedMap<MetricName, Gauge> gauges, SortedMap<MetricName, Counter> counters, SortedMap<MetricName, Histogram> histograms, SortedMap<MetricName, Meter> meters, SortedMap<MetricName, Timer> timers) {
          for (Map.Entry<MetricName, Gauge> entry : gauges) {
              MetricName metricName = entry.getKey();
              reportToOpentsdb(metricName.getKey(), metricName.getTags(), System.currentTimeMillis() / 1000, entry.getValue().getValue());
          }
      }
  }
  ```

## 自定义指标

- 初始化指标注册器

  ```java
  MetricRegistry metricRegistry = MetricManager.getIMetricManager().getMetricRegistryByGroup("GROUP_NAME");
  ```

- 在需要采集指标位置埋点

  ```java
  metricRegistry.counter(MetricName).inc();
  metricRegistry.fastCompass(MetricName).record(DURATION, SUBCATEGORY);
  metricRegistry.histogram(MetricName, ReservoirType.SLIDING_TIME_WINDOW).update(METRIC_VALUE)
      
  // 自定义count计算 高位减1是因为dubbo metric record的时候默认记录次数是加1，所以这里要减1
  long compassFlow = (count << LogAppenderReporter.FASTCOMPASS_COUNT_OFFSET) - (1L << LogAppenderReporter.FASTCOMPASS_COUNT_OFFSET) + size;
  ```

- 初始化`Reporter`输出指标信息

  ```java
  public class SimpleScheduledReporter {
      private static final int GENERAL_MAX_SAMPLE_NUM = 2;
      private static final int CRITICAL_MAX_SAMPLE_NUM = 10;
  
      /**
       * FastCompass实现中的数字分隔偏移量，后半段数字所占位数
       */
      public static final int FASTCOMPASS_COUNT_OFFSET = 38;
  
      private static final long FASTCOMPASS_MASK_OFFSET = (1L << FASTCOMPASS_COUNT_OFFSET) - 1;
  
      private final MetricFilter filter;
      private final MetricRegistry registry;
  
      private final Clock clock = Clock.defaultClock();
  
      private final MetricsCollectPeriodConfig metricsReportPeriodConfig = new MetricsCollectPeriodConfig();
  
      private final HashMap<MetricLevel, Long> lastTimestamps = new HashMap<>(MetricLevel.values().length);
  
      private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  
      private static final Logger LOGGER = LoggerFactory.getLogger(SimpleScheduledReporter.class);
  
      public SimpleScheduledReporter(final MetricFilter filter, final MetricRegistry registry) {
          this.filter = filter;
          this.registry = registry;
      }
  
      public void start(final long period, final TimeUnit unit) {
          executor.scheduleWithFixedDelay(new Runnable() {
              @Override
              public void run() {
                  try {
                      report();
                  } catch (final Throwable e) {
                      LOGGER.error("Throwable RuntimeException thrown from {}#report. Exception was suppressed.", SimpleScheduledReporter.this.getClass().getSimpleName(), e);
                  }
              }
          }, period, period, unit);
      }
  
      public void report() {
          synchronized (this) {
              report(registry.getGauges(filter), registry.getCounters(filter), registry.getHistograms(filter), registry.getMeters(filter), registry.getTimers(filter), registry.getCompasses(), registry.getFastCompasses());
          }
      }
  
      public void report(final SortedMap<MetricName, Gauge> gauges, final SortedMap<MetricName, Counter> counters, final SortedMap<MetricName, Histogram> histograms, final SortedMap<MetricName, Meter> meters, final SortedMap<MetricName, Timer> timers, final SortedMap<MetricName, Compass> compass, final SortedMap<MetricName, FastCompass> fastCompass) {
          final long now = this.clock.getTime();
          // reportOtherMetrics();
          reportCounter(counters, now);
          reportHistograms(histograms, now);
          reportFastCompass(fastCompass, now);
          for (final MetricLevel level : MetricLevel.values()) {
              final long interval = TimeUnit.SECONDS.toMillis(this.metricsReportPeriodConfig.period(level));
              final long endTime = (now / interval - 1) * interval;
              this.lastTimestamps.put(level, endTime);
          }
      }
  
      private void reportCounter(final SortedMap<MetricName, Counter> counters, final long now) {
          counters.entrySet().stream().forEach(entry -> {
              final Counter counter = entry.getValue();
              final MetricName metricName = entry.getKey();
              if (!(counter instanceof BucketCounter)) {
                  // doBaseCountReport();
              } else {
                  doReportBucketCounter(metricName, ((BucketCounter) counter), now);
              }
          });
      }
  
      private void reportFastCompass(final SortedMap<MetricName, FastCompass> fastCompass, final long now) {
          fastCompass.entrySet().forEach(entry -> doReportFastCompass(entry.getKey(), entry.getValue(), now));
      }
  
      private void reportHistograms(final SortedMap<MetricName, Histogram> histograms, final long now) {
          for (final Map.Entry<MetricName, Histogram> entry : histograms.entrySet()) {
              final MetricName metricName = entry.getKey();
              final Long lastReport = MapUtils.getLong(this.lastTimestamps, metricName.getMetricLevel(), 0L);
              final long interval = TimeUnit.SECONDS.toMillis(this.metricsReportPeriodConfig.period(metricName.getMetricLevel()));
              if (now - lastReport > interval) {
                  final Histogram histogram = entry.getValue();
                  doReportHistogram(metricName, histogram, now);
              }
          }
      }
  
      private void doReportBucketCounter(final MetricName metricName, final BucketCounter counter, final long now) {
          final Map<Long, Long> bucketCounts = counter.getBucketCounts();
          // 计算Report时间间隔
          final MetricLevel level = metricName.getMetricLevel();
          final long interval = TimeUnit.SECONDS.toMillis(this.metricsReportPeriodConfig.period(level));
          // 计算开始时间和结束时间
          final long endTime = (now / interval - 1) * interval;
          final long startTime = sampleReduce(MapUtils.getLong(this.lastTimestamps, level, 0L) + interval, endTime, interval, level);
          // 开始循环report指标数据
          for (long time = startTime; time <= endTime; time = time + interval) {
              final Long count = bucketCounts.get(time);
              if (count == null) {
                  continue;
              } else {
                  // reportToOpentsdb(metricName.getKey(), metricName.getTags(), time, count);
              }
          }
      }
  
      private void doReportFastCompass(final MetricName metricName, final FastCompass fastCompass, final long now) {
          // 计算Report时间间隔
          final MetricLevel level = metricName.getMetricLevel();
          final long interval = TimeUnit.SECONDS.toMillis(this.metricsReportPeriodConfig.period(level));
          // 计算开始时间和结束时间
          final long endTime = (now / interval - 1) * interval;
          final long startTime = sampleReduce(MapUtils.getLong(this.lastTimestamps, level, 0L) + interval, endTime, interval, level);
          // 开始循环report指标数据
          final Map<String, Map<Long, Long>> statsByCategory = fastCompass.getCountAndRtPerCategory(startTime);
          for (long time = startTime; time <= endTime; time = time + interval) {
              for (final Map.Entry<String, Map<Long, Long>> entry : statsByCategory.entrySet()) {
                  final String category = entry.getKey();
                  final Map<Long, Long> statsByTime = entry.getValue();
                  final Long compass = statsByTime.get(time);
                  if (compass != null) {
                      /**
                       *  ------------------------------------------
                       * |   1 bit    |     25 bit   |     38 bit |
                       * | signed bit |  total count |   total rt |
                       * ------------------------------------------
                       **/
                      final long count = getFastCompassCount(compass);
                      if (count > 0) {
                          final long sum = getFastCompassSum(compass);
                          // reportToOpenTSDB(metricName.getKey(), metricName.getTags(), time, sum / (double) count);
                      }
                  }
              }
          }
      }
  
      private void doReportHistogram(final MetricName metricName, final Histogram histogram, final long now) {
          final Snapshot snapshot = histogram.getSnapshot();
          // this.reportToOpenTSDB(metricName.getKey() + ".min", metricName.getTags(), now, snapshot.getMin());
          // this.reportToOpenTSDB(metricName.getKey() + ".max", metricName.getTags(), now, snapshot.getMax());
          // this.reportToOpenTSDB(metricName.getKey() + ".avg", metricName.getTags(), now, snapshot.getMean());
      }
  
      public static long getFastCompassCount(final long num) {
          // 获取总调用次数
          return num >> FASTCOMPASS_COUNT_OFFSET;
      }
  
      public static long getFastCompassSum(final long num) {
          // 获取总响应时间
          return num & FASTCOMPASS_MASK_OFFSET;
      }
  
      /**
       * 〈避免startTime与endTime差距太长导致的创建大量对象〉
       * <p>
       * - 主要作用于首次report的时候，startTime为0 / report阻塞导致时间差距拉大
       */
      private static long sampleReduce(final long startTime, final long endTime, final long interval, final MetricLevel level) {
          // 计算可能需要report次数
          final int sampleNum = (int) ((endTime - startTime) / interval);
          // 计算最大可允许的report次数
          final int maxSampleNum = (level == MetricLevel.CRITICAL) ? CRITICAL_MAX_SAMPLE_NUM : GENERAL_MAX_SAMPLE_NUM;
          // 返回可靠的report startTime
          return (sampleNum > maxSampleNum) ? endTime - interval * maxSampleNum : startTime;
      }
  }
  ```

## 防止Report被启动多次

```java
public class ClientStatsManager {
    private final static AtomicLong REFS = new AtomicLong(0);
    private final static AtomicReference<SimpleScheduledReporter> METRIC_REPORT = new AtomicReference<>();

    public static void refStats() {
        final long oldCount = REFS.getAndIncrement();
        if (oldCount == 0) {
            final SimpleScheduledReporter metricReporter = new SimpleScheduledReporter(XXXRegister register, MetricFilter.ALL);
            // 保证一分钟内至少被检查两次
            // 确保前一分钟的统计数据在当前分钟内一定内被处理
            metricReporter.start(29, TimeUnit.SECONDS);
            METRIC_REPORT.set(metricReporter);
        }
    }

    public static void unrefStats() {
        final MetricReporter reporter = METRIC_REPORT.get();
        final long oldCount = REFS.decrementAndGet();
        if (oldCount == 0) {
            reporter.stop();
            METRIC_REPORT.compareAndSet(reporter, null);
        }
    }
}
```

## 整合OpenTSDB

将指标值转成`Point`对象【指标名、`tag`、`当前时间戳`、`指标对应的值`】 - > 存入`OpenTSDB` -> 界面展示

- `OpenTsdb`查询模块相关代码

    - 指标数据格式化工具类

        ```java
        /**
         * 〈指标数据格式化.〉
         *
         * @author HuangTaiHong
         * @since 2020-03-16
         */
        public enum OpenTsdbDataFormat {
            /**
             * 指标数据格式化枚举
             */
            None {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length == 0) {
                        throw new OpenTsdbDataFormatException("numbers count error");
                    }
                    return numbers[0];
                }
            },
        
            GigaNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length == 0) {
                        throw new OpenTsdbDataFormatException("numbers count error");
                    }
                    if (numbers[0] == null) {
                        return null;
                    }
                    return numbers[0].doubleValue() / (1024 * 1024 * 1024);
                }
            },
        
            MegaNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length == 0) {
                        throw new OpenTsdbDataFormatException("numbers count error");
                    }
                    if (numbers[0] == null) {
                        return null;
                    }
                    return numbers[0].doubleValue() / (1024 * 1024);
                }
            },
        
            KiloNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length == 0) {
                        throw new OpenTsdbDataFormatException("numbers count error");
                    }
                    if (numbers[0] == null) {
                        return null;
                    }
                    return numbers[0].doubleValue() / 1024;
                }
            },
        
            DiffNoneNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length < 2) {
                        return null;
                    }
                    if (numbers[0] == null || numbers[1] == null) {
                        return null;
                    }
                    return (numbers[1].doubleValue() - numbers[0].doubleValue());
                }
            },
        
            DiffGigaNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length < 2) {
                        return null;
                    }
                    if (numbers[0] == null || numbers[1] == null) {
                        return null;
                    }
                    return (numbers[1].doubleValue() - numbers[0].doubleValue()) / (1024 * 1024 * 1024);
                }
            },
        
            DiffMegaNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length < 2) {
                        return null;
                    }
                    if (numbers[0] == null || numbers[1] == null) {
                        return null;
                    }
                    return (numbers[1].doubleValue() - numbers[0].doubleValue()) / (1024 * 1024);
                }
            },
        
            DiffKiloNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length < 2) {
                        return null;
                    }
                    if (numbers[0] == null || numbers[1] == null) {
                        return null;
                    }
                    return (numbers[1].doubleValue() - numbers[0].doubleValue()) / 1024;
                }
            },
        
            DiffThousandNar {
                @Override
                Number format(Number... numbers) {
                    if (numbers.length < 2) {
                        return null;
                    }
                    if (numbers[0] == null || numbers[1] == null) {
                        return null;
                    }
                    return (numbers[1].doubleValue() - numbers[0].doubleValue()) / 1000;
                }
            };
        
            abstract Number format(Number... numbers);
        
            public List<Number> formatDps(final Collection<Object> dataPointLists) {
                final List<Number> result = Lists.newArrayList();
                final Object[] values = dataPointLists.toArray();
                for (int i = 0; i < values.length; i++) {
                    result.add(this.format((Number) values[Math.max(0,i - 1)], (Number) values[i]));
                }
                return result;
            }
        }
        ```

        ```java
        public class OpenTsdbDataFormatException extends RuntimeException{
            public OpenTsdbDataFormatException(String message) {
                super(message);
            }
        
            public OpenTsdbDataFormatException(String message, Throwable cause) {
                super(message, cause);
            }
        }
        ```

    - 聚合周期降采样表达式获取工具类

        ```java
        public class AggregationIntervalUtils {
            public static final int M1 = 60;
            public static final int M5 = M1 * 5;
            public static final int M15 = M1 * 15;
            public static final int M30 = M1 * 30;
        
            public static final int H1 = M1 * 60;
            public static final int H6 = H1 * 6;
            public static final int H12 = H1 * 12;
        
            public static final int D1 = H1 * 24;
            public static final int D3 = D1 * 3;
            public static final int D7 = D1 * 7;
            public static final int D14 = D1 * 14;
            public static final int D30 = D1 * 30;
        
            /**
             * 获取时间节点.
             *
             * @param begin the begin
             * @param end   the end
             * @return the time nodes
             * @author HuangTaiHong
             * @since 2020.03.16 18:50:34
             */
            public static List<Long> getTimeNodes(long begin, long end) {
                int interval = D1;
                final long time = end - begin;
                if (time < H6) {
                    interval = M1;
                } else if (time < D1) {
                    interval = M5;
                } else if (time < D7) {
                    interval = M15;
                } else if (time < D14) {
                    interval = H1;
                }
                end = end / interval * interval;
                begin = begin / interval * interval;
                final List<Long> timeNodes = new ArrayList<>();
                for (long i = begin; i <= end; i += interval) {
                    timeNodes.add(i);
                }
                return timeNodes;
            }
        
            /**
             * 获取降采样表达式.
             *
             * @param begin 查询开始时间
             * @param end   查询结束时间
             * @return interval
             * @author HuangTaiHong
             * @since 2020.03.13 16:22:37
             */
            public static String getDownasmple(final long begin, final long end, final Aggregator aggregator) {
                final String downSample;
                final long time = end - begin;
                if (time < H6) {
                    // 小于6小时，按分钟进行降采样
                    downSample = Granularity.M1.getName();
                } else if (time < D1) {
                    // 小于1天，按5分钟进行降采样
                    downSample = Granularity.M5.getName();
                } else if (time < D7) {
                    // 小于7天，按15分钟进行降采样
                    downSample = Granularity.M15.getName();
                } else if (time < D14) {
                    // 小于14天，按1小时进行降采样
                    downSample = Granularity.H1.getName();
                } else {
                    downSample = Granularity.H24.getName();
                }
                return downSample + "-" + aggregator.getName() + "-null";
            }
        }
        ```

    - 请求响应实体类封装

        ```java
        @Data
        @ApiModel(value = "客户端指标查询请求")
        public class ClientMetricOpenTsdbRequest {
            @NotNull
            @ApiModelProperty("客户端指标名称")
            private String[] metrics;
        
            @NotNull
            @ApiModelProperty("开始时间")
            private Long start;
        
            @NotNull
            @ApiModelProperty("结束时间")
            private Long end;
        
            @ApiModelProperty("查询的TAGS")
            private Map<String, String> tags;
        
            @ApiModelProperty("搜索详情时表示展开哪个字段")
            private String expand;
        
            @ApiModelProperty("可能的查询TAG条件，多个使用逗号隔开")
            private String queryConditions;
        
            @NotNull
            @ApiModelProperty("聚合方式")
            private String aggregator;
        
            @NotNull
            @ApiModelProperty("返回结果数据格式化")
            private OpenTsdbDataFormat dataFormat;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @ApiModel("客户端指标OpenTsdb响应")
        public class ClientMetricOpenTsdbResponse {
            /**
             * 时间节点信息
             */
            @ApiModelProperty("时间节点")
            private List<Long> timeNodes;
        
            /**
             * 指标映射关系
             */
            @ApiModelProperty("客户端指标")
            private Map<String, List<ExpandTagMetricMapping>> metrics;
        
            /**
             * 下拉框可选值
             */
            @ApiModelProperty("下拉框可选值")
            private Map<String, Set<String>> conditionInfoMap;
        }
        
        public class ExpandTagMetricMapping {
            /**
             * TAG值
             */
            private String tagValue;
        
            /**
             * 指标值
             */
            private List<Number> metric;
        }
        
        @GetMapping(value = "/opentsdb/metric")
        @ApiOperation(value = "客户端指标采集响应")
        public ClientMetricOpenTsdbResponse clientMetricUseOpenTsdb(final ClientMetricOpenTsdbRequest clientMetricOpenTsdbRequest) {
            return this.clientMetricService.getMetricUseOpenTsdb(clientMetricOpenTsdbRequest);
        }
        ```

    - 查询指标`Service`实现

        ```java
        public ClientMetricOpenTsdbResponse getMetricUseOpenTsdb(final ClientMetricOpenTsdbRequest clientMetricOpenTsdbRequest) {
            // 获取阿里TSDB客户端连接
            final TSDB tsdb = this.getOpenTSDB();
            final String[] metrics = clientMetricOpenTsdbRequest.getMetrics();
            // 获取下拉框可选值
            final Map<String, Set<String>> conditionInfos = Maps.newConcurrentMap();
            // 获取时间节点信息
            final List<Long> timeNodes = AggregationIntervalUtils.getTimeNodes(clientMetricOpenTsdbRequest.getStart(), clientMetricOpenTsdbRequest.getEnd());
            // 聚合查询
            final boolean expand = StringUtils.isNotEmpty(clientMetricOpenTsdbRequest.getExpand());
            final Map<String, List<ExpandTagMetricMapping>> expandTagMetricMappingMap = Maps.newConcurrentMap();
            Arrays.stream(metrics).parallel().forEach(metric -> {
                try {
                    final Query query = this.buildSearchCondition(clientMetricOpenTsdbRequest, metric, null, null);
                    final List<QueryResult> results = tsdb.query(query);
                    expandTagMetricMappingMap.put(metric, results.stream().map(result -> new ExpandTagMetricMapping(expand ? result.getTags().get(clientMetricOpenTsdbRequest.getExpand()) : null, clientMetricOpenTsdbRequest.getDataFormat().formatDps(result.getDps().values()))).collect(Collectors.toList()));
        
                    final String queryConditions = clientMetricOpenTsdbRequest.getQueryConditions();
                    if (StringUtils.isNotBlank(queryConditions)) {
                        final String[] conditions = queryConditions.split(",");
                        for (final String condition : conditions) {
                            //【阿里TSDB支持dumpMeta】
                            Set<String> tagValues = conditionInfos.get(condition);
                            if (tagValues == null) {
                                synchronized (this) {
                                    if (tagValues == null) {
                                        tagValues = new HashSet<>();
                                        conditionInfos.put(condition, tagValues);
                                    }
                                }
                            }
                            tagValues.addAll(this.getConditionValues(clientMetricOpenTsdbRequest, metric, condition));
                        }
                    }
                } catch (final Exception e) {
                    log.error("failed to query opentsdb metric. cause by: {}", e.getMessage());
                }
            });
            return new ClientMetricOpenTsdbResponse(timeNodes, expandTagMetricMappingMap, conditionInfos);
        }
        
        /**
         * 获取下拉框可选值.
         *
         * @param clientMetricOpenTsdbRequest the client metric open tsdb request
         * @param metric                      the metric
         * @param condition                   the condition
         * @return the condition values
         * @author HuangTaiHong
         * @since 2020.03.16 21:29:04
         */
        private List<String> getConditionValues(final ClientMetricOpenTsdbRequest clientMetricOpenTsdbRequest, final String metric, final String condition) {
            List<String> tagValues = Lists.newArrayList();
            final Query query = this.buildSearchCondition(clientMetricOpenTsdbRequest, metric, condition, Aggregator.NONE);
            try {
                final List<QueryResult> results = this.getOpenTSDB().query(query);
                tagValues = results.stream().map(result -> result.getTags().get(condition)).collect(Collectors.toList());
            } catch (final Exception e) {
                log.error("failed to query opentsdb metric. cause by: {}", e.getMessage());
            }
            return tagValues;
        }
        
        /**
         * 构建查询表达式.
         *
         * @param clientMetricOpenTsdbRequest the client metric open tsdb request
         * @param metric                      the metric
         * @param specialExpand               用来替换clientMetricRequest中的展开条件
         * @param specialAggregator           用来替换clientMetricRequest中的聚合方式
         * @return the query
         * @author HuangTaiHong
         * @since 2020.03.16 21:53:46
         */
        private Query buildSearchCondition(final ClientMetricOpenTsdbRequest clientMetricOpenTsdbRequest, final String metric, final String specialExpand, final Aggregator specialAggregator) {
            final String expand = specialExpand == null ? clientMetricOpenTsdbRequest.getExpand() : specialExpand;
            final Aggregator aggregator = specialAggregator == null ? Aggregator.getEnum(clientMetricOpenTsdbRequest.getAggregator()) : specialAggregator;
            // 查询时间范围
            final Query.Builder builder = Query.timeRange(clientMetricOpenTsdbRequest.getStart(), clientMetricOpenTsdbRequest.getEnd());
            // 聚合方式
            final SubQuery.Builder subQueryBuilder = SubQuery.metric(metric).aggregator(aggregator);
            // 指定Tag查询
            final Map<String, String> tags = clientMetricOpenTsdbRequest.getTags();
            if (MapUtils.isNotEmpty(tags)) {
                for (final Map.Entry<String, String> entry : tags.entrySet()) {
                    subQueryBuilder.tag(entry.getKey(), entry.getValue());
                }
            }
            // 指标展开维度
            if (StringUtils.isNotEmpty(expand)) {
                subQueryBuilder.tag(expand, "*");
            }
            // 按时间进行降采样
            if (aggregator != Aggregator.NONE) {
                subQueryBuilder.downsample(AggregationIntervalUtils.getDownasmple(clientMetricOpenTsdbRequest.getStart(), clientMetricOpenTsdbRequest.getEnd(), aggregator));
            }
            return builder.sub(subQueryBuilder.build()).build();
        }
        ```

        