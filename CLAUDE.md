# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
Index by codebase-memory-mcp must be used always.



## Overview

Z80Emulator is a ZX Spectrum and Zux emulator written in Java with Swing. The project is organized around a modular, event-driven architecture using a CommandBus and EventBus for inter-module communication.

Zux is a porting of Minix 1.x to Z80

**Current Status:** Stable but incomplete. For backup purposes only.

## Build and Development Commands

The project uses Maven with Java 21 (preview features enabled).

**Build:**
```
mvn clean package -Pprod
```

**Build (fast, skip tests):**
```
mvn clean package -Pdev
```

**Run tests (all):**
```
mvn test
```

**Run a single test:**
```
mvn test -Dtest=ClassName
```
Example: `mvn test -Dtest=ArithmeticTest`

**Run the application:**
```
mvn exec:java -Dexec.mainClass="com.github.jagarsoft.ZuxApp.Main" -Dexec.args="-d"
```
Or from packaged JAR:
```
java -jar target/Z80Emulator-1.0-beta.jar [options]
```

**Application options:**
- `-h`: Show help
- `-zx | -zux`: Computer model (ZX Spectrum or Zux)
- `-d`: Debug mode (opens the debugger UI)
- `-r <path>`: Load data region file (.dbm Data Block Map File Descriptor)
- `-i <path>`: Load a ROM image (.bin for raw format or .tap for TAP format)

## Architecture

### Core Principles

- **Modular Design**: Each feature is a separate module that extends `BaseModule`
- **Command Pattern**: Modules send `Command` objects through `CommandBus` to request operations
- **Event-Driven**: Modules respond to `Event` objects published through `EventBus`
- **Loose Coupling**: Modules don't directly depend on each other, only on the bus interfaces
- **Dual Computer Model Support**: The application can emulate either ZX Spectrum or Zux computers

### Key Directory Structure

```
src/main/java/com/github/jagarsoft/ZuxApp/
├── core/                    # Core interfaces and abstractions
│   ├── bus/                 # CommandBus, EventBus, Command, Event interfaces
│   ├── module/              # Module interface
│   └── events/              # System-level events
├── infrastructure/          # Implementations of core interfaces
│   ├── bus/                 # SimpleCommandBus, SimpleEventBus, AsyncEventBus, BroadCastIODeviceEvents
│   └── module/              # BaseModule abstract class
└── modules/                 # Feature modules
    ├── computer/            # Z80 CPU and computer state
    ├── debugger/            # Execution control (step, run, pause, breakpoints)
    ├── disassembler/        # Z80 instruction disassembly view
    ├── memoryviewer/        # Memory dump view
    ├── memoryconfig/        # Configuration for memory layout
    ├── registers/           # CPU registers display
    ├── zxspectrum/          # ZX Spectrum-specific hardware (keyboard, screen, border)
    ├── zux/                 # Zux-specific hardware (Minix 1.x and VT-100 terminal)
    ├── tape/                # TAP file playback and audio
    ├── dataregion/          # Data block mapping and visualization
    ├── ports/               # I/O port read/write tracking
    ├── symboltable/         # Symbol file loading and lookup
    ├── logger/              # Logging output view
    ├── mainmodule/          # Menu bar, file browser, UI layout
    ├── console/             # Developer console
    ├── kernel/              # Process management and system monitoring (for Zux only)
    └── helloworld/          # Example module (reference implementation)

src/test/java/com/github/jagarsoft/test/
├── Z80ForTesting.java       # Z80 CPU instance for unit tests
├── ArithmeticTest.java      # ALU operation tests
├── LoadStoreTest.java       # Memory operations tests
├── LogicTest.java           # Logical operation tests
└── [other operation tests]
```

### Creating a New Module

