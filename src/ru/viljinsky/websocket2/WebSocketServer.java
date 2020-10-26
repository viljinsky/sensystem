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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author viljinsky
 */
abstract class WebSocketServer extends ArrayList {
    static final String UTF8 = "utf-8";
    ServerSocket server;
    
    public static final int SERVER_RUN = 1;
    public static final int SERVER_STOP = 2;
    
    public static final int SOCKET_CONNECT = 1;
    public static final int SOCKET_MESSAGE = 2;
    public static final int SOCKET_DISCONNECT = 3;
    public static final int SOCKET_ERROR = 4;
    
    public void onStateChange(int state){
    }
    
    public void onSocketEvent(int event,Socket socket){
    }
    
    public void onSocketEvent(int event,Socket socket,String message){
    }

    public abstract void onMassage(String message);

    class ClientHandler {

        Socket socket;
        InputStream in;
        OutputStream out;

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
//                                onClientBy(socket);
                                onSocketEvent(SOCKET_DISCONNECT, socket);
//                                onMessage(socket, "closed");
                                break;
                            }
                            onSocketEvent(SOCKET_MESSAGE, socket, message);
                        }
                    } catch (Exception e) {
                        onSocketEvent(SOCKET_ERROR, socket, e.getMessage());
//                        onSendError(socket, e.getMessage());
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
    
//    List<ClientHandler> list;

    public void addHandler(Socket socket) {
        try {
            ClientHandler h = new ClientHandler(socket);
            if (h.isConnected()) {
                add(h);
                h.listen();
                onSocketEvent(SOCKET_CONNECT, socket);
            }
        } catch (Exception e) {
        }
    }

    public ClientHandler getHandler(Socket socket) {
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
        } catch(Exception e){
        }
    }

    public void sendToAll(String message) {
        for (Object p: this) {
            ClientHandler h = (ClientHandler)p;
            try {
                h.send(message);
            } catch (Exception e) {
                onSocketEvent(SOCKET_ERROR,h.socket, e.getMessage());
            }
        }
    }

    public void list() {
        for (Object p : this) {
            onMassage(p.toString());
        }
    }
    
}
