/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;
import ru.viljinsky.server.StatusBar;

/**
 *
 * @author viljinsky
 */
public class WebSocketPanel extends JPanel {
    
    MessagePane messagePane = new MessagePane();
    StatusBar statusBar = new StatusBar();
    CommandBar commandBar = new CommandBar("cmd1", "cmd2", "cmd3") {
        @Override
        public void doCommand(String command) {
            WebSocketPanel.this.doCommand(command);
            updateActions();
        }

        @Override
        public boolean isEnabled(String command) {
            return WebSocketPanel.this.isEnabled();
        }
    };

    public void doCommand(String command) {
    }

    public boolean isEnabled(String command) {
        return true;
    }

    public void updateActions() {
        commandBar.updateActions();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(getParent(), message);
    }

    public void textOut(String message) {
        messagePane.textOut(message);
    }

    public void setStatus(String message) {
        statusBar.setText(message);
    }
    WindowListener adapter = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            onClosing();
        }
    };

    public void onClosing() {
    }
    
    public void setCommand(String... commands){
        commandBar.setButtons(commands);
    }

    public WebSocketPanel() {
        setPreferredSize(new Dimension(400, 200));
        setLayout(new BorderLayout());
        add(messagePane);
        add(commandBar, BorderLayout.PAGE_START);
        add(statusBar, BorderLayout.PAGE_END);
    }

    public void showInFrame(Component parent) {
        JFrame frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setAlwaysOnTop(true);
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        frame.addWindowListener(adapter);
    }
    
}
