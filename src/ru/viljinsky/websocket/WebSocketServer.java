/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author viljinsky
 */
public abstract class WebSocketServer extends ArrayList {
    static final String UTF8 = "utf-8";
    ServerSocket server;
    
    public boolean isClosed(){
        return server==null || server.isClosed();
    }
    
    public static final int SERVER_RUN = 1;
    public static final int SERVER_STOP = 2;
    
    public static final int SOCKET_CONNECT = 1;
    public static final int SOCKET_MESSAGE = 2;
    public static final int SOCKET_DISCONNECT = 3;
    public static final int SOCKET_ERROR = 4;
    
    public abstract void onStateChange(int state);
        
    public abstract void onSocketEvent(int event,Socket socket,String message);

    public abstract void onMassage(String message);

    
    class ClientHandler {

        Socket socket;
        InputStream in;
        OutputStream out;
        Map<String,Object> headers = new HashMap<>();
        
        void readHeader(String message){
            String[] ss = message.split("\n");
            for(String s:ss){
                String[] p  = s.split(":");
                if (p.length>1){
                    headers.put(p[0].trim(), p[1].trim());
                }
            }
        }
        
        public boolean isConnected() {
            return socket != null && socket.isConnected();
        }

        public boolean isClosed() {
            return socket == null || socket.isClosed();
        }

        public ClientHandler(Socket socket) throws Exception {
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            
        }

        public void send(String message) throws Exception {
            out.write(message.getBytes(UTF8));
            out.write(0);
            out.flush();
        }

        void listen() {
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            ByteArrayOutputStream data = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n;
                            while ((n = in.read(buf)) != 0) {
                                data.write(buf, 0, n);
                                if (buf[n - 1] == 0) {
                                    break;
                                }
                            }
                            if (data.size() == 0) {
                                continue;
                            }
                            String message = new String(data.toByteArray(), UTF8).trim();
                            if (message.startsWith("by")) {
                                close();
                                remove(ClientHandler.this);
                                onSocketEvent(SOCKET_DISCONNECT, socket,null);
                                break;
                            }
                            if (message.startsWith("GET")){
                                readHeader(message);
                            } else {
                                onSocketEvent(SOCKET_MESSAGE, socket, message);
                            }
                        }
                    } catch (Exception e) {
                        onSocketEvent(SOCKET_ERROR, socket, e.getMessage());
                    }
                }
            };
            t.start();
        }

        public void close() throws Exception {
            in.close();
            out.close();
            socket.close();
        }

        @Override
        public String toString() {
            return socket.toString() + " " + isConnected();
        }
    }
    
    public void addHandler(Socket socket) {
        try {
            ClientHandler h = new ClientHandler(socket);
            if (h.isConnected()) {
                add(h);
                h.listen();
                onSocketEvent(SOCKET_CONNECT, socket,null);
            }
        } catch (Exception e) {
        }
    }

    protected ClientHandler getHandler(Socket socket) {
        for(Object p: this){
            ClientHandler h = (ClientHandler)p;
            if (h.socket.equals(socket)) {
                return h;
            }
        }
        return null;
    }
    
    private Thread t;

    public void start() throws Exception {
        clear();
        server = new ServerSocket(3345);
        onStateChange(SERVER_RUN);
        t = new Thread() {

            @Override
            public void interrupt() {
                super.interrupt(); //To change body of generated methods, choose Tools | Templates.
                System.out.println("interapted");
            }
            
            @Override
            public void run() {
                try {
                    while (!interrupted()) {
                        addHandler(server.accept());
                    }
                    
                } catch (IOException e) {
                    System.err.println("server start error : " + e.getMessage());
                    onStateChange(SERVER_STOP);
                }
            }
        };
        t.start();
    }

    public void stop() {
        try{
            if (server != null) {
                t.interrupt();
                for (Iterator it = this.iterator(); it.hasNext();) {
                    ClientHandler h = (ClientHandler)it.next();
                    try {
                        h.send("by");
                        h.close();
                    } catch (Exception e) {
                    }
                    it.remove();
                }
                server.close();
                server = null;
                onStateChange(SERVER_STOP);
            }
        } catch(IOException e){
        }
    }

    //-------------------------------------------------------------------------
    
    public boolean hasHeaderValue(Socket socket,String key){
        for(int i=0;i<size();i++){
            ClientHandler h = (ClientHandler)get(i);
            if (h.socket.equals(socket) && h.headers.containsKey(key)){
                return true;
            }
        }
        return false;
    }
    
    public Object getHeaderValue(Socket socket,String key){
        for(int i=0;i<size();i++){
            ClientHandler h = (ClientHandler)get(i);
            if(h.socket.equals(socket) && h.headers.containsKey(key)){
                return h.headers.get(key);
            }
        }
        return null;
    }
    
    public void setHeaderValue(Socket socket,String key,Object value){
        for(int i=0;i<size();i++){
            ClientHandler h = (ClientHandler)get(i);
            if(h.socket.equals(socket) ){
                h.headers.put(key, value);
            }
        }
    }
    
    public Object getHeaders(Socket socket){
        for(Object p: this){
            ClientHandler h = (ClientHandler)p;
            if (h.socket.equals(socket)){
                return h.headers;
            }
        }
        return null;
    }
    
    public List<Socket> socketList(){
        List<Socket> list = new ArrayList<>();
        for(Object p: this){
            ClientHandler h = (ClientHandler)p;
            list.add(h.socket);
        }
        return list;
    }
    
    public void send(Socket socket,String message){
        for(Object p: this){
            ClientHandler h = (ClientHandler)p;
            if (h.socket.equals(socket)){
                try{
                    h.send(message);
                } catch(Exception e){
                    onSocketEvent(SOCKET_ERROR,h.socket, e.getMessage());
                }
            }
        }
    }
    
    public void sendToAll(String message) {
        if(server==null){
            onMassage("ERROR сервер не запущен");
            return;
        }
        if (size()==0){
            onMassage("ERROR Нет слушателей");
            return;
        }
        onMassage("Оправка сообщения...");
        for (Object p: this) {
            ClientHandler h = (ClientHandler)p;
            try {
                h.send(message);
            } catch (Exception e) {
                onSocketEvent(SOCKET_ERROR,h.socket, e.getMessage());
            }
        }
        onMassage("Сообщение успешно отпрвалено ("+size()+"слушателей)");
    }

    public void list() {
        for (Object p : this) {
            onMassage(p.toString());
        }
    }
    
}
