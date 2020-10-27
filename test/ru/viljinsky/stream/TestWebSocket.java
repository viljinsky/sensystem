/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import ru.viljinsky.websocket.WebSocketPanel;
import java.awt.Dimension;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import ru.viljinsky.websocket.WebSocketClient;
import ru.viljinsky.websocket.WebSocketServer;

class TestClient2 extends WebSocketPanel{

    private class Client extends WebSocketClient{

        public Client(String host, int port) {
            super(host, port);
        }
        
        @Override
        public void onStateChange(int state) {
            switch (state){
                case READY:
                    setStatus("reday");
                    break;
                case CLOSED:
                    setStatus("closed");
                    break;
                case WAIT:
                    setStatus("wait");
                    break;
            }   
            updateActions();
        }

        @Override
        public void onSocketEvent(int event, String message) {
            switch(event){
                case SOCKET_SEND_DATA:
                    textOut("send : "+message);
                    break;
                case SOCKET_READ_DATA:
                    textOut("read :"+message);
                    break;
                            
            }
        }
        
    }
        
    WebSocketClient client;
    
    @Override
    public void onClosing() {
        if (client!=null) client.stop();
    }

    @Override
    public void onOpen() {
        client = new Client("localhost",3345);    
        client.start();
    }

    @Override
    public boolean isEnabled(String command) {
        switch(command){
            case "open":return client.isClosed();
            case "close": return client.isConected();
            case "message": return client.isConected();
            default: return true;
        }
    }
    
    @Override
    public void doCommand(String command) {
        try{
            switch(command){
                case "master":
                    client.send("master");
                    break;
                case "open":
                    client.start();
                    break;
                case "close":
                    client.stop();
                    break;
                case "message":
                    if (client.isConected())
                        client.send("OK");
                    break;
            }
        } catch(Exception e){
            showMessage(e.getMessage());
        }
    }
        
    public TestClient2() {
        super();
        setTitle("Client");
        setCommand("open","close",null,"message","master");
    }

}

/**
 *
 * @author viljinsky
 */
public class TestWebSocket extends WebSocketPanel{
    
    WebSocketServer server = new WebSocketServer() {

        @Override
        public void onMassage(String message) {
            textOut(message);
        }

        @Override
        public void onStateChange(int state) {
            switch (state){
                case SERVER_RUN:
                    setStatus("server start");
                    break;
                case SERVER_STOP:
                    setStatus("server stop");
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
                    
//                    Map header = (HashMap)getHeaders(socket);
                    
                    if ("master".equals(message)){
                        setHeaderValue(socket, "master", "yes");
                    } else {
                        if (hasHeaderValue(socket,"master")){
                            for(Socket s: socketList()){
                                if(!s.equals(socket)){
                                    send(s, message);
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
        public void doCommand(String command) {
            try{
                switch(command){
                    case "list":
                        server.list();
                        break;
                    case "start":
                        server.start();
                        break;
                        
                    case "stop":
                        server.stop();
                        break;
                        
                    case "message":
                        server.sendToAll("hello gay");
                        break;
                        
                    case "test":
                        new TestClient2().showInFrame(getParent());
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
                case "message":
                case "stop":
                    return !server.isClosed();
            }
            return true;
        }
                        

    public TestWebSocket() {
        super();
        setPreferredSize(new Dimension(800,600));
        setTitle("Server");
        setCommand("start","stop","message",null,"test","main","list");
        updateActions();
    }
    
    
    public static void main(String[] args){
        
        SwingUtilities.invokeLater(() -> {
            new TestWebSocket().showInFrame(null);
        });
        
    }
    
}
