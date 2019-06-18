/**
 * FileName: CloserTest
 * Author:   HuangTaiHong
 * Date:     2019/6/18 9:41
 * Description: Closer Test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.guava.test.io;

/**
 * 〈Closer Test.〉
 *
 *  Closer closer = Closer.create();
 *  try {
 *      InputStream in = closer.register(openInputStream());
 *      OutputStream out = closer.register(openOutputStream());
 *  } catch (Throwable e) {
 *      throw closer.rethrow(e);
 *  } finally {
 *      closer.close();
 *  }
 *
 *  // 防止finally代码块关闭资源出现异常，导致catch块异常被吞没
 *
 * @author HuangTaiHong
 * @create 2019/6/18
 * @since 1.0.0
 */
public class CloserTest {

}