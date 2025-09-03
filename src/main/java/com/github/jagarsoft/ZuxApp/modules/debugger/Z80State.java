package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.Z80;

import java.util.BitSet;

public class Z80State {
    private BitSet z80State;

    public Z80State(BitSet newState) {
        this.z80State = newState;
    }

    public boolean isTouched(Z80.RegTouched r) {
        return z80State.get(r.ordinal());
    }

    public boolean hasChanged(Z80State oldState) {
        return ! z80State.equals(oldState.z80State);
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < z80State.length(); i++) {
            s.append(z80State.get(i) == true ? 1 : 0);
        }
        return s.toString();
    }
}
