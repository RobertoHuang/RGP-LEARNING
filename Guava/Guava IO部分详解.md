# IO

## Files简单使用

`Files`相关`API`比较简洁明了(略)

注:使用`ByteSource/CharSource/ByteSink/CharSink`机制可有效的解决的数据流关闭异常可能会导致的问题

## Closer简单使用

> 防止`finally`代码块关闭资源出现异常，导致`catch`块异常被吞没

```java
Closer closer = Closer.create();
try {
    InputStream in = closer.register(openInputStream());
    OutputStream out = closer.register(openOutputStream());
} catch (Throwable e) {
    throw closer.rethrow(e);
} finally {
    closer.close();
}
```

## BaseEncodingTest简单使用

```java
public class BaseEncodingTest {
    @Test
    public void testBase64Encode() {
        BaseEncoding baseEncoding = BaseEncoding.base64();
        System.out.println(baseEncoding.encode("hello".getBytes()));
    }

    @Test
    public void testBase64Decode() {
        BaseEncoding baseEncoding = BaseEncoding.base64();
        System.out.println(new String(baseEncoding.decode("aGVsbG8=")));
    }
}
```

