# Utilities

## Joiner

```java
public class JoinerTest {
    private final List<String> stringList = Arrays.asList("Google", "Guava", "Java", "Scala", "Kafka");
    private final List<String> stringListWithNullValue = Arrays.asList("Google", "Guava", "Java", "Scala", null);
    private final Map<String, String> stringMap = ImmutableMap.of("Hello", "Guava", "Java", "Scala");

    private final String targetFileName = HOME_PATH + "/.guava" + "guava-joiner.txt";
    public static final String HOME_PATH = System.getProperty("user.home").replace('\\', '/');

    @Test
    // 字符串拼接
    public void testJoinOnJoin() {
        String result = Joiner.on("#").join(stringList);
        assertThat(result, equalTo("Google#Guava#Java#Scala#Kafka"));
    }

    @Test(expected = NullPointerException.class)
    // 字符串拼接如果出现Null值则发生空指针异常
    public void testJoinOnJoinWithNullValue() {
        String result = Joiner.on("#").join(stringListWithNullValue);
        assertThat(result, equalTo("Google#Guava#Java#Scala#Kafka"));
    }

    @Test
    // 字符创拼接跳过Null值
    public void testJoinOnJoinWithNullValueButSkip() {
        String result = Joiner.on("#").skipNulls().join(stringListWithNullValue);
        assertThat(result, equalTo("Google#Guava#Java#Scala"));
    }

    @Test
    // 字符串拼接如果遇到Null则使用默认值替换
    public void testJoinOnJoinWithNullValueUseDefaultValue() {
        String result = Joiner.on("#").useForNull("DEFAULT").join(stringListWithNullValue);
        assertThat(result, equalTo("Google#Guava#Java#Scala#DEFAULT"));
    }

    @Test
    // 将Map数据转换并拼接成字符串
    public void testJoinOnWithMap() {
        assertThat(Joiner.on('#').withKeyValueSeparator("=").join(stringMap), equalTo("Hello=Guava#Java=Scala"));
    }

    @Test
    // 字符串拼接 -> 拼接到StringBuilder上
    public void testJoinOnAppendToStringBuilder() {
        final StringBuilder builder = new StringBuilder();
        StringBuilder resultBuilder = Joiner.on("#").useForNull("DEFAULT").appendTo(builder, stringListWithNullValue);
        assertThat(resultBuilder, sameInstance(builder));
        assertThat(builder.toString(), equalTo("Google#Guava#Java#Scala#DEFAULT"));
        assertThat(resultBuilder.toString(), equalTo("Google#Guava#Java#Scala#DEFAULT"));
    }

    @Test
    // 字符串拼接 -> 拼接到FileWriter上
    public void testJoinOnAppendToWriter() {
        try (FileWriter writer = new FileWriter(new File(targetFileName))) {
            Joiner.on("#").useForNull("DEFAULT").appendTo(writer, stringListWithNullValue);
            assertThat(Files.isFile().test(new File(targetFileName)), equalTo(true));
        } catch (IOException e) {
            fail("append to the writer occur fetal error.");
        }
    }
}
```

## Splitter

```java
public class SplitterTest {
    @Test
    // 分割字符串 - 普通
    public void testSplitOnSplit() {
        List<String> result = Splitter.on("|").splitToList("hello|world");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0), equalTo("hello"));
        assertThat(result.get(1), equalTo("world"));
    }

    @Test
    // 分割字符串 - 过滤掉空值
    public void testSplitOnSplitOmitEmpty() {
        List<String> result = Splitter.on("|").splitToList("hello|world|||");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(5));

        result = Splitter.on("|").omitEmptyStrings().splitToList("hello|world|||");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(2));
    }

    @Test
    // 分割字符串 - 对分割结果进行Trim操作
    public void testSplitOnSplitOmitEmptyTrimResult() {
        List<String> result = Splitter.on("|").omitEmptyStrings().splitToList("hello | world|||");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0), equalTo("hello "));
        assertThat(result.get(1), equalTo(" world"));

        result = Splitter.on("|").omitEmptyStrings().trimResults().splitToList("hello | world|||");
        assertThat(result.get(0), equalTo("hello"));
        assertThat(result.get(1), equalTo("world"));
    }

    @Test
    // 分割字符串 - 按指定步长进行分割
    public void testSplitFixLength() {
        List<String> result = Splitter.fixedLength(4).splitToList("aaaabbbbccccdddd");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(4));
        assertThat(result.get(0), equalTo("aaaa"));
        assertThat(result.get(3), equalTo("dddd"));
    }

    @Test
    // 分割字符串 - 限制分割结果数量
    public void testSplitOnSplitLimit() {
        List<String> result = Splitter.on("#").limit(3).splitToList("hello#world#java#google#scala");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(3));
        assertThat(result.get(0), equalTo("hello"));
        assertThat(result.get(1), equalTo("world"));
        assertThat(result.get(2), equalTo("java#google#scala"));
    }

    @Test
    // 分割字符串 - 根据正则表达式进行分割
    public void testSplitOnPatternString() {
        List<String> result = Splitter.onPattern("\\|").trimResults().omitEmptyStrings().splitToList("hello | world|||");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0), equalTo("hello"));
        assertThat(result.get(1), equalTo("world"));
    }

    @Test
    // 分割字符串 - 根据正则表达式进行分割
    public void testSplitOnPattern() {
        List<String> result = Splitter.on(Pattern.compile("\\|")).trimResults().omitEmptyStrings().splitToList("hello | world|||");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(2));
        assertThat(result.get(0), equalTo("hello"));
        assertThat(result.get(1), equalTo("world"));
    }

    @Test
    // 分割字符串 - 将分割结果转换成Map对象
    public void testSplitOnSplitToMap() {
        Map<String, String> result = Splitter.on(Pattern.compile("\\|")).trimResults().omitEmptyStrings().withKeyValueSeparator("=").split("hello=HELLO| world=WORLD|||");
        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(2));
        assertThat(result.get("hello"),equalTo("HELLO"));
        assertThat(result.get("world"),equalTo("WORLD"));
    }
}
```

