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
import javax.swing.SwingUtilities;
import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;

/**
 *
 * @author viljinsky
 */
class WebSocketFrame extends JPanel {
    
    JFrame frame;
    
    WebSocketClient         client = new WebSocketClient("localhost", 3345){
            
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

    public void connect() {
        if (client.isClosed()){
            client.run();
        }
    }
    
    
    Thread t;

    public void waitServer() {
        t = new Thread(){

            @Override
            public void run() {
                try{
                    while(!client.isConected()){
                        client.run();
                        join(1000);
                        System.out.println("next");
                    }
                } catch(InterruptedException e){
                    System.out.println("interapted");    
                }
                
            }
                                    
        };
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
                        t.interrupt();
//                        noWait();
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
                t.interrupt();
//                noWait();
                if (client != null) {
                    try {
                        client.by();
                        client.close();
                        client.socket = null;
                    } catch (Exception ex) {
                    }
                }
            }
        });
        waitServer();
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new WebSocketFrame().showInFrame(null);
            }
        });
    }
}
