package com.github.jagarsoft.ZuxApp.modules.memoryconfig.events;

import com.github.jagarsoft.ZuxApp.core.bus.Event;

public class MemoryConfigChangedEvent implements Event {
    private int pageSize;
    private long numberPages;
    private boolean execOnLoad;

    public MemoryConfigChangedEvent(int pageSize, long numberPages, boolean execOnLoad) {
        this.setPageSize(pageSize);
        this.setNumberPages(numberPages);
        this.execOnLoad = execOnLoad;
    }

    @Override
    public String getEventName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return getPageSize() + " * " + getNumberPages();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getNumberPages() {
        return numberPages;
    }

    public void setNumberPages(long numberPages) {
        this.numberPages = numberPages;
    }

    public boolean isExecOnLoad() { return  execOnLoad; }
}
