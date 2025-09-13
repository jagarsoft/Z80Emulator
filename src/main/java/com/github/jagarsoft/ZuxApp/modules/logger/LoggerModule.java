package com.github.jagarsoft.ZuxApp.modules.logger;

import com.github.jagarsoft.ZuxApp.core.bus.Event;
import com.github.jagarsoft.ZuxApp.core.bus.EventHandler;
import com.github.jagarsoft.ZuxApp.core.bus.UIEventHandler;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.core.events.LongTaskEvent;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;

import javax.swing.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LoggerModule extends BaseModule {

    private JTextArea logArea = new JTextArea(10, 40);

    private void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    @Override
    public void configure() {
        // Suscribirse al evento pesado
        /*eventBus.subscribe(LongTaskEvent.class, new EventHandler<LongTaskEvent>() {
            @Override
            public void handle(LongTaskEvent e) {
                System.out.println("Handler thread: " + Thread.currentThread().getName());

                appendLog("Tarea recibida: " + e.getMessage());
                try {
                    // Simular procesamiento costoso
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                appendLog("Tarea completada: " + e.getMessage());
            }
        });*/

        /*
        eventBus.subscribe(LogEvent.class, new EventHandler<LogEvent>() {
            @Override
            @RunOnUIThread
            public void handle(LogEvent event) {
                logArea.append("[LOG] " + event.getMessage() + "\n");
            }
        });
        */

        /*
        eventBus.subscribeToAll(new EventHandler<Object>() {
            @Override
            @RunOnUIThread
            public void handle(Object event) {
                logArea.append("[LOG] " + event.getMessage() + "\n");
            }
        });*/

        eventBus.subscribeToAll( new Consumer<Event>() {
            @Override
            public void accept(Event e) {
                System.out.print("[LOG] Consumer " + e.getEventName() + ": " + e.getMessage()  + "\n");
                logArea.append("[LOG] Consumer " + e.getEventName() + ": " + e.getMessage() + "\n");
            }
        });

        /*eventBus.subscribeToAll( new UIEventHandler<Event>() {
            @Override
            public void handle(Event e) {
                logArea.append("[LOG] UIEventHandler "+e.getEventName()+": "+e.getMessage()+"\n");
                System.out.print("[LOG] UIEventHandler "+e.getEventName()+": "+e.getMessage()+"\n");
            }
        });*/
    }

    @Override
    public void initUI() {
        logArea.setEditable(false);

        JInternalFrame frame = new JInternalFrame("Logger", true, true, true, true);
        frame.setSize(400, 300);
        frame.add(new JScrollPane(logArea));

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
