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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;

class WebSocketClient implements Runnable{
    
    String host;
    int port;

    public WebSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    
    Socket socket;
    
    InputStream in;
    
    OutputStream out;
    
    @Override
    public void run() {
        try{
            
            socket = new Socket(host,port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            listen();

        } catch (Exception e){
            onError(e.getMessage());
        }
    }
            
    void onMessage(String message){
    }
    
    void onError(String message){
    }
    
    void onListen(){
    }
    
    void listen(){
        try{
            while(true){
                onListen();
                ByteArrayOutputStream data = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n;
                
                n = in.read(buf);
                data.write(buf, 0, n);
                        
//                while((n=in.read(buf))!=-1){
//                    data.write(buf, 0, n);
//                }
                if (data.size()==0) continue;
                onMessage(new String(data.toByteArray(),"utf-8"));
            }
        } catch(Exception e){
            onError(e.getMessage());
        }
    }
    
    public void close(){
        try{
            in.close();
            out.close();
            socket.close();
        } catch (Exception e){
        }
    }
    
}

class WebSocketServer implements Runnable{
    
    class ClientHandler{
        Socket socket;
        InputStream in;
        OutputStream out;
        
        public ClientHandler(Socket socket) throws Exception{
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
        }
        
        void listen(){
            try{
                while(true){
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int n;
                    if ((n=in.read(buf)) != -1){
                        data.write(buf, 0, n);
                    }
                    if (data.size()==0) continue;
                    onClientMessage(socket,new String(data.toByteArray()));
                }
            } catch (IOException e){
                onClientError(socket,e.getMessage());
            }
        }
        
        public void close(){
            try{
                in.close();
                out.close();
                socket.close();
            } catch(Exception e){
            }
        }
        
    }
    
    List<ClientHandler> list = new ArrayList<>();
    
    void removeSocket(Socket socket){
        for(Iterator<ClientHandler> it = list.iterator();it.hasNext();){
            ClientHandler h = it.next();
            if (h.socket.equals(socket)){
                h.close();
                it.remove();
            }
        }
    }
    
    int port = 3345;
    
    public void onStart(){
        System.out.print("server started");
    }
        
    public void onError(String message){
        System.out.println("client created");
    }
    
    public void onMessage(String message){
        System.out.println("MESSAGE : "+message);
    }
    
    public void onClient(Socket soket){
        System.out.println("client created");
    }
    public void onClientMessage(Socket socket,String message){
    }
    
    public void onClientError(Socket soket,String message){
    }

    @Override
    public void run() {
        try {

            ServerSocket server = new ServerSocket(port);
            onStart();
            while(true){
                ClientHandler h = new ClientHandler(server.accept());
                list.add(h);                
                new Thread(){

                    @Override
                    public void run() {
                        h.listen();
                    }
                    
                }.start();
                
                onClient(h.socket);
            }
            
        } catch (Exception e) {
            onError(e.getMessage());
        }
    }
                
    public void start(){        
        new Thread(this).start();
    }
    
    public void message(String message){
        int count = 0;
        for(ClientHandler h: list){
            try{
                h.out.write(message.getBytes("utf-8"));
                h.out.flush();
                count++;
            } catch (IOException e){
                onError(e.getMessage());
            }
        }
        onMessage("message has be sebding to "+count+" client");
    }
        
}

class ChildFrame extends JPanel{
    JFrame frame;
    
    WebSocketClient client = new WebSocketClient("localhost",3345){

        @Override
        void onListen() {
            textOut("LISTEN...");
        }
        
        @Override
        void onError(String message) {
            textOut("ERROR   : "+message);
        }

        @Override
        void onMessage(String message) {
            textOut("MESSAGE : "+message);
        }
    };
    
    public void connect(){
        new Thread(client).start();
    }
    
    public void waitServer(){
        new Thread(){

            @Override
            public void run() {
                while(client.socket == null){
                    connect();
                    long t = System.currentTimeMillis();
                    while(System.currentTimeMillis()<(t+1000)){
                    }
                }
            }

        }.start();
    }
    
    CommandBar commandBar = new CommandBar("command","quit","by","wait"){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case "wait":
                        if (client.socket==null){
                            waitServer();
                        }
                        break;
                    case "command":
                        connect();
                        break;
                    case "quit":
                        client.out.write("Im client".getBytes("utf-8"));
                        client.out.flush();
                        textOut("message to server Im client");
                        break;
                    case "by":
                        client.out.write("by".getBytes("utf-8"));
                        client.out.flush();
                        textOut("message to server by");
                        client.close();
                        break;
                }
            } catch (Exception e){
                JOptionPane.showMessageDialog(getParent(), e.getMessage());
            }
        }
        
    };
    
    MessagePane messagePane = new MessagePane();
    
    void textOut(String message){
        messagePane.textOut(message+"\n");
    }

    public ChildFrame() {
        setLayout(new BorderLayout());
        add(commandBar,BorderLayout.PAGE_START);
        add(messagePane);
    }
    
    public void showInFrame(Component parent){
        frame = new JFrame("Client");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(300,200);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (client!=null){
                    try{
                        client.out.write("by".getBytes("utf-8"));
                        client.out.flush();
                        client.close();
                    } catch(Exception ex){                        
                    }
                }
            }
            
        });
    }    
}

/**
 *
 * @author viljinsky
 */
public class MainFrame  extends JPanel{
    
    WebSocketServer srv = new WebSocketServer(){

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
        ChildFrame child = new ChildFrame();
        child.showInFrame(getParent());
        child.waitServer();
    }
    
    void createClient2(){
        int x=100,y=100;
        for(int i=0;i<4;i++){
            ChildFrame child = new ChildFrame();
            child.showInFrame(child);
            child.frame.setLocation(x, y);
            x += 50;
            y += 50;
            child.connect();
        }
    }
    
    CommandBar commandBar = new CommandBar("start","command","message"){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case "start":
                        srv.start();
                        break;
                    case "command":
                        createClient();
                        break;
                    case "message":
                        srv.message("helo evry body");
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
