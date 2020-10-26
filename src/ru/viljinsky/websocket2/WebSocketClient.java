/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import static java.util.UUID.randomUUID;

/**
 *
 * @author viljinsky
 */
public class WebSocketClient implements Runnable {
    
    private final String host;
    private final int port;
    
    static final String UTF8 = "utf-8";
    static final String REQUEST = 
            "GET /chat HTTP/1.1\n" +
            "Host: %s\n" +
            "Upgrade: websocket\n" +
            "Connection: Upgrade\n" +
            "Origin: http://javascript.ru\n" +
            "Sec-WebSocket-Key: %s\n" +
            "Sec-WebSocket-Version: 13\n";

    public WebSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    Socket socket;
    InputStream in;
    OutputStream out;

    @Override
    public void run() {
        try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            out.write(String.format(REQUEST,host,randomUUID()).getBytes(UTF8));
            out.write(0);
            out.flush();
            listen();
        } catch (Exception e) {
            onError(e.getMessage());
        }
    }

    void onMessage(String message) {
    }

    void onError(String message) {
    }

    void onListen() {
    }
    
    void onStopListen(){
        System.out.println("server stop conection");
    }
    
    public void send(String message) throws Exception{
        out.write(message.getBytes(UTF8));
        out.write(0);
        out.flush();
    }

    void listen() {
        try {
        while (isConected()) {
            onListen();
            try(ByteArrayOutputStream data = new ByteArrayOutputStream();){
                byte[] buf = new byte[1024];
                int n;
                while((n=in.read(buf))!=-1){
                    data.write(buf, 0, n);
                    if(buf[n-1]==0) break;
                }
                    if (data.size() == 0) {
                        continue;
                    }
                    String msg = new String(data.toByteArray(),"utf-8");
                if (msg.startsWith("by")){ 
                    close();
                    break;
                }
                onMessage(msg);
            }
        }
        onStopListen();
        } catch (Exception e) {
            onError(e.getMessage());
    }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
        } finally{
            socket = null;
        }
    }
    
    public boolean isClosed(){
        return socket==null || socket.isClosed();
    }
    
    public boolean isConected(){
        return socket!=null && socket.isConnected();
    }
    
//    public void by(){
//        try{
//            out.write("by".getBytes(UTF8));
//            out.write(0);
//            out.flush();
//        } catch (IOException e){
//            System.err.println(e.getMessage());
//        }
//        
//    }
    
}
