package com.github.jagarsoft.ZuxApp.modules.mainmodule;

import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJMenuToMenuBarCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.events.FileSelectedEvent;

import javax.swing.*;
import java.awt.*;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class MainModule extends BaseModule {
    private final JDesktopPane desktopPane;

    private JMenuBar menuBar;
    public MainModule() {
        this.desktopPane = new JDesktopPane();
    }

    public void configure() {
        /*eventBus.subscribeToAll(
                event -> System.out.println("Evento: ventana abierta -> " + event.getWindowTitle()
                );*/

        this.commandBus.registerHandler(AddJInternalFrameToDesktopPaneCommand.class, new CommandHandler<AddJInternalFrameToDesktopPaneCommand>() {
            // Lambda version:
            // (AddJInternalFrameToDesktopPaneCommand)command -> desktopPane.add(command.getFrame())

            @Override
            public void handle(AddJInternalFrameToDesktopPaneCommand command) {
                desktopPane.add(command.getFrame());
            }
        });

        this.commandBus.registerHandler(AddJMenuToMenuBarCommand.class, new CommandHandler<AddJMenuToMenuBarCommand>() {
            @Override
            public void handle(AddJMenuToMenuBarCommand command) {
                menuBar.add(command.getMenu());
            }
        });
    }

    public void initUI() {
        JFrame frame = new JFrame("Zux Application");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());
        frame.add(desktopPane, BorderLayout.CENTER);

        // Barra de menú mínima
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem openFileItem = new JMenuItem("Open...");
        openFileItem.addActionListener(e -> openFileBrowser());

        JMenuItem exitFileItem = new JMenuItem("Exit");
        exitFileItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openFileItem);
        fileMenu.add(exitFileItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    private void openFileBrowser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file");

        fileChooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            eventBus.publish(new FileSelectedEvent(selectedFile));
        }
    }

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