## Preconditions

```java
public class PreconditionsTest {
    @Test(expected = NullPointerException.class)
    public void testCheckNotNull() {
        checkNotNull(null);
    }

    @Test
    public void testCheckNotNullWithMessage() {
        try {
            checkNotNullWithMessage(null);
        } catch (Exception e) {
            assertThat(e, is(NullPointerException.class));
            assertThat(e.getMessage(), equalTo("The list should not be null"));
        }
    }

    @Test
    public void testCheckNotNullWithFormatMessage() {
        try {
            checkNotNullWithFormatMessage(null);
        } catch (Exception e) {
            assertThat(e, is(NullPointerException.class));
            assertThat(e.getMessage(), equalTo("The list should not be null and the size must be 2"));
        }
    }

    @Test
    public void testCheckArguments() {
        final String type = "A";
        try {
            Preconditions.checkArgument(type.equals("B"));
        } catch (Exception e) {
            assertThat(e, is(IllegalArgumentException.class));
        }
    }

    @Test
    public void testCheckState() {
        try {
            final String state = "A";
            Preconditions.checkState(state.equals("B"));
            fail("should not process to here.");
        } catch (Exception e) {
            assertThat(e, is(IllegalStateException.class));
        }
    }

    @Test
    public void testCheckIndex() {
        try {
            List<String> list = ImmutableList.of();
            Preconditions.checkElementIndex(10, list.size());
        } catch (Exception e) {
            assertThat(e, is(IndexOutOfBoundsException.class));
        }
    }

    private void checkNotNull(final List<String> list) {
        Preconditions.checkNotNull(list);
    }

    private void checkNotNullWithMessage(final List<String> list) {
        Preconditions.checkNotNull(list, "The list should not be null");
    }

    private void checkNotNullWithFormatMessage(final List<String> list) {
        Preconditions.checkNotNull(list, "The list should not be null and the size must be %s", 2);
    }
}
```

## Strings

```java
public class StringsTest {
    @Test
    public void testStringsMethod() {
        assertThat(Strings.emptyToNull(""), nullValue());

        assertThat(Strings.nullToEmpty(null), equalTo(""));

        assertThat(Strings.commonPrefix("Hello", "Hit"), equalTo("H"));

        assertThat(Strings.commonSuffix("Hello", "Echo"), equalTo("o"));

        assertThat(Strings.repeat("Alex", 3), equalTo("AlexAlexAlex"));

        assertThat(Strings.isNullOrEmpty(null), equalTo(true));

        assertThat(Strings.isNullOrEmpty(""), equalTo(true));

        assertThat(Strings.padStart("Alex", 3, 'H'), equalTo("Alex"));

        assertThat(Strings.padStart("Alex", 5, 'H'), equalTo("HAlex"));

        assertThat(Strings.padEnd("Alex", 5, 'H'), equalTo("AlexH"));
    }
}
```

## Charsets

```java
public class CharsetsTest {
    @Test
    public void testCharsets() {
        Charset charset = Charset.forName("UTF-8");
        assertThat(Charsets.UTF_8, equalTo(charset));
    }

    @Test
    public void testCharMatcher() {
        assertThat(CharMatcher.javaDigit().matches('5'), equalTo(true));

        assertThat(CharMatcher.javaDigit().matches('x'), equalTo(false));

        assertThat(CharMatcher.is('A').countIn("Alex Sharing the Google Guava to Us"), equalTo(1));

        assertThat(CharMatcher.breakingWhitespace().collapseFrom("      hello Guava     ", '*'), equalTo("*hello*Guava*"));

        assertThat(CharMatcher.javaDigit().or(CharMatcher.whitespace()).removeFrom("hello 234 world"), equalTo("helloworld"));

        assertThat(CharMatcher.javaDigit().or(CharMatcher.whitespace()).retainFrom("hello 234 world"), equalTo(" 234 "));
    }
}
```

## StopWatch

```java
public class StopWatchTest {
    @Test
    public void testStopWatch() throws InterruptedException {
        // 创建stopwatch并开始计时
        Stopwatch stopwatch = Stopwatch.createStarted();

        // 向下取整
        Thread.sleep(1900);
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));

        // 停止计时
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));

        // 再次计时
        stopwatch.start();
        Thread.sleep(100);
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));

        // 重置并开始
        stopwatch.reset().start();
        Thread.sleep(3000);
        System.out.println(stopwatch.isRunning());
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
    }
}
```

