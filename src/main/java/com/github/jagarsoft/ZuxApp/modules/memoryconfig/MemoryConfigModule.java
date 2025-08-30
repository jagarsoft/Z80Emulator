package com.github.jagarsoft.ZuxApp.modules.memoryconfig;

import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJMenuToMenuBarCommand;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigAcceptedEvent;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigCancelledEvent;
import com.github.jagarsoft.ZuxApp.modules.memoryconfig.events.MemoryConfigChangedEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.function.Consumer;

import static java.lang.Math.pow;
import static java.lang.Math.round;

public class MemoryConfigModule extends BaseModule implements ChangeListener {
    private JRadioButton rb1k;
    private JRadioButton rb8k;
    private JRadioButton rb32k;
    private JRadioButton rb64k;
    private JSpinner spinner;
    private JInternalFrame frame;
    //private long memorySize = 0;
    private int pageSize = 64;
    private long numberPages;

    public MemoryConfigModule(int pageSize, long numberPages) {
        // TODO check values. Use CONSTANT for min/max values
        this.pageSize = pageSize;
        this.numberPages = numberPages;
    }

    @Override
    public String getName() {
        return "MemoryConfig";
    }

    @Override
    public void configure() {
        eventBus.subscribe(MemoryConfigAcceptedEvent.class,
                (Consumer<MemoryConfigAcceptedEvent>) e -> {
                    //memorySize = pageSize * round((Double) spinner.getValue());
                    //System.out.println(memorySize);
                    eventBus.publish(new MemoryConfigChangedEvent(pageSize, round((Double) spinner.getValue())));
                    frame.setVisible(false);
                });
        eventBus.subscribe(MemoryConfigCancelledEvent.class,
                (Consumer<MemoryConfigCancelledEvent>) e -> {
                    frame.setVisible(false);
                });
    }

    @Override
    public void initUI() {
        frame = new JInternalFrame("Memory Configuration", false, true, false, true);
        frame.setDefaultCloseOperation(JInternalFrame.HIDE_ON_CLOSE);
        frame.setSize(300, 250);
        frame.setLayout(new BorderLayout());

        frame.add(buildMainPanel(), BorderLayout.CENTER);

        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem memConfItem = new JMenuItem("Memory Configuration...");
        memConfItem.addActionListener(e -> {
            frame.setVisible(true);
            try {
                if (frame.isIcon()) {
                    frame.setIcon(false); // restore it
                }
                frame.setSelected(true); // bring to the front
            } catch (java.beans.PropertyVetoException ignored) {}
        });
        toolsMenu.add(memConfItem);

        this.commandBus.execute(new AddJMenuToMenuBarCommand(toolsMenu));

        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Label + radio buttons
        JLabel pageSizeLabel = new JLabel("Page size:");
        panel.add(pageSizeLabel);

        rb1k = new JRadioButton("1K");
        rb8k = new JRadioButton("8K");
        rb32k = new JRadioButton("32K");
        rb64k = new JRadioButton("64K");
        rb1k.addChangeListener(this);
        rb8k.addChangeListener(this);
        rb32k.addChangeListener(this);
        rb64k.addChangeListener(this);
        setDefaultPageSize();

        ButtonGroup group = new ButtonGroup();
        group.add(rb1k);
        group.add(rb8k);
        group.add(rb32k);
        group.add(rb64k);

        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(rb1k);
        radioPanel.add(rb8k);
        radioPanel.add(rb32k);
        radioPanel.add(rb64k);

        panel.add(radioPanel);

        // Label + spinner
        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spinnerPanel.add(new JLabel("Number of pages:"));
        spinner = new JSpinner(new SpinnerNumberModel(numberPages, 1, pow(2, 16), 1));
        spinner.addChangeListener(this);
        spinnerPanel.add(spinner);
        spinnerPanel.add(new JLabel("(Max. 2^16)"));

        panel.add(spinnerPanel);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 20));
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> eventBus.publish(new MemoryConfigAcceptedEvent()));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> eventBus.publish(new MemoryConfigCancelledEvent()));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);

        /*pageSizeLabel.setBorder(BorderFactory.createLineBorder(Color.RED));
        radioPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        */
        return panel;
    }

    private void setDefaultPageSize() {
        switch(pageSize){
            case 1:
                rb1k.setSelected(true);
                break;
            case 8:
                rb8k.setSelected(true);
                break;
            case 32:
                rb32k.setSelected(true);
                break;
            case 64:
                rb64k.setSelected(true);
                break;
        }

    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        String source = changeEvent.getSource().getClass().getSimpleName();
        /*System.out.print(source);
        System.out.print(": ");
        System.out.println(changeEvent.getClass());*/

        switch(source) {
            case "JSpinner":
                try {
                    spinner.commitEdit();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
                break;
            case "JRadioButton":
                if (rb1k.isSelected()) {
                    pageSize = 1;
                }
                if (rb8k.isSelected()) {
                    pageSize = 8;
                }
                if (rb32k.isSelected()) {
                    pageSize = 32;
                }
                if (rb64k.isSelected()) {
                    pageSize = 64;
                }
                break;
        }

    }
}
