# Collections

> `Guava`集合部分使用

## FluentIterable

> `FluentIterable`主要用于过滤、转换集合中的数据

```java
public class FluentIterableExampleTest {
    private List<String> dataSource = Lists.newArrayList("Roberto", "Huang", "DreamT", "Chen");

    @Test
    public void testFilter() {
        // 测试过滤
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.size(), equalTo(4));

        // JDK8方式
        List<String> filterList = dataSource.stream().filter(e -> e != null && e.length() > 4).collect(Collectors.toList());
        assertThat(filterList.size(), equalTo(3));

        // FluentIterable方式
        FluentIterable<String> result = fluentIterable.filter(e -> e != null && e.length() > 4);
        assertThat(result.size(), equalTo(3));
    }

    @Test
    public void testMatch() {
        // 测试匹配
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.size(), equalTo(4));

        // 全匹配
        // JDK8方式
        boolean matchResult = dataSource.stream().allMatch(e -> e != null && e.length() >= 4);
        assertThat(matchResult, is(true));
        // FluentIterable方式
        matchResult = fluentIterable.allMatch(e -> e != null && e.length() >= 4);
        assertThat(matchResult, is(true));

        // 任意匹配
        // JDK8方式
        matchResult = dataSource.stream().anyMatch(e -> e != null && e.length() == 5);
        assertThat(matchResult, is(true));
        // FluentIterable方式
        matchResult = fluentIterable.anyMatch(e -> e != null && e.length() == 5);
        assertThat(matchResult, is(true));

        // 获取首个匹配
        // JDK8方式
        java.util.Optional<String> jdkOptional = dataSource.stream().filter(e -> e != null && e.length() == 5).findFirst();
        assertThat(jdkOptional.isPresent(), is(true));
        assertThat(jdkOptional.get(), equalTo("Huang"));
        // FluentIterable方式
        Optional<String> guavaOptional = fluentIterable.firstMatch(e -> e != null && e.length() == 5);
        assertThat(guavaOptional.isPresent(), is(true));
        assertThat(guavaOptional.get(), equalTo("Huang"));
    }

    @Test
    public void testAppend() {
        // 测试拼接
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.size(), equalTo(4));

        // JDK8方式
        List<String> concatResult = Stream.concat(dataSource.stream(), Stream.of("APPEND")).collect(Collectors.toList());
        assertThat(concatResult.size(), equalTo(5));
        assertThat(concatResult.contains("APPEND"), is(true));
        // FluentIterable方式
        fluentIterable = fluentIterable.append("APPEND");
        assertThat(fluentIterable.size(), equalTo(5));
        assertThat(fluentIterable.contains("APPEND"), is(true));

        // JDK8方式
        concatResult = Stream.concat(concatResult.stream(), Lists.newArrayList("APPEND2").stream()).collect(Collectors.toList());
        assertThat(concatResult.size(), equalTo(6));
        assertThat(concatResult.contains("APPEND2"), is(true));
        // FluentIterable方式
        fluentIterable = fluentIterable.append(Lists.newArrayList("APPEND2"));
        assertThat(fluentIterable.size(), equalTo(6));
        assertThat(fluentIterable.contains("APPEND2"), is(true));
    }

    @Test
    public void testFirst$Last() {
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.size(), equalTo(4));

        // JDK8方式
        java.util.Optional<String> jdkOptional = dataSource.stream().findFirst();
        assertThat(jdkOptional.isPresent(), is(true));
        assertThat(jdkOptional.get(), equalTo("Roberto"));
        // FluentIterable方式
        Optional<String> guavaOptional = fluentIterable.first();
        assertThat(guavaOptional.isPresent(), is(true));
        assertThat(guavaOptional.get(), equalTo("Roberto"));

        // JDK8方式
        jdkOptional = dataSource.stream().skip(((dataSource.size() - 1 > 0) ? (dataSource.size() - 1) : 0)).findFirst();
        assertThat(jdkOptional.isPresent(), is(true));
        assertThat(jdkOptional.get(), equalTo("Chen"));
        // FluentIterable方式
        guavaOptional = fluentIterable.last();
        assertThat(guavaOptional.isPresent(), is(true));
        assertThat(guavaOptional.get(), equalTo("Chen"));
    }

    @Test
    public void testLimit() {
        // JDK8方式
        List<String> limitResult = dataSource.stream().limit(3).collect(Collectors.toList());
        assertThat(limitResult.contains("Chen"), is(false));

        // FluentIterable方式
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        FluentIterable<String> limit = fluentIterable.limit(3);
        assertThat(limit.contains("Chen"), is(false));
    }

    @Test
    public void testCycle() {
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.size(), equalTo(4));
        fluentIterable.cycle().limit(20).forEach(System.out::println);
    }

    @Test
    public void testTransform() {
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.size(), equalTo(4));

        // JDK8方式
        dataSource.stream().map(e -> e.length()).forEach(System.out::println);

        System.out.println("==================================================");

        // FluentIterable方式
        fluentIterable.transform(e -> e.length()).forEach(System.out::println);
    }

    @Test
    public void testTransformAndConcatInAction() {
        ArrayList<Integer> arrayList = Lists.newArrayList(1, 2);

        // JDK8方式
        arrayList.stream().flatMap(type -> search(type).stream()).forEach(System.out::println);

        // FluentIterable方式
        FluentIterable.from(arrayList).transformAndConcat(this::search).forEach(System.out::println);
    }

    @Test
    public void testJoin() {
        // JDK8方式
        String joinResult = dataSource.stream().collect(Collectors.joining(","));
        assertThat(joinResult, equalTo("Roberto,Huang,DreamT,Chen"));

        // FluentIterable方式
        FluentIterable<String> fluentIterable = FluentIterable.from(dataSource);
        assertThat(fluentIterable.join(Joiner.on(',')), equalTo("Roberto,Huang,DreamT,Chen"));
    }

    private List<Customer> search(int type) {
        if (type == 1) {
            return Lists.newArrayList(new Customer(type, "DreamT"), new Customer(type, "Chen"));
        } else {
            return Lists.newArrayList(new Customer(type, "Roberto"), new Customer(type, "Huang"));
        }
    }

    class Customer {
        final int type;
        final String name;

        Customer(int type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Customer{" + "type=" + type + ", name='" + name + '\'' + '}';
        }
    }
}
```

