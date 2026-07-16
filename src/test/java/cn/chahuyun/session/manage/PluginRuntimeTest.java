package cn.chahuyun.session.manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginRuntimeTest {

    @AfterEach
    void tearDown() {
        PluginRuntime.stop();
    }

    @Test
    void scheduledExecutorIsReusedAndTerminatedWithRuntime() throws Exception {
        PluginRuntime.start();
        CountDownLatch completed = new CountDownLatch(3);

        PluginRuntime.schedule(completed::countDown, 0, TimeUnit.MILLISECONDS);
        PluginRuntime.schedule(completed::countDown, 0, TimeUnit.MILLISECONDS);
        assertTrue(PluginRuntime.submitInteraction(completed::countDown));

        assertTrue(completed.await(2, TimeUnit.SECONDS));
        long runningThreads = runtimeThreadCount();
        assertTrue(runningThreads >= 1 && runningThreads <= 3);
        Set<Thread> cronTimers = cronTimerThreads();
        assertFalse(cronTimers.isEmpty());

        PluginRuntime.stop();

        assertThrows(IllegalStateException.class, PluginRuntime::cron);
        assertTrue(waitUntilNoRuntimeThreads(Duration.ofSeconds(2)));
        assertTrue(cronTimers.stream().noneMatch(Thread::isAlive));
    }

    @Test
    void repeatedReloadsDoNotAccumulateRuntimeThreads() {
        for (int index = 0; index < 20; index++) {
            PluginRuntime.start();
            PluginRuntime.stop();
        }

        assertFalse(Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.isAlive() && thread.getName().startsWith("HuYanSession-scheduler-")));
    }

    @Test
    void interactionQueueRejectsWorkBeyondItsConfiguredBound() throws Exception {
        PluginRuntime.start(1, 1);
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);

        assertTrue(PluginRuntime.submitInteraction(() -> {
            firstStarted.countDown();
            try {
                releaseFirst.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }));
        assertTrue(firstStarted.await(2, TimeUnit.SECONDS));
        assertTrue(PluginRuntime.submitInteraction(() -> { }));
        assertFalse(PluginRuntime.submitInteraction(() -> { }));

        releaseFirst.countDown();
    }

    private static boolean waitUntilNoRuntimeThreads(Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (runtimeThreadCount() == 0) {
                return true;
            }
            Thread.sleep(20);
        }
        return runtimeThreadCount() == 0;
    }

    private static long runtimeThreadCount() {
        return Thread.getAllStackTraces().keySet().stream()
                .filter(Thread::isAlive)
                .filter(thread -> thread.getName().startsWith("HuYanSession-scheduler-"))
                .count();
    }

    private static Set<Thread> cronTimerThreads() {
        return Thread.getAllStackTraces().entrySet().stream()
                .filter(entry -> Arrays.stream(entry.getValue())
                        .anyMatch(frame -> "cn.hutool.cron.CronTimer".equals(frame.getClassName())))
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
