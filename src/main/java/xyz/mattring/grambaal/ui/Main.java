package xyz.mattring.grambaal.ui;

import io.undertow.Undertow;

public class Main implements Runnable {

    private final int port;
    private final App app;

    public Main(int port, String basePath) {
        this.port = port;
        this.app = new App(
                "none".equalsIgnoreCase(basePath) ? "" : basePath);
    }

    boolean isProvisioned() {
        return app.isProvisioned();
    }

    @Override
    public void run() {
        Undertow server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(app.getCompositeHandler())
                .build();
        server.start();
    }

    public static void main(String[] args) {
        final int port = Integer.parseInt(args[0]);
        final String basePath = args[1];
        final Main main = new Main(port, basePath);
        if (!main.isProvisioned()) {
            System.err.println("App/UsrMgt not ready. Exiting.");
            System.exit(1);
        }
        main.run();
    }
}