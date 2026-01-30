package com.github.jagarsoft.ZuxApp.modules.mainmodule;

import com.github.jagarsoft.ZuxApp.core.bus.CommandHandler;
import com.github.jagarsoft.ZuxApp.infrastructure.FIleBrowser;
import com.github.jagarsoft.ZuxApp.infrastructure.MenuUtils;
import com.github.jagarsoft.ZuxApp.modules.computer.commands.LoadRawCodeAndRunCommand;
import com.github.jagarsoft.ZuxApp.modules.debugger.commands.DebuggerPauseCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJMenuItemToMenuCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJMenuToMenuBarCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.GetFileSelectedCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.events.FileBinaryImageSelectedEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.JFrame.EXIT_ON_CLOSE;

public class MainModule extends BaseModule {
    private final JDesktopPane desktopPane;
    private JMenuBar menuBar;
    private File selectedFile;

    private final Map<String, JMenu> menuEntries =  new HashMap<>();

    public MainModule() {
        this.desktopPane = new JDesktopPane();
    }

    public void configure() {
        /*eventBus.subscribeToAll(
                event -> System.out.println("Evento: ventana abierta -> " + event.getWindowTitle()
                );*/

        commandBus.registerHandler(AddJInternalFrameToDesktopPaneCommand.class, new CommandHandler<AddJInternalFrameToDesktopPaneCommand>() {
            // Lambda version:
            // (AddJInternalFrameToDesktopPaneCommand)command -> desktopPane.add(command.getFrame())

            @Override
            public void handle(AddJInternalFrameToDesktopPaneCommand command) {
                desktopPane.add(command.getFrame());
            }
        });

        commandBus.registerHandler(AddJMenuToMenuBarCommand.class, new CommandHandler<AddJMenuToMenuBarCommand>() {
            @Override
            public void handle(AddJMenuToMenuBarCommand command) {
                menuBar.add(command.getMenu());
                menuEntries.put(command.getMenu().getText(), command.getMenu());
            }
        });

        commandBus.registerHandler(AddJMenuItemToMenuCommand.class, new CommandHandler<AddJMenuItemToMenuCommand>() {
            @Override
            public void handle(AddJMenuItemToMenuCommand command) {
                JMenu menu = menuEntries.get(command.getMenuName());
                MenuUtils.insertAfter(menu, command.getMenuItemName(), command.getJMenuItem());
                //menu.add(command.getJMenuItem());
            }
        });

        commandBus.registerHandler(GetFileSelectedCommand.class, new CommandHandler<GetFileSelectedCommand>() {
            @Override
            public void handle(GetFileSelectedCommand command) {
                command.file = selectedFile;
            }
        });
    }

    public void initUI() {
        JFrame frame = new JFrame("Z80 Emulation Workbench");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(800, 700);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());
        frame.add(desktopPane, BorderLayout.CENTER);

        // Barra de menú mínima
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        menuEntries.put("File", fileMenu);

        JMenuItem openFileItem = new JMenuItem("Load binary image...");
        openFileItem.addActionListener(new FIleBrowser(eventBus, new FileBinaryImageSelectedEvent()) );

        //JMenuItem openFileCode = new JMenuItem("Load raw code..."); // TODO pending remove
        //openFileCode.addActionListener(rawCodeInit());

        JMenuItem exitFileItem = new JMenuItem("Exit");
        exitFileItem.addActionListener(e -> System.exit(0)); // TODO Raise event instead

        fileMenu.add(openFileItem);
        //fileMenu.add(openFileCode);
        fileMenu.addSeparator();
        fileMenu.add(exitFileItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    /*private void openTestWindow() {
        JInternalFrame internalFrame = new JInternalFrame("Test Window", true, true, true, true);
        internalFrame.setSize(300, 200);
        internalFrame.setVisible(true);
        desktopPane.add(internalFrame);
        try {
            internalFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {}
    }*/

    /*private ActionListener rawCodeInit() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Load Raw Code");
                frame.setSize(400, 80);
                frame.setLayout(new BorderLayout());
                JPanel rawCodePanel = new JPanel(new GridLayout(2, 2));
                JLabel rawCodeLabel = new JLabel("Load and run code on:");
                JTextField rawCodeInit = new JTextField(10);
                //rawCodeInit.setEditable(true);
                JLabel rawBinLabel = new JLabel("Binary Image:");
                JTextField rawCodeBin = new JTextField(40);
                rawCodeBin.setEditable(true);
                //rawCodeBin.setSize(200, 20);
                JButton rawCodeButton = new JButton("Load & Run");
                rawCodeButton.addActionListener(c ->
                        commandBus.execute(new LoadRawCodeAndRunCommand(rawCodeInit.getText(), rawCodeBin.getText())));
                        //eventBus.publish(new LoadRawCodeAndRunEvent(rawCodeInit.getText(), rawCodeBin.getText())));
                rawCodePanel.add(rawCodeLabel);
                rawCodePanel.add(rawCodeInit);
                rawCodePanel.add(rawBinLabel);
                rawCodePanel.add(rawCodeBin);
                rawCodePanel.add(rawCodeButton);
                frame.add(rawCodePanel, BorderLayout.NORTH);
                frame.setVisible(true);
            }
        };
    }*/
}
