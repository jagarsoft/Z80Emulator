package com.github.jagarsoft.ZuxApp.modules.ports;

import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.ports.events.PortWriteEvent;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class PortsViewModule extends BaseModule {
    PortsTableModel portsModel;
    private final JTextField inputField = new JTextField();

    @Override
    public void configure() {
        eventBus.subscribe(PortWriteEvent.class, (Consumer<PortWriteEvent>) e -> {
            portsModel.addValue(e.getAddr(),  e.getData());
        });
    }

    @Override
    public void initUI() {
        portsModel = new PortsTableModel();
        JTable portsTable = new JTable(portsModel);

        JInternalFrame frame = new JInternalFrame("Ports", true, true, true, true);
        frame.setSize(300, 250);
        frame.add(new JScrollPane(portsTable));

        frame.setLayout(new BorderLayout());

        // Configurar input arriba
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Add port to listen on: "), BorderLayout.WEST);
        topPanel.add(inputField, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);

        // Configurar tabla abajo
        portsTable.setFillsViewportHeight(true);
        portsTable.setRowHeight(22);
        frame.add(new JScrollPane(portsTable), BorderLayout.CENTER);

        // Manejar ENTER para añadir puerto
        inputField.addActionListener(e -> {
            String text = inputField.getText().trim();
            try {
                int port = Integer.decode(text); // admite 0xNN o decimal
                portsModel.addPort(port);
                inputField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid port: " + text);
            }
        });

        frame.setSize(600, 300);

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
