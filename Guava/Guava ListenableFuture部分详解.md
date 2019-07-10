# ListenableFuture

```java
public class ListenableFutureExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(2);

        /* ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(service);
        ListenableFuture<Integer> future = listeningExecutorService.submit(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {

            }
            return 100;
        });
        Futures.addCallback(future, new MyCallBack(), service); */

        // 进行变换
        // public <U> CompletionStage<U> thenApply(Function<? super T,? extends U> fn);
        // public <U> CompletionStage<U> thenApplyAsync(Function<? super T,? extends U> fn);
        // public <U> CompletionStage<U> thenApplyAsync(Function<? super T,? extends U> fn,Executor executor);

        // 进行消耗
        // public CompletionStage<Void> thenAccept(Consumer<? super T> action);
        // public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
        // public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);

        // 对上一步的计算结果不关心，执行下一个操作
        // public CompletionStage<Void> thenRun(Runnable action);
        // public CompletionStage<Void> thenRunAsync(Runnable action);
        // public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);

        // 结合两个CompletionStage的结果，进行转化后返回
        // public <U,V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
        // public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
        // public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn,Executor executor);

        // 结合两个CompletionStage的结果，进行消耗
        // public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
        // public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
        // public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action,     Executor executor);

        // 在两个CompletionStage都运行完执行
        // public CompletionStage<Void> runAfterBoth(CompletionStage<?> other,Runnable action);
        //public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,Runnable action);
        //public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,Runnable action,Executor executor);

        // 两个CompletionStage，谁计算的快就用那个CompletionStage的结果进行下一步的转化操作
        // public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,Function<? super T, U> fn);
        // public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn);
        // public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn,Executor executor);

        // 两个CompletionStage，谁计算的快就用那个CompletionStage的结果进行下一步的消耗操作
        // public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other,Consumer<? super T> action);
        // public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,Consumer<? super T> action);
        // public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,Consumer<? super T> action,Executor executor);

        // 两个CompletionStage，任何一个完成了都会执行下一步的操作
        // public CompletionStage<Void> runAfterEither(CompletionStage<?> other,Runnable action);
        // public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other,Runnable action);
        // public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other,Runnable action,Executor executor);

        // 当运行时出现了异常，可以通过exceptionally进行补偿
        // public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn);

        // 当运行完成时对结果的记录。这里的完成时有两种情况，一种是正常执行返回值 另外一种是遇到异常抛出造成程序的中断
        // public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action);
        // public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action);
        // public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action,Executor executor);

        // 运行完成时对结果的处理。这里的完成时有两种情况，一种是正常执行返回值 另外一种是遇到异常抛出造成程序的中断
        // public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn);
        // public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn);
        // public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn,Executor executor);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {

            }
            return 100;
        }, service).whenComplete((v, t) -> System.out.println("I am finished and the result is " + v));
    }

    static class MyCallBack implements FutureCallback<Integer> {
        @Override
        public void onFailure(Throwable t) {
            t.printStackTrace();
        }

        @Override
        public void onSuccess(@Nullable Integer result) {
            System.out.println("I am finished and the result is " + result);
        }
    }
}
```

