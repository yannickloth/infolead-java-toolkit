package eu.infolead.jtk.async;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Shared timer infrastructure for deadlines and retry backoff.
 *
 * <p>The pool has a single daemon thread and removes cancelled tasks from its queue immediately, so
 * a cancelled deadline cannot retain the operation it was guarding. The thread is a daemon: the JVM
 * can exit even when the pool was never shut down.
 */
final class Timers {

    private static final ScheduledExecutorService SHARED = create();

    private Timers() {
        // Utility class
    }

    /**
     * Returns the shared timer used by default contexts.
     *
     * @return the shared scheduler
     */
    static ScheduledExecutorService shared() {
        return SHARED;
    }

    private static ScheduledExecutorService create() {
        final ScheduledThreadPoolExecutor executor =
                new ScheduledThreadPoolExecutor(
                        1,
                        runnable -> {
                            final Thread thread = new Thread(runnable, "jtk-async-timer");
                            thread.setDaemon(true);
                            return thread;
                        });
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        return executor;
    }
}
