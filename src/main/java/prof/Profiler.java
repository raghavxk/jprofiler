package prof;

import javax.annotation.processing.SupportedSourceVersion;
import java.time.Duration;

public class Profiler implements Runnable {
    private final Options opts;
    private final Store store;
    private volatile boolean stop = false;

    public Profiler(Options options) {
        this.opts = options;
        this.store = new Store(options.getFlamePath());
    }

    public static Profiler newInstance(Options opts) {
        Profiler profiler = new Profiler(opts);
        Runtime.getRuntime().addShutdownHook(new Thread(profiler::onEnd));
        return profiler;
    }

    private static void sleep(Duration duration) throws InterruptedException {
        if (duration.isNegative() || duration.isZero()) {
            return;
        }
        Thread.sleep(duration.toMillis(), duration.toNanosPart() % 1000000);
    }

    @Override
    public void run() {
        while (!stop) {
            Duration start = Duration.ofNanos(System.nanoTime());
            sample();
            Duration duration = Duration.ofNanos(System.nanoTime()).minus(start);
            Duration sleep = opts.getInterval().minus(duration);
            try {
                sleep(sleep);
            } catch (InterruptedException e) {
                break;
            }

            if (opts.printMethodTable()) {
                store.printMethodTable();
            }
            store.storeFlameGraphIfNeeded();
            stop = false;
        }
    }

    private void sample() {
        Thread.getAllStackTraces().forEach((thread, stackTraceElements) -> {
            if (thread.isDaemon()) {
                return;
            }
            store.addSample(stackTraceElements);
        });
    }

    private void onEnd() {
        stop = true;
        while (stop) ;
    }
}
