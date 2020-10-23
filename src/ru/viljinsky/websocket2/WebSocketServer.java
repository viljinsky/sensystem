/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author viljinsky
 */
class WebSocketServer implements Runnable {
    
    class Request extends HashMap<String, String>{
        
        boolean isWebSocket(){
            if (containsKey("Upgrade")){
                return "websocket".equalsIgnoreCase(get("Upgraded"));
            }
            return false;
        }

        public Request(InputStream in) throws Exception{
            try( BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"))
                    ){
                String line;
                while((line=reader.readLine())!=null){
                    System.out.println("'"+line+"'");
                    String[] s  = line.split(":");
                    if (s.length>1){
                        put(s[0].trim(), s[1].trim());
                    }
                    if (!reader.ready()) break;
                }                
            }            
        }
        
    }

    static final String BAD_REQUEST = "HTTP/1.1 200 OK\n";
    
    class ClientHandler {

        Request request;
        Socket socket;
        InputStream in;
        OutputStream out;

        public ClientHandler(Socket socket) throws Exception {
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
            
        //    request = new Request(in);
            
            list.add(ClientHandler.this);
            listen();
            onClient(socket);
      //      onMessage(request.toString());
            
        }

        private void listen() {
            new Thread(){

                @Override
                public void run() {
                    try {
                        while (true) {
                            try(ByteArrayOutputStream data = new ByteArrayOutputStream();){
                                byte[] buf = new byte[1024];
                                int n;
                                while((n = in.read(buf))!=-1){
                                    data.write(buf, 0, n);
                                    if(buf[n-1] == 0) break;
                                }
                                if (data.size() == 0) {
                                    continue;
                                }
                                onClientMessage(socket, new String(data.toByteArray()));
                            }
                        }
                    } catch (IOException e) {
                        onClientError(socket, e.getMessage());
                    }                    
                }
                
            }.start();
        }

        public void close() {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
            }
        }
        
        public void send(String message) throws Exception{
            out.write(message.getBytes("utf-8"));
            out.flush();
        }
        
        @Override
        public String toString(){
            return "handler "+socket.toString();
        }
    }
    
    List<ClientHandler> list = new ArrayList<>();

    void removeSocket(Socket socket) {
        for (Iterator<ClientHandler> it = list.iterator(); it.hasNext();) {
            ClientHandler h = it.next();
            if (h.socket.equals(socket)) {
                h.close();
                it.remove();
            }
        }
    }
    
    int port;

    public WebSocketServer(int port) {
        this.port = port;
    }
        
    public void onStart() {
        System.out.print("server started");
    }

    public void onError(String message) {
        System.out.println("client created");
    }

    public void onMessage(String message) {
        System.out.println("MESSAGE : " + message);
    }

    public void onClient(Socket soket) {
        System.out.println("client created");
    }

    public void onClientMessage(Socket socket, String message) {
    }

    public void onClientError(Socket soket, String message) {
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            onStart();
            while (true) {
                new ClientHandler(server.accept());
            }
        } catch (Exception e) {
            onError(e.getMessage());
        }
    }

    public void start() {
        new Thread(this).start();
    }
    
    public void by(){
        for(Iterator<ClientHandler> it = list.iterator();it.hasNext();){
            ClientHandler h = it.next();
            try{
                h.send("by");
                h.close();
            } catch (Exception e){
                System.err.println("by error : "+e.getMessage());
            } finally{
                it.remove();
            }
        }
    }
    
    public void list(){
        for(ClientHandler h: list){
            onMessage(h.toString());
        }
        if (list.isEmpty())
            onMessage("list is empty");
        else 
            onMessage("--------------\ntotal client "+list.size());
    }

    public void message(String message) throws Exception{
        int count = 0;
        byte[] msg = message.getBytes("utf-8");
        for (ClientHandler h : list) {
            try {
                h.out.write(msg);
                h.out.write(0);
                h.out.flush();
                count++;
            } catch (IOException e) {
                onError(e.getMessage());
            }
        }
        onMessage("message has be sebding to " + count + " client");
    }
    
}
