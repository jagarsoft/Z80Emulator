package com.github.jagarsoft.ZuxApp.modules.dataregion;

import com.github.jagarsoft.ZuxApp.infrastructure.FIleBrowser;
import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.dataregion.events.DataBlockMapLoadedEvent;
import com.github.jagarsoft.ZuxApp.modules.dataregion.events.FileDataBlockMapSelectedEvent;
import com.github.jagarsoft.ZuxApp.modules.logger.events.LogEvent;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJMenuItemToMenuCommand;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

public class DataRegionModule extends BaseModule {
    private DataRegion dataRegion;

    @Override
    public String getName() {
        return "DataRegion";
    }

    @Override
    public void configure() {
        dataRegion = new DataRegion();

        eventBus.subscribe(FileDataBlockMapSelectedEvent.class, (Consumer<FileDataBlockMapSelectedEvent>)e -> {
            try {
                dataRegion.load(e.getSelectedFile().getAbsolutePath());
                eventBus.publish(new LogEvent(dataRegion.getEntries() + " entries, " + dataRegion.getRegionsCount() + " regions; " + dataRegion.getInvalidEntries() + " invalid entries"));
                eventBus.publish(new DataBlockMapLoadedEvent(dataRegion));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void initUI() {
        JInternalFrame frame = new JInternalFrame("Data Regions", true, true, true, true);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        DataRegionView view = new DataRegionView(dataRegion);
        frame.add(view, BorderLayout.CENTER);

        JMenuItem openFileItem = new JMenuItem("Load Data Block Map...");
        openFileItem.addActionListener(new FIleBrowser(eventBus, new FileDataBlockMapSelectedEvent()));

        commandBus.execute(new AddJMenuItemToMenuCommand("File", "Load binary image...", openFileItem));
        
        commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
    
    public DataRegion getDataRegion() {
        return dataRegion;
    }
}