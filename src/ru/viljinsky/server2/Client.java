/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import ru.viljinsky.server.DB_JSON_decoder;
import ru.viljinsky.server.IDB;
import ru.viljinsky.server.ScheduleView;
import ru.viljinsky.server.StatusBar;
import ru.viljinsky.server.ViewControl;
import ru.viljinsky.websocket.WebSocketClient;

/**
 *
 * @author viljinsky
 */
public class Client extends JPanel{
    
    ScheduleView view = new ScheduleView();
    StatusBar statusBar = new StatusBar();
    
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
         //               client.send("giveme");
                    } catch(Exception e){
                        showMessage(e.getMessage());
                    }
                    break;
                    
                case WAIT:
                    statusBar.setText("Ожидание соединения...");
                    break;
                    
                case CLOSED:
                    statusBar.setText("Соединение разорвано.");
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
    
    public Client() {
        setLayout(new BorderLayout());
        add(new JScrollPane(view));
        ViewControl control = new ViewControl(view);
        add(control,BorderLayout.PAGE_START);
        add(statusBar,BorderLayout.PAGE_END);
    }
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            if (client!=null && client.isConected()){
                client.close();
            }
        }

        @Override
        public void windowOpened(WindowEvent e) {
            client = new ScheduleClient("localhost", 3345);
            client.start();
        }
        
    };
    
    public void showInFrame(Component parent){  
        JFrame frame = new JFrame("Client");
        if (parent==null)
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        else
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
