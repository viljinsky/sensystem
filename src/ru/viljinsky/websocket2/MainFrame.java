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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
public class MainFrame  extends JPanel{
    
    public static final int port = 3345;
    
    public static final String host = "localhost";
    
    WebSocketServer srv = new WebSocketServer(port){

        @Override
        public void onError(String message) {
            textOut("ERROR :"+message+"\n");
        }

        @Override
        public void onClient(Socket socket) {
            textOut(socket+" : connected");
        }

        @Override
        public void onStart() {
            textOut("server started\n");
        }

        @Override
        public void onMessage(String message) {
            textOut(message+"\n");
        }

        @Override
        public void onClientMessage(Socket socket, String message) {
            if ("by".equals(message)){
                removeSocket(socket);
            }
            textOut(socket+" : "+message+"\n");
        }                                
        
    };
        
    void json() throws Exception{
        File file = new File("server_data.json");
        if (file.exists()){
            try(InputStream in = new FileInputStream(file); ByteArrayOutputStream out = new ByteArrayOutputStream();){
                byte[] buf = new byte[1024];
                int n;
                while((n=in.read(buf))!=-1){
                    out.write(buf, 0, n);
                }                
                srv.message(new String(out.toString()));
                
            }
            
        }
    }
    
    int message_count = 0;
    int client_count = 0;
    static final String START = "start";
    static final String STOP = "stop";
    static final String LIST = "list";
    static final String MESSAGE = "message";
    
    CommandBar commandBar = new CommandBar(START,STOP,LIST,null,MESSAGE, "client1",null,"client2","client3","json",null,"clear"){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){

                    case START:
                        srv.start();
                        break;
                        
                    case STOP:
                        srv.stop();
                        break;

                    case LIST:
                        srv.list();
                        break;
                        
                    case MESSAGE:
                        srv.message("messsage"+(++message_count));
                        break;
                    
                    case "clear":
                        messagePane.clear();
                        break;
                        
                    case "client2":
                        new WebSocketFrame2().showInFrame("Клиент "+(++client_count));
                        break;
                                                                        
                    case "client1":
                        new WebSocketFrame().showInFrame(getParent());
                        break;
                        
                    case "client3":
                        new WebSocketFrame3().showInFrame(getParent());
                        break;
                        
                    case "json":
                        json();
                        break;
                }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
    StatusBar statusBar = new StatusBar();
    
    MessagePane messagePane = new MessagePane();
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    
    void textOut(String message){
        messagePane.textOut(message+"\n");
    }

    public MainFrame() {
        setLayout(new BorderLayout());
        add(commandBar,BorderLayout.PAGE_START);
        add(messagePane);
        add(statusBar,BorderLayout.PAGE_END);
    }
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            srv.stop();
        }
        
    };
    
    public void showInFrame(){
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new MainFrame());
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(adapter);        
    }
        
    public static void main(String[] args){
        new MainFrame().showInFrame();
    }
    
}