## Lists

> 集合工具类`Lists`

```java
public class ListsExampleTest {
    @Test
    public void testPartition() {
        ArrayList<String> list = Lists.newArrayList("1", "2", "3", "4");
        List<List<String>> result = Lists.partition(list, 2);
        assertThat(result.get(0), equalTo(Lists.newArrayList("1", "2")));
        assertThat(result.get(1), equalTo(Lists.newArrayList("3", "4")));
    }

    @Test
    public void testTransform() {
        ArrayList<String> sourceList = Lists.newArrayList("Roberto", "Huang", "DreamT", "Chen");
        assertThat(Lists.transform(sourceList, e -> e.toUpperCase()), equalTo(Lists.newArrayList("ROBERTO", "HUANG", "DREAMT", "CHEN")));
    }

    @Test
    public void testReverse() {
        ArrayList<String> list = Lists.newArrayList("1", "2", "3");
        assertThat(Joiner.on(",").join(list), equalTo("1,2,3"));

        List<String> result = Lists.reverse(list);
        assertThat(Joiner.on(",").join(result), equalTo("3,2,1"));
    }

    @Test
    public void testCartesianProduct() {
        System.out.println(Lists.cartesianProduct(Lists.newArrayList("1", "2"), Lists.newArrayList("A", "B")));
    }
}
```

## Sets

> 集合工具类`Sets`

```java
public class SetsExampleTest {
    @Test
    public void testCombinations() {
        // 求集合的子集
        Sets.combinations(Sets.newHashSet(1, 2, 3, 4), 2).forEach(System.out::println);
    }

    @Test
    public void testCartesianProduct() {
        // 求两个集合笛卡尔积
        System.out.println(Sets.cartesianProduct(Sets.newHashSet(1, 2), Sets.newHashSet(3, 4)));
    }

    @Test
    public void testDiff() {
        // 求集合差集
        HashSet<Integer> set1 = Sets.newHashSet(1, 2, 3);
        HashSet<Integer> set2 = Sets.newHashSet(1, 4, 6);
        Sets.SetView<Integer> diffResult = Sets.difference(set1, set2);
        assertThat(diffResult, equalTo(Sets.newHashSet(2, 3)));
    }

    @Test
    public void testIntersection() {
        // 求集合交集
        HashSet<Integer> set1 = Sets.newHashSet(1, 2, 3);
        HashSet<Integer> set2 = Sets.newHashSet(1, 4, 6);
        Sets.SetView<Integer> intersection = Sets.intersection(set1, set2);
        assertThat(intersection, equalTo(Sets.newHashSet(2, 3)));
    }

    @Test
    public void testUnionSection() {
        // 求集合并集
        HashSet<Integer> set1 = Sets.newHashSet(1, 2, 3);
        HashSet<Integer> set2 = Sets.newHashSet(1, 4, 6);
        Sets.SetView<Integer> union = Sets.union(set1, set2);
        assertThat(union, equalTo(Sets.newHashSet(1, 2, 3, 4, 6)));
    }
}
```

