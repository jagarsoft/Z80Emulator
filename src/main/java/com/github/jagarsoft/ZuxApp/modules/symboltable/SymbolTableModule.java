package com.github.jagarsoft.ZuxApp.modules.symboltable;

import com.github.jagarsoft.ZuxApp.infrastructure.module.BaseModule;
import com.github.jagarsoft.ZuxApp.modules.mainmodule.commands.AddJInternalFrameToDesktopPaneCommand;

import javax.swing.*;
import java.awt.*;

public class SymbolTableModule extends BaseModule {
    private SymbolTable symbolTable;

    @Override
    public String getName() {
        return "SymbolTable";
    }

    @Override
    public void configure() {
        symbolTable = new SymbolTable();
        // Aquí podrías suscribirte a eventos como "SymbolFileLoadedEvent"
        // y cargar el contenido de symbolTable.addSymbol(...)
    }

    @Override
    public void initUI() {
        JInternalFrame frame = new JInternalFrame("Symbol Table", true, true, true, true);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        SymbolTableView view = new SymbolTableView(symbolTable);
        view.loadData();

        frame.add(view, BorderLayout.CENTER);
        this.commandBus.execute(new AddJInternalFrameToDesktopPaneCommand(frame));
        frame.setVisible(true);
    }
}
