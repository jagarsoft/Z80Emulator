package com.github.jagarsoft.ZuxApp;

public interface Bootstrap {
    void initialize();
    void terminate();
    void withdraw();

    void setImage(String image);
    void setDataRegion(String dataRegion);
}