## Maps

> 集合工具类`Maps`

```java
public class MapsExampleTest {
    @Test
    public void testFilter() {
        Map<String, String> map = Maps.asMap(Sets.newHashSet("1", "2", "3"), k -> k + "_value");

        // Maps.filterEntries  Maps.filterValues同理
        assertThat(Maps.filterKeys(map, k -> Lists.newArrayList("1", "2").contains(k)).containsKey("3"), is(false));
    }

    @Test
    public void testDifferent() {
        Map<String, String> map = Maps.asMap(Sets.newHashSet("1", "2", "3"), k -> k + "_value");
        Map<String, String> map2 = Maps.asMap(Sets.newHashSet("3", "4", "5"), k -> k + "_value");
        MapDifference<String, String> difference = Maps.difference(map, map2);
        assertThat(difference.entriesDiffering(), equalTo(Maps.newHashMap()));
        assertThat(difference.entriesInCommon(), equalTo(Maps.asMap(Sets.newHashSet("3"), k -> k + "_value")));
        assertThat(difference.entriesOnlyOnLeft(), equalTo(Maps.asMap(Sets.newHashSet("1", "2"), k -> k + "_value")));
        assertThat(difference.entriesOnlyOnRight(), equalTo(Maps.asMap(Sets.newHashSet("4", "5"), k -> k + "_value")));
    }

    @Test
    public void testTransform() {
        Map<String, String> map = Maps.asMap(Sets.newHashSet("1", "2", "3"), k -> k + "_value");

        Map<String, String> newMap = Maps.transformValues(map, v -> v + "_transform");

        assertThat(newMap, equalTo(ImmutableMap.of("1", "1_value_transform", "2", "2_value_transform", "3", "3_value_transform")));
    }

    @Test
    public void testCreate() {
        ArrayList<String> valueList = Lists.newArrayList("1", "2", "3");

        ImmutableMap<String, String> map = Maps.uniqueIndex(valueList, v -> v + "_key");
        assertThat(map, equalTo(ImmutableMap.of("1_key", "1", "2_key", "2", "3_key", "3")));

        // asMap输出可变Map
        Map<String, String> map2 = Maps.asMap(Sets.newHashSet("1", "2", "3"), k -> k + "_value");
        assertThat(map2, equalTo(ImmutableMap.of("1", "1_value", "2", "2_value", "3", "3_value")));

        // toMap输出可变Map
        ImmutableMap<String, String> map3 = Maps.toMap(valueList, k -> k + "_value");
        assertThat(map3, equalTo(ImmutableMap.of("1", "1_value", "2", "2_value", "3", "3_value")));
    }
}
```

## BiMap

> 集合工具类`BiMap`

```java
public class BiMapExampleTest {
    @Test
    public void testCreateAndPut() {
        HashBiMap<String, String> hashBiMap = HashBiMap.create();
        hashBiMap.put("1", "2");
        hashBiMap.put("1", "3");
        assertThat(hashBiMap.size(), equalTo(1));
        assertThat(hashBiMap.containsKey("1"), is(true));
        try {
            hashBiMap.put("2", "3");
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue("value already present: 3".equals(e.getMessage()));
        }
    }

    @Test
    public void testCreateAndForcePut() {
        HashBiMap<String, String> hashBiMap = HashBiMap.create();
        hashBiMap.put("1", "2");
        assertThat(hashBiMap.size(), equalTo(1));
        assertThat(hashBiMap.containsKey("1"), is(true));
        
        hashBiMap.forcePut("2", "2");
        assertThat(hashBiMap.containsKey("1"), is(false));
        assertThat(hashBiMap.containsKey("2"), is(true));
    }

    @Test
    public void testBiMapInverse() {
        HashBiMap<String, String> biMap = HashBiMap.create();
        biMap.put("1", "2");
        biMap.put("2", "3");
        biMap.put("3", "4");
        assertThat(biMap.size(), equalTo(3));
        assertThat(biMap.containsKey("1"), is(true));
        assertThat(biMap.containsKey("2"), is(true));
        assertThat(biMap.containsKey("3"), is(true));

        BiMap<String, String> inverseKey = biMap.inverse();
        assertThat(inverseKey.size(), equalTo(3));
        assertThat(inverseKey.containsKey("2"), is(true));
        assertThat(inverseKey.containsKey("3"), is(true));
        assertThat(inverseKey.containsKey("4"), is(true));
    }
}
```

