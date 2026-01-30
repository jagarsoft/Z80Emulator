package com.github.jagarsoft.ZuxApp;

public class Application {

    private final Bootstrap bootstrap;

    public Application(String[] args) {
        Bootstrap bootstrapTMP = null;
        int i = 0;
        String image = null;
        String dataRegion = null;
        boolean debugOn = false;
        boolean dataRegionOn = false;

        if (args.length == 0 || args[i].equals("-h")) {
            usage();
            System.exit(0);
        }

        if (i < args.length && args[i].equals("-d")) {
            bootstrapTMP = new BootstrapDebug();
            i++;
            debugOn = true;
        }

        if (i < args.length && args[i].equals("-r")) {
            dataRegion = args[++i];
            i++;
            dataRegionOn = true;
        }

        if (i < args.length && args[i].equals("-i")) {
            image = args[++i];
            if( !debugOn )
                bootstrapTMP = new BootstrapImage();
        } else {
            if( !debugOn ) {
                usage();
                System.exit(1);
            }
        }

        bootstrap = bootstrapTMP;
        bootstrap.setImage(image);
        bootstrap.setDataRegion(dataRegion);
    }

    public void start() {
        bootstrap.initialize();
    }

    // TODO: No hay forma de llamar aqui porque la ref a app es local ;-(
    public void shutdown() {
        bootstrap.terminate();
        bootstrap.withdraw();
    }

    private void usage() {

    }
}