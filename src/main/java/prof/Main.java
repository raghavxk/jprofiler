package prof;

public class Main {
    public static void agentMain(String args) {
        preMain(args);
    }

    public static void preMain(String args) {
        Main main = new Main();
        main.run(new Options(args));
    }

    private void run(Options options) {
        Thread t = new Thread(new Profiler(options));
        t.setDaemon(true);
        t.setName("profiler");
        t.start();
    }
}
