package com.github.jagarsoft.ZuxApp.modules.helloworld;

import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.core.events.LongTaskEvent;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;

import javax.swing.*;

public class HelloWorldModule extends BaseModule {

    @Override
    public void configure() {

    }

    @Override
    public void initUI() {
        // Botón para disparar un evento "pesado"
        JButton button = new JButton("Ejecutar tarea pesada");
        button.addActionListener(e -> {
            eventBus.publish(new LongTaskEvent("Iniciando tarea larga..."));
        });

        JInternalFrame frame = new JInternalFrame("Hello World", true, true, true, true);
        frame.setSize(300, 200);
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(button);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
