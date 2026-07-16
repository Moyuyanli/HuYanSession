package cn.chahuyun.session.manage;

import cn.chahuyun.session.HuYanSession;
import cn.hutool.cron.Scheduler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Owns background resources whose lifetime must not exceed the plugin lifetime.
 */
public final class PluginRuntime {

    private static final AtomicInteger THREAD_NUMBER = new AtomicInteger();
    private static ScheduledThreadPoolExecutor delayedExecutor;
    private static ThreadPoolExecutor interactionExecutor;
    private static ExecutorService cronExecutor;
    private static Scheduler cronScheduler;

    private PluginRuntime() {
    }

    public static synchronized void start() {
        start(8, 100);
    }

    public static synchronized void start(int configuredInteractionThreads, int configuredQueueCapacity) {
        if (delayedExecutor != null && !delayedExecutor.isShutdown()) {
            return;
        }
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "HuYanSession-scheduler-" + THREAD_NUMBER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
        delayedExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        delayedExecutor.setRemoveOnCancelPolicy(true);
        delayedExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        delayedExecutor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

        int interactionThreads = Math.max(1, configuredInteractionThreads);
        int interactionQueueCapacity = Math.max(1, configuredQueueCapacity);
        interactionExecutor = new ThreadPoolExecutor(
                interactionThreads,
                interactionThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(interactionQueueCapacity),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );

        cronExecutor = new ThreadPoolExecutor(
                2,
                2,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1000),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        cronScheduler = new Scheduler()
                .setDaemon(true)
                .setThreadExecutor(cronExecutor)
                .setMatchSecond(true);
        cronScheduler.start(true);
    }

    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return executor().schedule(task, delay, unit);
    }

    public static boolean submitInteraction(Runnable task) {
        ThreadPoolExecutor executor;
        synchronized (PluginRuntime.class) {
            executor = interactionExecutor;
        }
        if (executor == null || executor.isShutdown()) {
            return false;
        }
        try {
            executor.execute(() -> {
                try {
                    task.run();
                } catch (cn.chahuyun.session.exception.InteractionTimeoutException ignored) {
                    // Timeout is a normal end state and the user has already been notified.
                } catch (Exception exception) {
                    HuYanSession.LOGGER.error("执行交互指令失败", exception);
                }
            });
            return true;
        } catch (java.util.concurrent.RejectedExecutionException ignored) {
            return false;
        }
    }

    public static synchronized Scheduler cron() {
        if (cronScheduler == null || !cronScheduler.isStarted()) {
            throw new IllegalStateException("Plugin runtime is not running");
        }
        return cronScheduler;
    }

    private static synchronized ScheduledThreadPoolExecutor executor() {
        if (delayedExecutor == null || delayedExecutor.isShutdown()) {
            throw new IllegalStateException("Plugin runtime is not running");
        }
        return delayedExecutor;
    }

    public static synchronized void stop() {
        InteractionManager.shutdown();
        JoinRequestManager.shutdown();
        try {
            if (cronScheduler != null) {
                cronScheduler.stop(true);
                cronScheduler.clear();
            }
        } catch (Exception exception) {
            HuYanSession.LOGGER.warning("停止 Cron 调度器失败: " + exception.getMessage());
        } finally {
            cronScheduler = null;
            if (cronExecutor != null) {
                shutdownExecutor(cronExecutor);
                cronExecutor = null;
            }
        }
        if (delayedExecutor != null) {
            shutdownExecutor(delayedExecutor);
            delayedExecutor = null;
        }
        if (interactionExecutor != null) {
            shutdownExecutor(interactionExecutor);
            interactionExecutor = null;
        }
    }

    private static void shutdownExecutor(ExecutorService executor) {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                HuYanSession.LOGGER.warning("后台线程未能在超时时间内退出");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
