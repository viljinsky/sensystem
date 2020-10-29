/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
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
            return WebSocketPanel.this.isEnabled(command);
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
        messagePane.textOut(message+"\n");
    }

    public void setStatus(String message) {
        statusBar.setText(message);
    }
    WindowListener adapter = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            onClosing();
        }

        @Override
        public void windowOpened(WindowEvent e) {
            onOpen();
        }
        
        
    };

    public void onClosing() {
    }
    
    public void onOpen(){
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

    JFrame frame;
    String title = "WebSocketPanel";
    public void setTitle(String title){
        this.title = title;
        if (frame!=null){
            frame.setTitle(title);
        }
    }
    
    Image image = null;
    public void setIcon(Image image){
        this.image = image;
        if(frame!=null){
            frame.setIconImage(image);
        }
    }
    public void showInFrame(Component parent) {
        frame = new JFrame(title);
        frame.setIconImage(image);
        frame.setContentPane(this);
        if(parent == null){
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setAlwaysOnTop(true);
        }
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        frame.addWindowListener(adapter);
    }
 
    public static void main(String[] args){
        WebSocketPanel panel = new WebSocketPanel(){

            @Override
            public void doCommand(String command) {
                System.out.println(command);
                updateActions();
            }

            @Override
            public boolean isEnabled(String command) {
                System.out.println(command);
                return super.isEnabled(command); //To change body of generated methods, choose Tools | Templates.
            }
            
            
            
        };
        panel.showInFrame(null);
    }
}
