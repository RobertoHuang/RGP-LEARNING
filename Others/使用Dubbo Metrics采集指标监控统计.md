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
      
  // 自定义count计算
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

## 整合OpenTSDB

将指标值转成`Point`对象【指标名、`tag`、`当前时间戳`、`指标对应的值`】 - > 存入`OpenTSDB` -> 界面展示

