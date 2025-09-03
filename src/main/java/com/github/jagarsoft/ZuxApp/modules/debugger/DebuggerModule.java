package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.computer.Z80Cpu;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.ComputerLoadImageCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCPUCommand;
import com.github.jagarsoft.ZuxApp.modules.debugger.commands.*;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.*;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.events.FileSelectedEvent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

//
// TODO asegurarse de que se sigue el patron MVC:
// las acciones sobre la VISTA cambian el MODELO
// un cambio de estado del MODELO se refleja en la VISTA
// No debe haber recursion mutua
//

public class DebuggerModule extends BaseModule {

    private Z80Cpu cpu;
    private final BreakpointManager breakpoints = new BreakpointManager();

    private final ExecutorService runExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean running = new AtomicBoolean(false);
    //private NotificationStrategy notificationStrategy = new NoEventsStrategy();
    //private NotificationStrategy notificationStrategy = new StepStrategy();
    private NotificationStrategy notificationStrategy = new SnapshotStrategy();

    // Coalescing de UI: un “tick” cada ~33ms (30 FPS)
    private volatile long lastUiPushMs = 0L;
    private int uiPushIntervalMs = 33;

    public enum DebuggerState {
        RUNNING,   // real, no events
        PAUSED,    // snapshot
        STEPPING   // immediate updates
    }

    DebuggerState state = DebuggerState.PAUSED;

    private JButton runButton;
    private JButton pauseButton;
    private JButton stepButton;

    public DebuggerModule() {
        this.cpu = null; //new Z80Cpu();
    }

    public DebuggerModule(IZ80Cpu cpu) {
        this.cpu = (Z80Cpu) cpu;
    }

    @Override
    public String getName() { return "Debugger"; }

    @Override
    public void configure() {
        /*
         * Debugger Controller
         */

        // STEP
        commandBus.registerHandler(DebuggerStepCommand.class, new CommandHandler<DebuggerStepCommand>() {
            @Override public void handle(DebuggerStepCommand cmd) {
                if (running.get()) return; // ignore if already running
                doOneStepAndPublish();
            }
        });

        // RUN
        commandBus.registerHandler(DebuggerRunCommand.class, new CommandHandler<DebuggerRunCommand>() {
            @Override public void handle(DebuggerRunCommand cmd) {
                if (!running.compareAndSet(false, true)) {
                    System.out.println("[DEBUGGER] Already running.");
                    return;
                }
                runExecutor.submit(new Runnable() {
                    @Override public void run() { runLoop(); }
                });
            }
        });

        // PAUSE
        commandBus.registerHandler(DebuggerPauseCommand.class, new CommandHandler<DebuggerPauseCommand>() {
            @Override public void handle(DebuggerPauseCommand cmd) {
                if (running.compareAndSet(true, false)) {
                    pauseDebugger("PAUSE");
                }
            }
        });

        // RESET
        commandBus.registerHandler(DebuggerResetCommand.class, new CommandHandler<DebuggerResetCommand>() {
            @Override public void handle(DebuggerResetCommand cmd) {
                resetDebugger();
            }
        });

        // TOGGLE BREAKPOINT
        commandBus.registerHandler(DebuggerToggleBreakpointCommand.class, new CommandHandler<DebuggerToggleBreakpointCommand>() {
            @Override public void handle(DebuggerToggleBreakpointCommand cmd) {
                boolean enabled = breakpoints.toggle(cmd.getAddress());
                // TODO (Opcional) publicar un evento de “breakpoints actualizados”
                // eventBus.publish(new BreakPointToggledEvent(breakpoints));
            }
        });

        //eventBus.subscribe(ExecutionStartedEvent.class, (Consumer<ExecutionStartedEvent>) (e) -> {
        eventBus.subscribe(ExecutionStartedEvent.class, (UIEventHandler<ExecutionStartedEvent>) (e) -> {
            runButton.setEnabled(false);
            pauseButton.setEnabled(true);
            stepButton.setEnabled(false);
        });
        //eventBus.subscribe(ExecutionPausedEvent.class, (Consumer<ExecutionPausedEvent>) (e) -> {
        eventBus.subscribe(ExecutionPausedEvent.class, (UIEventHandler<ExecutionPausedEvent>) (e) -> {
            runButton.setEnabled(true);
            pauseButton.setEnabled(false);
            stepButton.setEnabled(true);
        });
        /*eventBus.subscribe(BreakpointHitEvent.class, (Consumer<BreakpointHitEvent>) (e) -> {
            eventBus.publish(new ExecutionPausedEvent("BREAKPOINT"));
        });*/

        GetComputerCPUCommand currentCPU = new GetComputerCPUCommand();
        commandBus.execute(currentCPU);
        this.cpu = new Z80Cpu(currentCPU.getCpu());

        eventBus.subscribe(FileSelectedEvent.class, (Consumer<FileSelectedEvent>) (e) -> {
            File currentFile = e.getSelectedFile();
            commandBus.execute(new ComputerLoadImageCommand(currentFile));
            // eventBus.publish(new ImageLoadedEvent());
            //commandBus.execute(new DebuggerRunCommand()); // RUN ??
            commandBus.execute(new DebuggerPauseCommand()); // PAUSE ??
        });

        /*
        eventBus.subscribe(ImageLoadedEvent.class, (Consumer<ImageLoadedEvent>) (e) -> {

        });*/
    }

