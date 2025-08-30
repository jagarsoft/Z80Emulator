package com.github.jagarsoft.ZuxApp.core.bus;

public interface UIEventHandler<T> extends EventHandler<T> {
    // Used as marker, as a substitute for
    // @com.github.jagarsoft.ZuxApp.core.annotations.RunOnUIThread
}
