/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.Socket;
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
public class MainFrame extends JPanel{
    
    WebSocketServer server = new WebSocketServer() {

        @Override
        public void onStateChange(int state) {
            switch(state){
                case SERVER_RUN:
                    setStatusText("Сервер запущен");
                    break;
                case SERVER_STOP:
                    setStatusText("Сервер остановлен");
                    break;
            }
        }

        @Override
        public void onSocketEvent(int event, Socket socket) {
            onSocketEvent(event, socket,null); 
        }
                
        @Override
        public void onSocketEvent(int event, Socket socket,String message) {
            switch(event){
                
                case SOCKET_CONNECT:
                    textOut(socket.toString()+" connected");
                    break;
                    
                case SOCKET_MESSAGE:
                    textOut(socket.toString()+" \""+message+"\"");
                    break;
                    
                case SOCKET_ERROR:
                    textOut(socket.toString()+" ERROR :\""+message+"\"");
                    break;
                    
                case SOCKET_DISCONNECT:
                    textOut(socket.toString()+" disconnected");
                    break;
            }
        }
        
        
        
        @Override
        public void onMassage(String message) {
            textOut(message);
        }

    };
    
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String MESSAGE = "message";
    public static final String LIST = "list";
    public static final String CLIENT = "client";
    public static final String CLEAR = "clear";
        
    MessagePane messagePane = new MessagePane();
    
    StatusBar statusBar = new StatusBar();
    
    CommandBar commandBar = new CommandBar(START,STOP,null,LIST,MESSAGE,null,CLIENT,null,CLEAR){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case CLEAR:
                        messagePane.clear();
                        break;
                        
                    case START:
                        server.start();
                        break;
                        
                    case STOP:
                        server.stop();
                        break;
                                                                        
                    case CLIENT:
                        new WebSocketFrame().showInFrame(getParent());
                        break;
                        
                    case MESSAGE:
                        server.sendToAll("hello gays");
                        break;
                        
                    case LIST:
                        server.list();
                        break;
                        
                    case "CMD1":
                        statusBar.setText(command);
                        textOut(command);
                        throw new UnsupportedOperationException("unsupported yet");
                }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            server.stop();
            System.out.println("window closing");
        }
        
    };
                
    void setStatusText(String str){
        statusBar.setText(str);
    }
    
    void textOut(String str){
        messagePane.textOut(str+"\n");
    }
    
    void showMessage(String str){
        JOptionPane.showMessageDialog(getParent(), str);
    }
        
    public MainFrame() {
        setLayout(new BorderLayout());
        add(messagePane);
        add(statusBar,BorderLayout.PAGE_END);
        add(commandBar,BorderLayout.PAGE_START);
    }
        
    public void showInFrame(){
        JFrame frame = new JFrame("WebSocket Server");
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        new MainFrame().showInFrame();
    }
    
}
