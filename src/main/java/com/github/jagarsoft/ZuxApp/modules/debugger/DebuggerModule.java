package com.github.jagarsoft.ZuxApp.modules.debugger;

import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.computer.Z80Cpu;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.ComputerLoadImageCommand;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.GetComputerCommand;
import com.github.jagarsoft.ZuxApp.modules.debugger.commands.*;
import com.github.jagarsoft.ZuxApp.modules.debugger.events.*;
import com.github.jagarsoft.ZuxApp.modules.disassembler.events.BreakpointToggledEvent;
import com.github.jagarsoft.ZuxApp.modules.logger.events.LogEvent;
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
    private NotificationStrategy notificationStrategy = new StepStrategy();
    //private NotificationStrategy notificationStrategy = new SnapshotStrategy();

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

        // STEP
        commandBus.registerHandler(DebuggerStepCommand.class, new CommandHandler<DebuggerStepCommand>() {
            @Override public void handle(DebuggerStepCommand cmd) {
                if (running.get()) return; // ignore if already running
                doOneStepAndPublish();
            }
        });

        // RESET
        commandBus.registerHandler(DebuggerResetCommand.class, new CommandHandler<DebuggerResetCommand>() {
            @Override public void handle(DebuggerResetCommand cmd) {
                resetDebugger();
            }
        });

        // TOGGLE BREAKPOINT
        /*commandBus.registerHandler(DebuggerToggleBreakpointCommand.class, new CommandHandler<DebuggerToggleBreakpointCommand>() {
            @Override public void handle(DebuggerToggleBreakpointCommand cmd) {
                boolean enabled = breakpoints.toggle(cmd.getAddress());
                // TODO (Opcional) publicar un evento de “breakpoints actualizados”
                // eventBus.publish(new BreakPointToggledEvent(breakpoints));
            }
        });*/

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
        eventBus.subscribe(BreakpointToggledEvent.class, (Consumer<BreakpointToggledEvent>) (e) -> {
            breakpoints.toggle(e.getAddress());
        });

        GetComputerCommand currentComputer = new GetComputerCommand();
        commandBus.execute(currentComputer);
        this.cpu = new Z80Cpu(currentComputer.getCpu());

        // TODO UIEventHandler probably
        eventBus.subscribe(FileSelectedEvent.class, (Consumer<FileSelectedEvent>) (e) -> {
            File currentFile = e.getSelectedFile();
            commandBus.execute(new ComputerLoadImageCommand(currentFile));
            // TODO delegar en ComputerLoadImageCommand si la carga tiene exito
            eventBus.publish(new ImageLoadedEvent(currentComputer.getComputer(), currentFile.length()));
        });

        eventBus.subscribe(ImageLoadedEvent.class, (Consumer<ImageLoadedEvent>) (e) -> {
            //commandBus.execute(new DebuggerRunCommand()); // RUN ??
            commandBus.execute(new DebuggerPauseCommand()); // PAUSE ??
        });
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
        eventBus.publish( new LogEvent("setNotificationStrategy => " + notificationStrategy.getClass()));
    }

    /*
     * Debugger Model
     */

    private void doOneStepAndPublish() {
        stepDebugger();
        int pc = cpu.getPC();
        //if (! isBreakpointHit(pc)) {
            cpu.step();
            notificationStrategy.onInstruction(cpu, eventBus);
            if (cpu.isHalted()) {
                //running.set(false);
                pauseDebugger("HALTED");
                //setNotificationStrategy( new SnapshotStrategy() );
                notificationStrategy.onPause(cpu, eventBus);
            }
        //}
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
            pauseDebugger("BREAKPOINT");
            eventBus.publish(new BreakpointHitEvent(pc, cpu.snapshot()));
            return true;
        } else
            return false;
    }

    private void runLoop() {
        runDebugger();
        while (running.get() && !cpu.isHalted()) {
            int pc = cpu.getPC();
            cpu.step();
            if (isBreakpointHit(pc)) {
                notificationStrategy.onInstruction(cpu, eventBus);
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
            //setNotificationStrategy( new StepStrategy() );
            setNotificationStrategy( new SnapshotStrategy() );
        }

        eventBus.publish(new ExecutionPausedEvent(mode)); // PAUSE, HALTED, RESET
    }

    private void resetDebugger() {
        cpu.reset();
        pauseDebugger("RESET");
        notificationStrategy.onPause(cpu, eventBus);
    }

    public void shutdown() {
        running.set(false);
        runExecutor.shutdownNow();
    }
}
