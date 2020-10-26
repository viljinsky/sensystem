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
public class WebSocketFrame extends JPanel{
    
    static final String START = "start";
    static final String MESSAGE = "message";
    static final String MESSAGE_TO_ALL = "message to all";
    static final String STOP = "stop";
    static final String CLEAR = "clear";
    
    WebSocketClient client = new WebSocketClient("localhost",3345){

        @Override
        public void onMessage(String message) {
            textOut(message);
        }

        @Override
        public void onStateChange(int state) {
            switch(state){
                case CLOSED:
                    statusText("Нет соединения");
                    break;
                case WAIT:
                    statusText("Ожидание сервера...");
                    break;
                case READY:
                    statusText("Слушаю...");
                    break;                                        
            }
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
    
    CommandBar commandBar = new CommandBar(START,STOP,null,MESSAGE,MESSAGE_TO_ALL,null,CLEAR){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    
                    case START:
                        client.start();
                        break;
                        
                    case STOP:
                        client.stop();
                        break;
                        
                    case MESSAGE:
                        if (client.isConected())
                            client.send("message");
                        break;
                        
                    case MESSAGE_TO_ALL:
                        if (client.isConected()){
                            client.send("all: hello evry body");
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
        
    WindowAdapter adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            if (client!=null)
                client.stop();
        }
        
    };

    public WebSocketFrame() {
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
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        new WebSocketFrame().showInFrame(null);
    }
    
    
}
