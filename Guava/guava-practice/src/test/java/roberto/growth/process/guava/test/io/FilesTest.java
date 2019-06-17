/**
 * FileName: FilesTest
 * Author:   HuangTaiHong
 * Date:     2019/6/17 16:13
 * Description: Files Test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.guava.test.io;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.CharSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 〈Files Test.〉
 *
 *  ByteSource/CharSource/ByteSink/CharSink/Closer
 *  该机制有效的解决的数据流关闭异常可能会导致的问题，更安全
 *
 * @author HuangTaiHong
 * @create 2019/6/17
 * @since 1.0.0
 */
public class FilesTest {
    private static final String HOME_PATH = System.getProperty("user.home").replace('\\', '/');
    private final String SOURCE_FILE = HOME_PATH + "/source.txt";
    private final String TARGET_FILE = HOME_PATH + "/target.txt";

    @Before
    public void before() throws IOException {
        File sourceFile = new File(SOURCE_FILE);
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
        FileWriter fileWriter = new FileWriter(sourceFile);
        fileWriter.write("Roberto-Huang!");
        fileWriter.flush();
        fileWriter.close();
    }

    @Test
    public void testTouchFile() throws IOException {
        File touchFile = new File(SOURCE_FILE);
        if (touchFile.exists()) {
            touchFile.delete();
        }
        Files.touch(touchFile);
        assertThat(touchFile.exists(), equalTo(true));
    }

    @Test
    public void testTreeFilesPreOrderTraversal() {
        File root = new File(HOME_PATH);
        FluentIterable<File> files = Files.fileTreeTraverser().preOrderTraversal(root);
        files.stream().forEach(System.out::println);
    }

    @Test
    public void testTreeFilesPostOrderTraversal() {
        File root = new File(HOME_PATH);
        FluentIterable<File> files = Files.fileTreeTraverser().postOrderTraversal(root);
        files.stream().forEach(System.out::println);
    }

    @Test
    public void testTreeFilesBreadthFirstTraversal() {
        File root = new File(HOME_PATH);
        FluentIterable<File> files = Files.fileTreeTraverser().breadthFirstTraversal(root);
        files.stream().forEach(System.out::println);
    }

    @Test
    public void testTreeFilesChild() {
        File root = new File(HOME_PATH);
        Iterable<File> children = Files.fileTreeTraverser().children(root);
        children.forEach(System.out::println);
    }

    @Test
    public void testCopyFile() throws IOException {
        File targetFile = new File(TARGET_FILE);
        File sourceFile = new File(SOURCE_FILE);
        Files.copy(sourceFile, targetFile);
        assertThat(targetFile.exists(), equalTo(true));

        HashCode sourceHashCode = Files.asByteSource(sourceFile).hash(Hashing.sha256());
        HashCode targetHashCode = Files.asByteSource(targetFile).hash(Hashing.sha256());
        assertThat(sourceHashCode.toString(), equalTo(targetHashCode.toString()));
    }

    @Test
    public void testMoveFile() throws IOException {
        Files.move(new File(SOURCE_FILE), new File(TARGET_FILE));
        assertThat(new File(TARGET_FILE).exists(), equalTo(true));
        assertThat(new File(SOURCE_FILE).exists(), equalTo(false));
    }

    @Test
    public void testToString() throws IOException {
        final String expectedString = "Roberto-Huang!";
        List<String> strings = Files.readLines(new File(SOURCE_FILE), Charsets.UTF_8);
        String result = Joiner.on("\n").join(strings);
        assertThat(result, equalTo(expectedString));
    }

    @Test
    public void testToProcessString() throws IOException {
        final String expectedString = "Roberto-Huang!";
        LineProcessor<List<String>> lineProcessor = new LineProcessor<List<String>>() {
            private final List<String> lengthList = new ArrayList<>();

            @Override
            public boolean processLine(String line) {
                if (line.length() == 0)
                    return false;
                lengthList.add(line);
                return true;
            }

            @Override
            public List<String> getResult() {
                return lengthList;
            }
        };
        List<String> result = Files.asCharSource(new File(SOURCE_FILE), Charsets.UTF_8).readLines(lineProcessor);
        assertThat(result, equalTo(expectedString));
    }

    @Test
    public void testFileWrite() throws IOException {
        File testFile = new File(SOURCE_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }

        String content1 = "content1";
        Files.asCharSink(testFile, Charsets.UTF_8).write(content1);
        String actually = Files.asCharSource(testFile, Charsets.UTF_8).read();
        assertThat(actually, equalTo(content1));

        String content2 = "content2";
        Files.asCharSink(testFile, Charsets.UTF_8).write(content2);
        String actually2 = Files.asCharSource(testFile, Charsets.UTF_8).read();
        assertThat(actually2, equalTo(content2));
    }

    @Test
    public void testFileAppend() throws IOException {
        File testFile = new File(SOURCE_FILE);
        if (testFile.exists()) {
            testFile.delete();
        }

        CharSink charSink = Files.asCharSink(testFile, Charsets.UTF_8, FileWriteMode.APPEND);
        charSink.write("content1");
        String actually = Files.asCharSource(testFile, Charsets.UTF_8).read();
        assertThat(actually, equalTo("content1"));

        CharSink charSink2 = Files.asCharSink(testFile, Charsets.UTF_8, FileWriteMode.APPEND);
        charSink2.write("content2");
        String actullay2 = Files.asCharSource(testFile, Charsets.UTF_8).read();
        assertThat(actullay2, equalTo("content1content2"));
    }

    @After
    public void testAfter() {
        new File(SOURCE_FILE).deleteOnExit();

        new File(TARGET_FILE).deleteOnExit();
    }
}