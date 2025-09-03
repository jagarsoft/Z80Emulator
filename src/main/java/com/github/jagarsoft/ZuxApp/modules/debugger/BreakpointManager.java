package com.github.jagarsoft.ZuxApp.modules.debugger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class BreakpointManager {
    private final Set<Integer> pcs = ConcurrentHashMap.newKeySet();
    public boolean toggle(int pc) {
        if (pcs.contains(pc)) { pcs.remove(pc); return false; }
        pcs.add(pc); return true;
    }
    public boolean isBreakpoint(int pc) { return pcs.contains(pc); }
}
