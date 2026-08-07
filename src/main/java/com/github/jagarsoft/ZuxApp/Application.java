package com.github.jagarsoft.ZuxApp;

public class Application {

    private final Bootstrap bootstrap;

    /**
     * A new Application have been created.
     * Make a suitable Bootstrap according to args in command line
     * <p>Namely:<ul>
     *      <li>BootstrapImage() for normal mode</li>
     *      <li>BootstrapDebug() for debug mode</li></ul>
     * <p>
     * Bootstrap also receives Image, DataRegion and ComputerModel
     *
     * @param args
     */
    public Application(String[] args) {
        Bootstrap bootstrapTMP = null;
        int i = 0;
        String image = null;
        String dataRegion = null;
        boolean debugOn = false;
        boolean dataRegionOn = false;
        Bootstrap.COMPUTER_MODEL computerModel = Bootstrap.COMPUTER_MODEL.DEFAULT;

        if (args.length == 0 || args[i].equals("-h")) {
            usage();
            System.exit(0);
        }

        if (i < args.length && args[i].equals("-zx")) {
            computerModel = Bootstrap.COMPUTER_MODEL.ZXSPECTRUM;
            i++;
        }

        if (i < args.length && args[i].equals("-zux") ) {
            if( computerModel != Bootstrap.COMPUTER_MODEL.ZXSPECTRUM ) {
                computerModel = Bootstrap.COMPUTER_MODEL.ZUX;
                i++;
            } else {
                System.out.printf("Only a computer model must be specified.\n");
                System.out.printf("Use -zx for ZX-Spectrum or -zux for Zux\n");
                System.exit(-1);
            }
        }

        if( computerModel ==  Bootstrap.COMPUTER_MODEL.DEFAULT ) {
            System.out.printf("Computer model not specified.\n");
            System.out.printf("Use -zx for ZX-Spectrum or -zux for Zux\n");
            System.exit(-2);
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
                System.out.println("Missing -i option");
                usage();
                System.exit(1);
            }
        }

        bootstrap = bootstrapTMP;
        bootstrap.setImage(image);
        bootstrap.setDataRegion(dataRegion);
        bootstrap.setComputerModel(computerModel);
    }

    // Main calls Application.start()
    public void start() {
        bootstrap.initialize();
    }

    // TODO: No hay forma de llamar aqui porque la ref a app es local ;-(
    public void shutdown() {
        bootstrap.terminate();
        bootstrap.withdraw();
    }

    private void usage() {
        System.out.println("Z80 Emulation Workbench");
        System.out.println("Usage: options, if present, must appears in this order");
        System.out.println("\t-h: Show help and exit");
        System.out.println("\t-zx | -zux: Computer model");
        System.out.println("\t[-d]: Debug mode (opens the debugger UI)");
        System.out.println("\t[-r <path>]: Load data region file (.dbm Data Block Map File Descriptor)");
        System.out.println("\t-i <path>: Load a ROM or image (.bin for raw format or .tap for TAP format)");
    }
}