/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import ru.viljinsky.websocket.WebSocketPanel;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;
import ru.viljinsky.server.StatusBar;
import ru.viljinsky.websocket.WebSocketClient;
import ru.viljinsky.websocket.WebSocketServer;

class TestClient2 extends WebSocketPanel{

    @Override
    public void onClosing() {
        if (client!=null) client.stop();
    }
            
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
        public void onMessage(String message) {
            textOut(message+"\n");
        }
        
    }
        
    WebSocketClient client;

    public TestClient2() {
        super();
        client = new Client("localhost",3345);    
        setCommand("open","close",null,"message");
    }

    @Override
    public boolean isEnabled(String command) {
        switch(command){
            case "open":return client.isClosed();
            case "close": return client.isConected();
            case "message": return client.isClosed();
            default: return true;
        }
    }
    
    @Override
    public void doCommand(String command) {
        try{
            switch(command){
                case "open":
                    client.start();
                    break;
                case "close":
                    client.stop();
                    break;
                case "message":
                    client.send("OK");
                    break;
            }
        } catch(Exception e){
            showMessage(e.getMessage());
        }
    }
        
}

/**
 *
 * @author viljinsky
 */
public class TestWebSocket extends JPanel{
    
    WebSocketServer server = new WebSocketServer() {

        @Override
        public void onMassage(String message) {
            textOut(message);
        }

        @Override
        public void onStateChange(int state) {
            switch (state){
                case SERVER_RUN:
                    setStatusText("server start");
                    break;
                case SERVER_STOP:
                    setStatusText("server stop");
                    break;
            }
            commandBar.updateActions();
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
                    textOut(socket+" "+message);
                    break;
            }
        }
        
    };
    
    
    MessagePane messagePane = new MessagePane();
    CommandBar commandBar = new CommandBar("start","stop","message",null,"test","main"){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
//                    case "main":
//                        new MainClient().showInFrame(getParent());
//                        break;
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
                        
    };
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    void setStatusText(String message){
        statusBar.setText(message);
    }
    
    void textOut(String message){
        messagePane.textOut(message+"\n");
    }
    
    StatusBar statusBar = new StatusBar();

    public TestWebSocket() {
        setLayout(new BorderLayout());
        add(messagePane);
        add(commandBar,BorderLayout.PAGE_START);
        add(statusBar,BorderLayout.PAGE_END);
        commandBar.updateActions();       
    }
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            server.stop();
        }
        
    };
    public void showInFrame(){
        JFrame frame = new JFrame("server");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        
        SwingUtilities.invokeLater(() -> {
            new TestWebSocket().showInFrame();
        });
        
    }
    
}
