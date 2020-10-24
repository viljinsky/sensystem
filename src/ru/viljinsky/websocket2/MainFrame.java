/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.awt.BorderLayout;
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
        
    void createClient(){
        WebSocketFrame child = new WebSocketFrame(host,port);
        child.showInFrame(getParent());
        child.waitServer();
    }
    
    
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
    static final String BY = "by";
    static final String LIST = "list";
    static final String MESSAGE = "message";
    
    CommandBar commandBar = new CommandBar(START,STOP,LIST,BY,null,MESSAGE, "client1",null,"client2","json",null,"clear"){

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
                        
                    case BY:
                        srv.by();
                        break;
                                                
                    case MESSAGE:
                        srv.message("messsage"+(++message_count));
                        break;
                    
                    case "clear":
                        messagePane.clear();
                        break;
                        
                    case "client2":
                        new Thread(){

                            @Override
                            public void run() {
                                new WebSocketFrame2().showInFrame("Клиент "+(++client_count));
                            }
                            
                            
                        }.start();
                        break;
                                                                        
                    case "client1":
                        createClient();
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
    }
        
    public static void main(String[] args){
        JFrame frame = new JFrame("Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MainFrame());
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
}
