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
import javax.swing.text.StyleConstants;
import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;

/**
 *
 * @author viljinsky
 */
class WebSocketFrame extends JPanel {
    
    JFrame frame;
    
    WebSocketClient client;
    
    public void connect() {
        if (client.isClosed()){
            client.run();
        }
    }
    
    class TClient extends Thread{

        @Override
        public void run() {
            textOut("waiting server");
            while (!client.isConected()) {
                client.run();
                long t = System.currentTimeMillis();
                while (System.currentTimeMillis() < (t + 1000)) {
                }
                if (isInterrupted()){
                    textOut("interapted");
                    break;
                }
            }
        }
        
    }
    
    TClient t;

    public void waitServer() {
        t = new TClient();
        t.start();
    }
    
    public void noWait(){
        if (t!=null){
            t.interrupt();
            t = null;
        }
    }
    
    static final String CONNECT = "connect";
    static final String BY = "by";
    static final String MESSAGE = "message";
    static final String WAIT = "wait";
    static final String NOWAIT = "nowait";
    CommandBar commandBar = new CommandBar(CONNECT,WAIT,NOWAIT,BY,null, MESSAGE, null,"clear") {
        @Override
        public void doCommand(String command) {
            try {
                switch (command) {
                    
                    case NOWAIT:
                        noWait();
                        break;
                        
                    case WAIT:
                        waitServer();
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

    public WebSocketFrame(String host,int port) {
        
        setLayout(new BorderLayout());
        add(commandBar, BorderLayout.PAGE_START);
        add(messagePane);
        client = new WebSocketClient(host, port){
            
            @Override
            void onStopListen() {
                client.close();
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
                noWait();
                if (client != null) {
                    try {
                        client.by();
                        client.close();
                    } catch (Exception ex) {
                    }
                }
            }
        });
//        waitServer();
    }
    
    public static void main(String[] args){
        new WebSocketFrame(MainFrame.host,MainFrame.port).showInFrame(null);
    }
}