    @Override
    public void initUI() {
        JInternalFrame frame = new JInternalFrame("Debugger", true, true, true, true);
        frame.setSize(400, 80);
        frame.setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        runButton = new JButton("Run");
        runButton.addActionListener(
                e -> commandBus.execute(new DebuggerRunCommand())
        );

        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(
                e -> commandBus.execute(new DebuggerPauseCommand())
        );

        stepButton = new JButton("Step");
        stepButton.addActionListener(
                e -> commandBus.execute(new DebuggerStepCommand())
        );

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(
                e -> commandBus.execute(new DebuggerResetCommand())
        );

        runButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stepButton.setEnabled(true);

        toolBar.add(runButton);
        toolBar.add(pauseButton);
        toolBar.add(stepButton);
        toolBar.add(resetButton);

        frame.add(toolBar, BorderLayout.NORTH);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }

    public void setNotificationStrategy(NotificationStrategy strategy) {
        notificationStrategy = strategy;
    }

    /*
     * Debugger Model
     */

    private void doOneStepAndPublish() {
        stepDebugger();
        int pc = cpu.getPC();
        if (! isBreakpointHit(pc)) {
            cpu.step();
            notificationStrategy.onInstruction(cpu, eventBus);
            if (cpu.isHalted()) {
                //running.set(false);
                pauseDebugger("HALTED");
                setNotificationStrategy( new StepStrategy() );
                //notificationStrategy.onPause(cpu, eventBus);
            }
        }
    }

    private void stepDebugger() {
        if( state != DebuggerState.STEPPING ) {
            state = DebuggerState.STEPPING;
            setNotificationStrategy( new StepStrategy() );
        }
    }

    private boolean isBreakpointHit(int pc) {
        if (breakpoints.isBreakpoint(pc)) {
            //running.set(false);
            pauseDebugger("PAUSE");
            eventBus.publish(new BreakpointHitEvent(pc, cpu.snapshot()));
            return true;
        } else
            return false;
    }

    /*
    private void runLoop() {
        try {
            while (running.get()) {
                if (cpu.isHalted()) {
                    running.set(false);
                    eventBus.publish(new ExecutionStoppedEvent());
                    break;
                }

                doOneStepAndPublish(true);

                int pc = cpu.getPC();
                if (breakpoints.isBreakpoint(pc)) {
                    running.set(false);
                    eventBus.publish(new BreakpointHitEvent(pc, cpu.snapshot()));
                    break;
                }

                // Coalescing de UI (30 Hz aprox)
                long now = System.currentTimeMillis();
                if (now - lastUiPushMs >= uiPushIntervalMs) {
                    lastUiPushMs = now;
                    eventBus.publish(new CpuStateUpdatedEvent(cpu.snapshot()));
                }
            }
        } finally {
            if (!running.get()) {
                eventBus.publish(new ExecutionStoppedEvent());
            }
        }
    }

    private void doOneStepAndPublish(boolean emitSteppedEvent) {
        cpu.step();
        Z80State snap = cpu.snapshot();
        if (emitSteppedEvent) {
            eventBus.publish(new CpuSteppedEvent(snap)); // útil para “paso a paso”
        } else {
            // En run no publicamos cada instrucción; el coalescing arriba enviará CpuStateUpdatedEvent
        }
    }
    */

    /*
    private void runLoop() {
        while (running.get() && !cpu.isHalted()) {
            doOneStepAndPublish();
        }
        notificationStrategy.onPause(cpu, eventBus);
    }
    */

    private void runLoop() {
        runDebugger();
        while (running.get() && !cpu.isHalted()) {
            int pc = cpu.getPC();
            if (! isBreakpointHit(pc)) {
                cpu.step();
                //notificationStrategy.onInstruction(cpu, eventBus);
            }
        }
        if (cpu.isHalted()) {
            pauseDebugger("HALTED");
        }
        notificationStrategy.onPause(cpu, eventBus);
    }

    private void runDebugger() {
        running.set(true);
        state = DebuggerState.RUNNING;
        setNotificationStrategy( new NoEventsStrategy() );
        eventBus.publish(new ExecutionStartedEvent("RUN"));
    }

    private void pauseDebugger(String mode) {
        running.set(false);
        if( state != DebuggerState.PAUSED ) {
            state = DebuggerState.PAUSED;
            setNotificationStrategy( new SnapshotStrategy() );
        }

        // TODO si esta pausado tras un HALT debe de cambiar el estado de los botones
        // FALLA: llegar a HALT y dar RUN
        eventBus.publish(new ExecutionPausedEvent(mode)); // PAUSE, HALTED, RESET
    }

    private void resetDebugger() {
        cpu.reset();
        pauseDebugger("RESET");
    }

    public void shutdown() {
        running.set(false);
        runExecutor.shutdownNow();
    }
}
