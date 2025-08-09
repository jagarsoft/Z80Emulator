package com.github.jagarsoft.ZuxApp.ui;

import com.github.jagarsoft.ZuxApp.core.bus.CommandBus;
import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.core.bus.EventBus;
import com.github.jagarsoft.ZuxApp.core.commands.AddJInternalFrameToDesktopPaneCommand;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CommandBus commandBus;
    private final EventBus eventBus;
    private final JDesktopPane desktopPane;

    private JMenuBar menuBar;
    public MainFrame(CommandBus commandBus, EventBus eventBus) {
        super("Zux Application");
        this.commandBus = commandBus;
        this.eventBus = eventBus;
        this.desktopPane = new JDesktopPane();

        // TODO: Move MainFrame into MainModule implements Module
        //register(commandBus, eventBus);
        configure();
        initUI();
    }

    void configure() {
        this.commandBus.registerHandler(AddJInternalFrameToDesktopPaneCommand.class, new CommandHandler<AddJInternalFrameToDesktopPaneCommand>() {
            // Lambda version:
            // (AddJInternalFrameToDesktopPaneCommand)command -> desktopPane.add(command.getFrame())

            @Override
            public void handle(AddJInternalFrameToDesktopPaneCommand command) {
                desktopPane.add(command.getFrame());
            }
        });
    }

    private void initUI() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(desktopPane, BorderLayout.CENTER);

        // Barra de menú mínima
        this.menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem testItem = new JMenuItem("Open Test Window");
        testItem.addActionListener(e -> openTestWindow());
        //testItem.setEnabled(false);
        fileMenu.add(testItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /*
    @Override
    public void register(CommandBus commandBus, EventBus eventBus) {
        eventBus.subscribe(
                null,
                event -> System.out.println("Evento: ventana abierta -> " + event.getWindowTitle()
        );
    }
    */

    private void openTestWindow() {
        JInternalFrame internalFrame = new JInternalFrame("Test Window", true, true, true, true);
        internalFrame.setSize(300, 200);
        internalFrame.setVisible(true);
        desktopPane.add(internalFrame);
        try {
            internalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {}
    }


}
