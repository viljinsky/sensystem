/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import ru.viljinsky.server.DB_JSON_decoder;
import ru.viljinsky.server.DateControl;
import ru.viljinsky.server.FilterPanel;
import ru.viljinsky.server.IDB;
import ru.viljinsky.server.ScheduleView;
import ru.viljinsky.server.ViewControl;
import ru.viljinsky.websocket.WebSocketClient;



/**
 *
 * @author viljinsky
 */
public class Client extends JPanel{
    
    ClientConnection cc = new ClientConnection();
    
    ScheduleView view = new ScheduleView();
    
    ClientStatusBar statusBar = new ClientStatusBar(){
        
        @Override
        public void doCommand(String command) {
            try{
            switch (command){
                case "config":
                    cc.config(getParent());
                    break;
                case "start":
                    start();
                    break;
                case "stop":
                    stop();
                    break;
                case "reload":
                    reload();
                    break;
                    
            }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
//    void config() throws Exception{
//        ValuesPanel valuesPanel = new ValuesPanel();
//        valuesPanel.setFields(new ValuesField[]{
//            new ValuesFieldString("host"),
//            new ValuesFieldInteger("port")
//        });
//        valuesPanel.setValues(getConnection());
//        if (valuesPanel.showModal(getParent())){
//            setConnection(valuesPanel.getValues());
//        }
//      
//    }
    
    void start() throws Exception{
        if (client==null){
            client = new ScheduleClient(cc.host, cc.port);
            client.start();
        }
    }
    void stop() throws Exception{
        if (client!=null){
            client.stop();
            client=null;
        }
    }
    void reload() throws Exception{
        if (client!=null && client.isConected()){
            client.send("hello");
        }
    }
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    
    class ScheduleClient extends WebSocketClient{

        public ScheduleClient(String host, int port) {
            super(host, port);
        }

        @Override
        public void onStateChange(int state) {
            switch(state){
                case READY:
                    statusBar.setText("Соединение установлено.");
                    
                    try{
                        client.send("hello");
                    } catch(Exception e){
                        showMessage(e.getMessage());
                    }
                    break;
                    
                case WAIT:
                    statusBar.setText("Ожидание соединения...");
                    setTitle("client ["+cc.host+"]");
                    break;
                    
                case CLOSED:
                    statusBar.setText("Соединение разорвано.");
                    setTitle("client");
                    break;
            }
        }
        
        

        @Override
        public void onSocketEvent(int event, String message) {
            try{
                switch (event){
                    case SOCKET_READ_DATA:
                        setVisible(false);
                        new Thread(){

                            @Override
                            public void run() {
                                try{
                                    IDB db = new DB_JSON_decoder(message);
                                    view.open(db);
                                    view.setDate(new Date());
                                } catch(Exception e){
                                } finally{
                                    setVisible(true);
                                }
                            }
                            
                        }.start();
                }
            } catch (Exception e){
                showMessage(e.getMessage());
            }
        }
                
    }
    
    ScheduleClient client; 
    
    static class FrameHeader extends JPanel{

        public FrameHeader() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(new EtchedBorder());
        }
        
    }
    
    public Client() {
        setLayout(new BorderLayout());
        add(new JScrollPane(view));
        ViewControl control = new ViewControl(view);
        JPanel panel2 = new FrameHeader();
        panel2.add(control);
        panel2.add(new FilterPanel(view));
        add(panel2,BorderLayout.PAGE_START);
        
        JPanel panel = new FrameHeader();
        panel.add(new DateControl(){

            @Override
            public void change() {
                view.setDate(getDate());
            }
            
        });
        panel.add(statusBar);
        
        add(panel,BorderLayout.PAGE_END);
    }
    
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            try{
            if (client!=null){ // && client.isConected()){
                client.stop();
                client=null;
            }
            } catch(Exception s){
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
            client = new ScheduleClient(cc.host, cc.port);
            client.start();
        }
        
    };
    
    String title = "client";
    public void setTitle(String title){
        this.title = title;
        if(frame!=null){
            frame.setTitle(title);
        }
    }
    
    JFrame frame;
    public void showInFrame(Component parent){  
        frame = new JFrame("Client");
        frame.setIconImage(Master.createImage(Color.yellow));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        frame.setContentPane(this);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            new Client().showInFrame(null);
        });
       
    }
    
}
