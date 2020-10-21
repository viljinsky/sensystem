/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;


/**
 *
 * @author viljinsky
 */
public class MainTCP extends JPanel{
    
    String example = "HTTP/1.1 200 OK\n" +
                    "Content-Type: text/html; charset=UTF-8\n" +
                    "Content-Length: %d\n" +
                    "\n%s";
    
    String str(){
        return String.format(example,0,"");
    }
    
    String str(String text){
        return String.format(example, text.length(),text);
    }
    
    MessagePane messagePane = new MessagePane();
    
    static final String SERVER_START = "start";
    static final String SERVER_STOP = "stop";
    static final String CLIENT = "client";
    static final String CLEAR = "clear";
    
    void server_start() throws Exception{
        
        Pattern p = Pattern.compile("form-data");
      
        new Thread(){
                        

            @Override
            public void run() {
                TcpServer server = new TcpServer(){

                    @Override
                    public void onStart(ServerSocket server) {
                        messagePane.textOut(server.toString()+" started\n");
                    }
                                        
                    @Override
                    public String responce(Socket client, String request) {
                        
                        messagePane.textOut(client.toString()+" \""+ request+"\"\n");                                                 
                        return str("yes. it is.");
                    }
                    
                };
                server.start(3345);
            }
            
        }.start();
        
    }
    
    void server_stop() throws Exception{
        TcpClient client = new TcpClient("localhost", 3345);
        try{
            System.out.println(client.sendMessage("."));
        } finally{
            client.close();
        }
    }
    
    public void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    
    public void doCommand(String command){
        try{
            switch(command){
                case CLEAR:
                    messagePane.clear();
                    break;
                    
                case SERVER_START:
                    server_start();
                    break;
                    
                case SERVER_STOP:
                    server_stop();
                    break;
                    
                case CLIENT:
                    new ClientFrame().showModal(getParent());
                    break;
            }
            
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }

    public MainTCP() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800,600));
        JToolBar toolBar = new CommandBar(SERVER_START,SERVER_STOP,null,CLIENT,null,CLEAR){

            @Override
            public void doCommand(String command) {
                MainTCP.this.doCommand(command);
            }
            
            
        };
        add(toolBar,BorderLayout.PAGE_START);
        add(messagePane);
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame("TcpServer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MainTCP());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }
    
}