## Multimap

> 集合工具类`Multimap`

```java
public class MultimapsExampleTest {
    @Test
    public void testBasic() {
        HashMap<String, String> hashMap = Maps.newHashMap();
        hashMap.put("1", "1");
        hashMap.put("1", "2");
        assertThat(hashMap.size(), equalTo(1));
        assertThat(hashMap.get("1"), equalTo("2"));

        LinkedListMultimap<String, String> multipleMap = LinkedListMultimap.create();
        multipleMap.put("1", "1");
        multipleMap.put("1", "2");
        assertThat(multipleMap.size(), equalTo(2));
        assertThat(multipleMap.get("1"), equalTo(Lists.newArrayList("1", "2")));
    }
}
```

## Table

> 集合工具类`Table`

```java
public class TableExampleTest {
    @Test
    public void test() {
        Table<String, String, String> table = HashBasedTable.create();
        table.put("Language", "Kafka", "Scala");
        table.put("Language", "RocketMQ", "Java");
        table.put("Database", "Kafka", "File");
        table.put("Database", "RocketMQ", "File");

        Map<String, String> language = table.row("Language");
        assertThat(language.containsKey("Kafka"), is(true));
        assertThat(language.containsKey("RocketMQ"), is(true));
        assertThat(language.get("Kafka"), equalTo("Scala"));
        assertThat(language.get("RocketMQ"), equalTo("Java"));

        Map<String, String> result = table.column("Kafka");
        assertThat(result.containsKey("Language"), is(true));
        assertThat(result.get("Language"), equalTo("Scala"));

        Set<Table.Cell<String, String, String>> cells = table.cellSet();
        assertThat(cells, equalTo(Sets.newHashSet(Tables.immutableCell("Language","Kafka","Scala"), Tables.immutableCell("Language","RocketMQ","Java"), Tables.immutableCell("Database","Kafka","File"), Tables.immutableCell("Database","RocketMQ","File"))));
    }
}
```

## Range

> 工具类`Range`

```java
public class RangeExampleTest {
    @Test
    public void testClosedRange() {
        // {x|a<=x<=b}
        Range<Integer> closedRange = Range.closed(0, 9);
        assertThat(closedRange.contains(5), is(true));
        assertThat(closedRange.lowerEndpoint(), equalTo(0));
        assertThat(closedRange.upperEndpoint(), equalTo(9));
        assertThat(closedRange.contains(0), is(true));
        assertThat(closedRange.contains(9), is(true));
    }

    @Test
    public void testOpenRange() {
        // {x|a<x<b}
        Range<Integer> openRange = Range.open(0, 9);
        assertThat(openRange.contains(5), is(true));
        assertThat(openRange.lowerEndpoint(), equalTo(0));
        assertThat(openRange.upperEndpoint(), equalTo(9));
        assertThat(openRange.contains(0), is(false));
        assertThat(openRange.contains(9), is(false));
    }

    @Test
    public void testOpenClosedRange() {
        // {x|a<x<=b}
        Range<Integer> openClosedRange = Range.openClosed(0, 9);
        assertThat(openClosedRange.contains(5), is(true));
        assertThat(openClosedRange.lowerEndpoint(), equalTo(0));
        assertThat(openClosedRange.upperEndpoint(), equalTo(9));
        assertThat(openClosedRange.contains(0), is(false));
        assertThat(openClosedRange.contains(9), is(true));
    }

    @Test
    public void testClosedOpenRange() {
        // {x|a<=x<b}
        Range<Integer> closedOpen = Range.closedOpen(0, 9);
        assertThat(closedOpen.contains(5), is(true));
        assertThat(closedOpen.lowerEndpoint(), equalTo(0));
        assertThat(closedOpen.upperEndpoint(), equalTo(9));
        assertThat(closedOpen.contains(0), is(true));
        assertThat(closedOpen.contains(9), is(false));
    }

    @Test
    public void testOtherMethod() {
        // (-∞..10)
        Range<Integer> lessThanRange = Range.lessThan(10);
        // (-∞..10]
        Range<Integer> atMostRange = Range.atMost(10);

        // (10..+∞)
        Range<Integer> greaterThanRange = Range.greaterThan(10);
        // [10..+∞)
        Range<Integer> atLeastRange = Range.atLeast(10);

        // (-∞..+∞)
        Range<Integer> allRange = Range.all();
        // (-∞..10]
        Range<Integer> upToRange = Range.upTo(10, BoundType.CLOSED);
        // [10..+∞)
        Range<Integer> downToRange = Range.downTo(10, BoundType.CLOSED);
    }

    @Test
    public void testRangeMap() {
        RangeMap<Integer, String> gradeScale = TreeRangeMap.create();
        gradeScale.put(Range.closed(0, 60), "E");
        gradeScale.put(Range.closed(61, 70), "D");
        gradeScale.put(Range.closed(71, 80), "C");
        gradeScale.put(Range.closed(81, 90), "B");
        gradeScale.put(Range.closed(91, 100), "A");
        assertThat(gradeScale.get(77), equalTo("C"));
    }
}
```

