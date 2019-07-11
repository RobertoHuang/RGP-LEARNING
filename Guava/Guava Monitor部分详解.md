# Monitor

```java
public class MonitorExample {
    public static void main(String[] args) {
        final MonitorGuard monitorGuard = new MonitorGuard();
        final AtomicInteger COUNTER = new AtomicInteger(0);
        for (int i = 0; i <= 3; i++) {
            new Thread(() -> {
                for (; ; )
                    try {
                        int data = COUNTER.getAndIncrement();
                        System.out.println(currentThread() + " offer " + data);
                        monitorGuard.offer(data);
                        TimeUnit.MILLISECONDS.sleep(2);
                    } catch (InterruptedException e) {

                    }
            }).start();
        }

        for (int i = 0; i <= 2; i++) {
            new Thread(() -> {
                for (; ; )
                    try {
                        int data = monitorGuard.take();
                        System.out.println(currentThread() + " take " + data);
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {

                    }
            }).start();
        }
    }

    static class MonitorGuard {
        private final int MAX = 10;
        private final LinkedList<Integer> queue = new LinkedList<>();

        private final Monitor monitor = new Monitor();
        private final Monitor.Guard CAN_TAKE = monitor.newGuard(() -> !queue.isEmpty());
        private final Monitor.Guard CAN_OFFER = monitor.newGuard(() -> queue.size() < MAX);

        public void offer(int value) {
            try {
                monitor.enterWhen(CAN_OFFER);
                queue.addLast(value);
            } catch (InterruptedException e) {

            } finally {
                monitor.leave();
            }
        }

        public int take() {
            try {
                monitor.enterWhen(CAN_TAKE);
                return queue.removeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                monitor.leave();
            }
        }
    }

    static class LockCondition {
        private final ReentrantLock reentrantLock = new ReentrantLock();
        private final Condition FULL_CONDITION = reentrantLock.newCondition();
        private final Condition EMPTY_CONDITION = reentrantLock.newCondition();

        private final int MAX = 10;
        private final LinkedList<Integer> queue = new LinkedList<>();

        public void offer(int value) {
            try {
                reentrantLock.lock();
                while (queue.size() >= MAX) {
                    FULL_CONDITION.await();
                }
                queue.addLast(value);
                EMPTY_CONDITION.signalAll();
            } catch (InterruptedException e) {

            } finally {
                reentrantLock.unlock();
            }
        }

        public int take() {
            Integer value = null;
            try {
                reentrantLock.lock();
                while (queue.isEmpty()) {
                    EMPTY_CONDITION.await();
                }
                value = queue.removeFirst();
                FULL_CONDITION.signalAll();
            } catch (InterruptedException e) {

            } finally {
                reentrantLock.unlock();
            }
            return value;
        }
    }

    static class Synchronized {
        private final int MAX = 10;
        private final LinkedList<Integer> queue = new LinkedList<>();

        public void offer(int value) {
            synchronized (queue) {
                while (queue.size() >= MAX) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {

                    }
                }
                queue.addLast(value);
                queue.notifyAll();
            }
        }

        public int take() {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {

                    }
                }
                Integer value = queue.removeFirst();
                queue.notifyAll();
                return value;
            }
        }
    }
}
```

```java
// 进入到当前Monitor，无限期阻塞等待锁
enter()
// 进入到当前Monitor，最多阻塞给定的时间返回是否进入Monitor
enter(long time, TimeUnit unit)
// 如果可以的话立即进入Monitor，不阻塞，返回是否进入Monitor
tryEnter()
// 进入当前Monitor，等待Guard的isSatisfied()为true后继续往下执行 ，但可能会被打断
enterWhen(Guard guard)
// 如果Guard的isSatisfied()为true进入当前Monitor等待获得锁，不需要等待Guard satisfied
enterIf(Guard guard)
// 如果Guard的isSatisfied()为true并且获得锁立即进入Monitor，不等待获取锁，也不等待Guard satisfied
tryEnterIf(Guard guard)：
```