1. **Create the module class** extending `BaseModule`:
```java
package com.github.jagarsoft.ZuxApp.modules.yourmodule;

public class YourModule extends BaseModule {
    @Override
    public void configure() {
        // Register command handlers
        // commandBus.registerHandler(YourCommand.class, cmd -> { ... });
        
        // Subscribe to events
        // eventBus.subscribe(SomeEvent.class, event -> { ... });
    }

    @Override
    public void initUI() {
        // Create and add Swing components to the UI
        // Called after module is configured
    }
}
```

2. **Register the module** in the Bootstrap class (BootstrapDebug.java or BootstrapImage.java)

3. **Follow existing modules as templates** - examine similar modules like `DisassemblerModule`, `MemoryViewModule`, or `RegistersModule` to understand patterns

### Computer Models

The application supports two distinct computer architectures:

**ZX Spectrum** (`-zx` flag)
- Classic ZX Spectrum emulation with VRAM and border rendering
- Uses ZXSpectrumModule for hardware-specific features

**Zux** (`-zux` flag)
- Minix 1.x porting
- Alternative computer architecture with terminal-based I/O
- Uses ZuxModule for hardware-specific features (IOManagementUnit, TerminalPanel)

The correct module is loaded at bootstrap time based on the selected model.

### Creating a New Module

1. **Create the module class** extending `BaseModule`:
```java
package com.github.jagarsoft.ZuxApp.modules.yourmodule;

public class YourModule extends BaseModule {
    @Override
    public void configure() {
        // Register command handlers
        // commandBus.registerHandler(YourCommand.class, cmd -> { ... });
        
        // Subscribe to events
        // eventBus.subscribe(SomeEvent.class, event -> { ... });
    }

    @Override
    public void initUI() {
        // Create and add Swing components to the UI
        // Called after module is configured
    }
}
```

2. **Register the module** in the Bootstrap class (BootstrapDebug.java or BootstrapImage.java)

3. **Follow existing modules as templates** - examine similar modules like `DisassemblerModule`, `MemoryViewModule`, or `RegistersModule` to understand patterns

### Module Communication

**Sending a Command (request an operation):**
```java
commandBus.execute(new YourCommand(parameters));
```

**Publishing an Event (notify other modules):**
```java
eventBus.publish(new YourEvent(data));
```

**Subscribing to Events (react to changes):**
```java
eventBus.subscribe(SomeEvent.class, event -> {
    // Handle event
});
```

**Registering a Command Handler:**
```java
commandBus.registerHandler(YourCommand.class, command -> {
    // Process command
});
```

## Testing

Tests are located in `src/test/java/com/github/jagarsoft/test/`. The Z80 CPU is tested extensively:
- **Arithmetic operations** (ADD, SUB, INC, DEC, DAA)
- **Logic operations** (AND, OR, XOR)
- **Load/Store operations** (LD, PUSH, POP)
- **Block operations** (LDIR, LDDR, etc.)
- **Rotate/Shift operations** (RLA, RRA, SLA, etc.)
- **Branch operations** (JP, JR, CALL, RET)
- **IO operations** (IN, OUT)
- **Interrupts and stack operations**
- **Bit operations** (BIT, SET, RES)

To test changes to the CPU emulation, run the relevant test file after modifying CPU code. The test suite uses a custom `Z80ForTesting` instance that allows verification against expected opcode behavior.

## Important Notes

- **No direct module dependencies**: Modules communicate only through CommandBus/EventBus
- **Model existing modules**: Before creating new functionality, review how similar features are implemented in existing modules
- **Bootstrap process**: The application initialization is handled by Bootstrap classes - examine `BootstrapDebug.java` and `BootstrapImage.java` to understand module loading order and computer model selection
- **Test-first for CPU changes**: The Z80 emulation is extensively tested; verify CPU instruction correctness with unit tests before integration
- **Computer model awareness**: Code that needs to behave differently for ZX Spectrum vs Zux should be model-aware or placed in the appropriate model-specific module

## Reference and Documentation

- It is centralized at docs\architecture\