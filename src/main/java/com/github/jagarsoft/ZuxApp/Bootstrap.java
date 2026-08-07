package com.github.jagarsoft.ZuxApp;

public interface Bootstrap {
    enum COMPUTER_MODEL {
        DEFAULT,
        ZXSPECTRUM,
        ZUX
    };

    void initialize();
    void terminate();
    void withdraw();

    void setImage(String image);
    void setDataRegion(String dataRegion);
    void setComputerModel(COMPUTER_MODEL computerModel);

    boolean isDebugOn();
}
