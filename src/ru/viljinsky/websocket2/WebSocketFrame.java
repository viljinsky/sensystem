/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;

/**
 *
 * @author viljinsky
 */
class WebSocketFrame extends JPanel {
    JFrame frame;
    
    WebSocketClient client = new WebSocketClient("localhost", 3345) {
        @Override
        void onStopListen() {
            textOut("SERVER SEND BY");
        }

        @Override
        void onListen() {
            textOut("LISTEN...");
        }

        @Override
        void onError(String message) {
            textOut("ERROR   : " + message);
        }

        @Override
        void onMessage(String message) {
            textOut("MESSAGE : " + message);
        }
    };

    public void connect() {
        new Thread(client).start();
    }

    public void waitServer() {
        new Thread() {
            @Override
            public void run() {
                while (client.socket == null) {
                    connect();
                    long t = System.currentTimeMillis();
                    while (System.currentTimeMillis() < (t + 1000)) {
                    }
                }
            }
        }.start();
    }
    
    static final String CONNECT = "connect";
    static final String BY = "by";
    static final String MESSAGE = "message";
    static final String WAIT = "wait";
    CommandBar commandBar = new CommandBar(CONNECT,BY,null, MESSAGE, WAIT, null,"clear") {
        @Override
        public void doCommand(String command) {
            try {
                switch (command) {
                    case WAIT:
                        if (client.socket == null) {
                            waitServer();
                        }
                        break;
                        
                    case CONNECT:
                        connect();
                        break;
                        
                    case MESSAGE:
                        client.send("im lient");
                        textOut("message to server Im client");
                        break;
                        
                    case BY:
                        client.send(BY);
                        textOut("message to server by");
                        client.close();
                        break;
                        
                    case "clear":
                        messagePane.clear();
                        break;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(getParent(), e.getMessage());
            }
        }
    };
    MessagePane messagePane = new MessagePane();

    void textOut(String message) {
        messagePane.textOut(message + "\n");
    }

    public WebSocketFrame() {
        setLayout(new BorderLayout());
        add(commandBar, BorderLayout.PAGE_START);
        add(messagePane);
    }

    public void showInFrame(Component parent) {
        frame = new JFrame("Client");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client != null) {
                    try {
                        client.out.write("by".getBytes("utf-8"));
                        client.out.flush();
                        client.close();
                    } catch (Exception ex) {
                    }
                }
            }
        });
    }
    
    public static void main(String[] args){
        new WebSocketFrame().showInFrame(null);
    }
}
