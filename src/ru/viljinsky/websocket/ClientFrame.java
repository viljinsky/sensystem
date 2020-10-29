/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket;

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
public class ClientFrame extends JPanel{
    
    static final String START = "start";
    static final String MESSAGE = "message";
    static final String MESSAGE_TO_ALL = "message to all";
    static final String STOP = "stop";
    static final String CLEAR = "clear";
    static final String HELLOW = "hello";
    
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
                    try{
                        sendHello();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
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
    
    void sendHello() throws Exception{
        if (client.isConected()){
            
            client.send("hello","login=admin","password=sensystem","description=Кабинет руссуого языка и литературы");

//            client.out.write("hello".getBytes("utf-8"));
//            client.out.write(0x0d);
//            
//            client.out.write("login = client1".getBytes("utf-8"));
//            client.out.write(0x0d);
//            
//            client.out.write("password = sensystem".getBytes("utf-8"));
//            client.out.write(0x0d);
//            
//            client.out.write("description = Кабиент русского языка и литературы".getBytes("utf-8"));
//            client.out.write(0x0d);
//            
//            client.out.write(0);
//            
//            client.out.flush();


        }
    }
    
    CommandBar commandBar = new CommandBar(START,STOP,null,MESSAGE,MESSAGE_TO_ALL,HELLOW,null,CLEAR){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){                    
                    case HELLOW:
                        sendHello();
                        break;
                    
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
                            client.send("master","hello evry body");
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

        @Override
        public void windowOpened(WindowEvent e) {
            System.out.println("ClientFrame.opened");
            client.start();
        }
        
        
        
    };
    
    

    public ClientFrame() {
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
        frame.addWindowListener(adapter);
        frame.setVisible(true);
    }
    
    public static void main(String[] args){
        new ClientFrame().showInFrame(null);
    }
    
    
}
