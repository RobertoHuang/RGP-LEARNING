/**
 * FileName: StopWatchTest
 * Author:   HuangTaiHong
 * Date:     2019/6/14 15:27
 * Description: Stop Watch Test.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.growth.process.guava.test.utilities;

import com.google.common.base.Stopwatch;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * 〈Stop Watch Test.〉
 *
 * @author HuangTaiHong
 * @create 2019/6/14
 * @since 1.0.0
 */
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