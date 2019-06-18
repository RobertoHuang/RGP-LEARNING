/**
 * FileName: BaseEncodingTest
 * Author:   HuangTaiHong
 * Date:     2019/6/18 10:05
 * Description: Base Encoding Test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.guava.test.io;

import com.google.common.io.BaseEncoding;
import org.junit.Test;

/**
 * 〈Base Encoding Test.〉
 *
 * @author HuangTaiHong
 * @create 2019/6/18
 * @since 1.0.0
 */
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