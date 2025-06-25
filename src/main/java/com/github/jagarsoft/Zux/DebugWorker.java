package com.github.jagarsoft.Zux;

import com.github.jagarsoft.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class DebugWorker {
}

private static class DebugWorkerServer extends SwingWorker<Void, Void> {
    @Override
    protected Void doInBackground() throws IOException {
        int puerto = 1234;
        ServerSocket serverSocket = new ServerSocket(puerto);
        System.out.println("ZuxEmulator started debugging server on port " + puerto);

        while (true) {
            Socket cliente = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);

            String mensaje = in.readLine();
            System.out.println("Cliente dijo: " + mensaje);

            if( mensaje == null ) {
                continue;
            }

            out.println("¡Hola cliente! Recibí tu mensaje: " + mensaje);

            try {
                dbgCommands.put(mensaje);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            cliente.close();
        }
    }
}

dbgWorker = new DebugWorkerServer();

SwingWorker worker = new SwingWorker<Void, Rectangle>() {
    String cmd;

    @Override
    protected Void doInBackground() {
        // interrupts are ignored while debugging is on

        do {
            int pc = cpu.getPC();
            Logger.info("fetch ");
            byte opC = zux.peek(pc);
            //disassembler.fetch(opC);
            disassembler.list(pc, 16);

            do {
                //try {
                //cmd = dbgCommands.poll(500, TimeUnit.MILLISECONDS);
                cmd = dbgCommands.poll();
                        /*} catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }*/
            } while (cmd == null);

            switch (cmd) {
                case "stepinto":
                    cpu.fetch(opC); // fetch opCode
                    break;
                case "stepover":

                    break;
                case "stepout":

                    break;
            }
        } while (!cmd.equals("stop"));

        return null;
    }

    @Override
    protected void process(List<Rectangle> chunks) {
        for (Rectangle rect : chunks) {
            screen.repaint(rect);
        }
    }
};