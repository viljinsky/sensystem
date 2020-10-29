/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.Color;
import ru.viljinsky.websocket.WebSocketPanel;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import javax.swing.SwingUtilities;
import ru.viljinsky.websocket.WebSocketServer;


/**
 *
 * @author viljinsky
 */
public class Server extends WebSocketPanel{
    
    int port = 7035;
    
    String fileName = "timetabler.json";
    
    void saveData(String json){
        File file = new File(fileName);
        try(FileOutputStream out = new FileOutputStream(file);){
            out.write(json.getBytes("utf-8"));
            out.flush();
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }
    String readData(){
        File file = new File(fileName);
        if(file.exists()){
            try(FileInputStream in = new FileInputStream(file);){
                byte[] buf = new byte[1024];
                int n ;
                StringBuilder stringBuilder = new StringBuilder();
                while((n=in.read(buf))!=-1){
                    stringBuilder.append(new String(buf,0,n,"utf-8"));
                }
                return stringBuilder.toString();
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        return null;
    }
    
    WebSocketServer server = new WebSocketServer(port) {

        @Override
        public void onMassage(String message) {
            textOut(message);
        }

        @Override
        public void onStateChange(int state) {
            switch (state){
                case SERVER_RUN:
                    setStatus("Сервер работает");
                    break;
                case SERVER_STOP:
                    setStatus("Сервер остановлен");
                    break;
            }
            updateActions();
        }

        @Override
        public void onSocketEvent(int event, Socket socket, String message) {
            switch (event){
                case SOCKET_CONNECT:
                    textOut(socket+" connect");
                    break;
                    
                case SOCKET_DISCONNECT:
                    textOut(socket + " disconnect");
                    break;
                    
                case SOCKET_MESSAGE:
                    
                    if (message.equals("hello")){
                        String json = readData();
                        if(json!=null){
                            send(socket, json);
                        }
                        
                    } else {
                            
                        if ("master".equals(message)){
                            setHeaderValue(socket, "master", "yes");
                        } else {
                            if (hasHeaderValue(socket,"master")){
                                saveData(message);
                                for(Socket s: socketList()){
                                    if(!s.equals(socket)){
                                        send(s, message);
                                    }
                                }
                            }
                        }
                        
                    }
                    textOut(socket+" \""+message+"\"");
                    break;
            }
        }
        
    };

    @Override
    public void onClosing() {
        server.stop();
    }

    @Override
    public void onOpen() {
        try{
            server.start();
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }
    
    
    
    @Override
    public void doCommand(String command) {
        try{
            switch(command){
                case "send":
                    String json = readData();
                    if (json!=null){
                        for(Socket socket:server.socketList()){
                            server.send(socket, json);
                        }
                    }
                case "list":
                    server.list();
                    break;

                case "start":
                    server.start();
                    break;

                case "stop":
                    server.stop();
                    break;

                case "client":
                    new Client().showInFrame(getParent());
                    break;
                    
                case "master":
                    new Master().showInFrame(getParent());
                    break;
            }

        } catch(Exception e){
            showMessage(e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(String command) {
        switch(command){
            case "start":
                return server.isClosed();
            case "stop":
                return !server.isClosed();
        }
        return true;
    }
                        

    public Server() {
        super();
        setPreferredSize(new Dimension(800,300));
        setTitle("Server");
        setCommand("start","stop","message",null,"client","master","list","send");
        updateActions();
        setIcon(Master.createImage(Color.PINK));
    }

    
    public static void main(String[] args){
        
        SwingUtilities.invokeLater(() -> {
            new Server().showInFrame(null);
        });
        
    }
    
}
