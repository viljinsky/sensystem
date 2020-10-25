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
import ru.viljinsky.server.StatusBar;


abstract class WebSocketServer2{
    
    static final String UTF8 = "utf-8";
    
    ServerSocket server;
    
    public abstract void onStart();
    public abstract void onStop();
    public abstract void onClient(Socket socket);
    public abstract void onSendError(Socket socket,String message);
    public abstract void onClientError(Socket socket,String message);
    public abstract void onMessage(Socket socket,String message);
    public abstract void onMassage(String message);
    
    class ClientHandler{
        Socket socket;
        InputStream in;
        OutputStream out;
        
        public boolean isConnected(){
            return socket!=null && socket.isConnected();
        }
        
        public boolean isClosed(){
            return socket==null || socket.isClosed();
        }

        public ClientHandler(Socket socket) throws Exception {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        }
        
        public void send(String message) throws Exception{
            out.write(message.getBytes(UTF8));
            out.write(0);
            out.flush();
        }
        
        void listen(){
            Thread t = new Thread(){

                @Override
                public void run() {
                    try{
                        while(true){
                            ByteArrayOutputStream data = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];

                            int n;
                            while ((n = in.read(buf))!=0){
                                data.write(buf, 0, n);
                                if(buf[n-1]==0) break;
                            }
                            if (data.size()==0) continue;                
                            
                            String message = new String(data.toByteArray(),UTF8);
                            if (message.startsWith("by")){
                                close();
                                list.remove(ClientHandler.this);
                                onMessage(socket,"closed");
                                break;
                            }
                            onMessage(socket,message);
                        }
                    } catch (Exception e){
                        onSendError(socket, e.getMessage());
                    }
                }
                
            };
            t.start();
        }
        
        public void close() throws Exception{
            in.close();
            out.close();
            socket.close();
        }

        @Override
        public String toString() {
            return socket.toString()+ " "+isConnected();
        }
        
        
    }
    
    List<ClientHandler> list;
    
    public void addHandler(Socket socket){
        try{
            ClientHandler h = new ClientHandler(socket);
            if (h.isConnected()){
                list.add(h);
                h.listen();
                onClient(h.socket);
            }
        } catch (Exception e){
        }
    }
    
    Thread t;
    
    public void start() throws Exception{
        list = new ArrayList<>();
        server = new ServerSocket(3345);
        onStart();
        t = new Thread(){

            @Override
            public void run() {
                try{
                    while(true){
                        addHandler(server.accept());
                    }
                } catch(IOException e){
                    System.err.println("server start error : "+e.getMessage());
                    onStop();
                }
            }
        
        
        };
        t.start();
    }
    
    public void stop() throws Exception{
        if (server!=null){
            t.interrupt();
            for(Iterator<ClientHandler> it = list.iterator();it.hasNext();){
                ClientHandler h = it.next();
                try{
                    h.send("by");
                    h.close();
                } catch (Exception e){
                }
                it.remove();
            }
            server.close();
            server=null;
            onStop();
        }
    };
    
    public void sendToAll(String message){
        for(ClientHandler h: list){
            try{
                h.send(message);
            } catch(Exception e){
                onSendError(h.socket, e.getMessage());
            }
        }
    }
    
    public void list(){
        for(ClientHandler h:list){
            onMassage(h.toString());
        }
    }
    
}

/**
 *
 * @author viljinsky
 */
public class MainFrame2 extends JPanel{
    
    WebSocketServer2 server = new WebSocketServer2() {

        @Override
        public void onStart() {
            setStatusText("server start");
        }

        @Override
        public void onStop() {
            setStatusText("server stop");
        }

        @Override
        public void onMassage(String message) {
            textOut(message);
        }

        @Override
        public void onClient(Socket socket) {
            textOut(socket.toString() + " new client ");
        }

        @Override
        public void onClientError(Socket socket, String message) {
            textOut(socket.toString() + " client error "+message);
        }
                

        @Override
        public void onMessage(Socket socket, String message) {
            textOut(socket.toString()+ " "+ message);
        }

        @Override
        public void onSendError(Socket socket, String message) {
            textOut(socket.toString()+" send error :"+message);
        }
        
        
    };
    
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String MESSAGE = "message";
    public static final String LIST = "list";
    public static final String CLIENT = "client";
    
    
    MessagePane messagePane = new MessagePane();
    
    StatusBar statusBar = new StatusBar();
    
    CommandBar commandBar = new CommandBar(START,STOP,null,LIST,MESSAGE,null,CLIENT){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case START:
                        server.start();
                        break;
                        
                    case STOP:
                        server.stop();
                        break;
                                                                        
                    case CLIENT:
                        new WebSocketFrame3().showInFrame(getParent());
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
            try{
                server.stop();
            } catch (Exception ex){
            }
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

    public MainFrame2() {
        setLayout(new BorderLayout());
        add(messagePane);
        add(statusBar,BorderLayout.PAGE_END);
        add(commandBar,BorderLayout.PAGE_START);
                
    }
        
    public void showInFrame(){
        JFrame frame = new JFrame("Server2");
        frame.setContentPane(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        new MainFrame2().showInFrame();
    }
    
}
