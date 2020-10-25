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
import ru.viljinsky.server.StatusBar;

/**
 *
 * @author viljinsky
 */
public class WebSocketFrame3 extends JPanel{
    
    static final String WAIT = "wait";
    static final String MESSAGE = "message";
    static final String BY = "by";
    static final String CLEAR = "clear";
    
    WebSocketClient client = new WebSocketClient("localhost",3345){

        @Override
        void onStopListen() {
            statusText("stop");
        }

        @Override
        void onListen() {
            statusText("listen");
        }

        @Override
        void onError(String message) {
            statusText(message);
        }

        @Override
        void onMessage(String message) {
            textOut(message);
        }
        
    };
    
    void textOut(String text){
        messagePane.textOut(text+"\n");
    }
    
    void statusText(String text){
        statusBar.setText(text);
    }
    
    void showMessage(String text){
        JOptionPane.showMessageDialog(getParent(),text);
    }
    
    StatusBar statusBar = new StatusBar();
    MessagePane messagePane = new MessagePane();
    CommandBar commandBar = new CommandBar(WAIT,MESSAGE,BY,CLEAR){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case WAIT:
                        waitServer();
                        break;
                    case MESSAGE:
                        if (client.isConected())
                            client.send("message");
                        break;
                    case BY:
                        t.interrupt();
                        if (client.isConected()){
                            client.by();
                            client.close();
                        }
                        break;
                    case CLEAR: 
                        messagePane.clear();
                        break;
                }
            } catch (Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
    Thread t;
    
    void waitServer(){
        t = new Thread(){

            @Override
            public void run() {
                try{
                    textOut("listen...");
                    while(!isInterrupted()){
                        client.run();
                        join(1000);
                        System.out.println("wait");
                        textOut("wait");
                    }
                } catch(InterruptedException e){
                    textOut("stiop");
                }
            }
            
        };
        
        t.start();
        
    }
    
    WindowAdapter adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            t.interrupt();
            if (client.isConected()){
                client.by();
                client.close();
            }
        }
        
    };

    public WebSocketFrame3() {
        setLayout(new BorderLayout());
        add(messagePane);
        add(commandBar,BorderLayout.PAGE_START);
        add(statusBar,BorderLayout.PAGE_END);
    }
    
    public void showInFrame(Component parent){
        JFrame frame = new JFrame("Client3");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        frame.setContentPane(this);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        waitServer();
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        new WebSocketFrame3().showInFrame(null);
    }
    
    
}
