/**
 * FileName: StringsTest
 * Author:   HuangTaiHong
 * Date:     2019/6/13 20:48
 * Description: Strings Test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.guava.test.utilities;

import com.google.common.base.Strings;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * 〈Strings Test.〉
 *
 * @author HuangTaiHong
 * @create 2019/6/13
 * @since 1.0.0
 */
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