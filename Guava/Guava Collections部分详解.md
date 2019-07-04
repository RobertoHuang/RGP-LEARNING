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