## Immutable

> 不可变集合

```java
public class ImmutableCollectionsTest {
    @Test
    public void testImmutableMap() {
        Map<String, String> map = new HashMap<>() {
            {
                put("DreamT", "Chen");
                put("Roberto", "Huang");
            }
        };

        ImmutableMap<String, String> immutableMap = ImmutableMap.of("DreamT", "Chen", "Roberto", "Huang");

        ImmutableMap<String, String> immutableMap2 = ImmutableMap.copyOf(map);

        ImmutableMap<String, String> immutableMap3 = ImmutableMap.<String, String>builder().put("DreamT", "Chen").put("Roberto", "Huang").build();
    }
}
```

## Ordering

> 集合工具类`Ordering`

```java
public class OrderingExampleTest {
    // natural 使用自然规则(如从小到大、日期先后)创建Ordering
    // usingToString 根据排序值的ToString方法值使用natural创建Ordering
    // from(Comparator) 根据自定义的Comparator创建Ordering

    // reverser() 获取语义相反的排序器
    // nullsFirst() 使用当前的排序器，但额外把null值排到最前面
    // nullsLast() 使用当前的排序器，但额外把null值排到最后面
    // compound(Comparator) 合并另外一个比较器，以处理当前排序器中的相等情况
    // lexicographical() 基于处理类型T的排序器，返回该类型的可迭代对象Iterable<T>的排序器
    // onResultOf(Function) 对集合中元素调用Function方法，再按返回值用当前排序器排序

    // greatestOf(Iterable iterable, int k) 获取可迭代对象中最大的K个元素
    // isOrdered(Iterable iterable) 判断可迭代对象是否已按排序器排序，允许有排序值相等的元素
    // sortedCopy(Iterable iterable) 判断可迭代对象是否已严格按排序器排序，不允许排序值相等的元素
    // min(E, E) 返回两个参数中最小的那个，如果相等则返回第一个参数
    // min(E, E, E, E, E...) 返回多个参数中最小的那个，如果有超过一个参数都最小，则返回第一个最小的参数
    // min(Iterable iterable) 返回迭代器中最小元素，如果可迭代对象中没有元素则抛出NoSuchElementException

    @Test
    public void testOrderNaturalByNullFirstOrLast() {
        List<Integer> list = Arrays.asList(1, 8, null, 6, 4, 9, 8, 7);

        Collections.sort(list, Ordering.natural().nullsFirst());
        assertThat(list, equalTo(Lists.newArrayList(null, 1, 4, 6, 7, 8, 8, 9)));

        Collections.sort(list, Ordering.natural().nullsLast());
        assertThat(list, equalTo(Lists.newArrayList(1, 4, 6, 7, 8, 8, 9, null)));
    }

    @Test
    public void testOrderReverse() {
        // 按位比较
        List list = Arrays.asList(Lists.newArrayList(3, 3), Lists.newArrayList(2, 4), Lists.newArrayList(1, 5), Lists.newArrayList(1, 6), Lists.newArrayList(2, 3));
        Collections.sort(list, Ordering.natural().lexicographical());
        assertThat(list, equalTo(Lists.newArrayList(Lists.newArrayList(1, 5), Lists.newArrayList(1, 6), Lists.newArrayList(2, 3), Lists.newArrayList(2, 4), Lists.newArrayList(3, 3))));
    }
}
```

