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
import java.net.Socket;
import static java.util.UUID.randomUUID;

/**
 *
 * @author viljinsky
 */
public class WebSocketClient{
    
    public static final int CLOSED =0;
    public static final int WAIT = 1;
    public static final int READY = 2;
    public static final int SERVER_DISCONNECT = 3;
    
    public void onMessage(String message){
    }
    
    public void onStateChange(int state){
    }
    
    private final String host;
    private final int port;
    
    static final String UTF8 = "utf-8";
    static final String REQUEST = 
            "GET / HTTP/1.1\n" +
            "Host: %s\n" +
            "Upgrade: websocket\n" +
            "Connection: Upgrade\n" +
            "Origin: http://timetabler.ru\n" +
            "Sec-WebSocket-Key: %s\n" +
            "Sec-WebSocket-Version: 13\n";

    public WebSocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    Socket socket;
    InputStream in;
    OutputStream out;

    Thread t ;
    
    public void start(){
        if(t!=null) return;
        t = new Thread(){

            @Override
            public void run() {
                while(!isInterrupted()){
                    try{
                        System.out.println("wait connection...");
                        onStateChange(WAIT);
                        socket = new Socket(host, port);
                        in = socket.getInputStream();
                        out = socket.getOutputStream();
                        send(String.format(REQUEST,host,randomUUID()));
                        listen();
                        join(1000);
                    } catch (InterruptedException e){
                        System.out.println("no connection");
                        onStateChange(CLOSED);
                        break;
                    } catch (Exception e){
                        socket = null;
                    }
                }
            }
            
        };
        t.start();
    }
    
    public void stop(){
        try{
            if (t!=null){
                t.interrupt();
                System.out.println("stop");
                onStateChange(CLOSED);
                if (isConected()){
                    send("by");
                    socket.close();
                    socket = null;
                }   
            }
        } catch (Exception e){
        } finally{
            t = null;
        }
    }

    public void send(String message) throws Exception{
        out.write(message.getBytes(UTF8));
        out.write(0);
        out.flush();
        System.out.println("send message");
    }

    void listen() {
        try {
            while (isConected()) {
                System.out.println("listen.");
                onStateChange(READY);
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
                    String message = new String(data.toByteArray(),UTF8);
                    if (message.startsWith("by")){ 
                        close();
                        break;
                    }
                    System.out.println("message");
                    onMessage(message);
                }
            }
        } catch (IOException e) {
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
            onStateChange(CLOSED);
        }
    }
    
    public boolean isClosed(){
        return socket==null || socket.isClosed();
    }
    
    public boolean isConected(){
        return socket!=null && socket.isConnected();
    }
    
}
