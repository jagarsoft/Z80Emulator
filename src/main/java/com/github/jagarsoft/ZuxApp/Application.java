package com.github.jagarsoft.ZuxApp;

public class Application {

    private final Bootstrap bootstrap;

    public Application() {
        this.bootstrap = new Bootstrap();
    }

    public void start() {
        bootstrap.initialize();
        bootstrap.launchMainWindow();
    }

    // TODO: No hay forma de llamar aqui porque la ref a app es local ;-(
    public void shutdown() {
        bootstrap.terminate();
        bootstrap.withdraw();
    }
}