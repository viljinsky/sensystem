/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import ru.viljinsky.server.DB_JSON_decoder;
import ru.viljinsky.server.IDB;
import ru.viljinsky.server.ScheduleView;
import ru.viljinsky.server.StatusBar;
import ru.viljinsky.server.ViewControl;


/**
 *
 * @author viljinsky
 */
public class WebSocketFrame2 extends JPanel{
    
    String host = "localhost";
    int port = 3345;
    
    WebSocketClient client = new WebSocketClient(host,port){

        @Override
        void onStopListen() {
            statusBar.setText("stop listen");
        }

        @Override
        void onListen() {
            statusBar.setText("listen...");
        }

        @Override
        void onError(String message) {
            statusBar.setText("ERROR : "+message);
        }

        @Override
        void onMessage(String message) {
            System.out.println("MESSAGE : "+message);
            try{
                statusBar.setText("Загрузка данных...");
                new Thread(){

                    @Override
                    public void run() {
                        try{
                            IDB db = new DB_JSON_decoder(message);
                            view.open(db);
                            view.setDate(new Date());
                            statusBar.setText("Данные успешно загружены");
                        } catch(Exception ex){statusBar.setText(ex.getMessage());}
                    }
                    
                }.start();
            } catch(Exception e){
                System.err.println(e.getMessage());
            }
        }
        
    };
    
    ScheduleView view = new ScheduleView();
    
    ViewControl viewControl = new ViewControl(view);
    
    StatusBar statusBar = new StatusBar();

    public WebSocketFrame2() {
        setLayout(new BorderLayout());
        add(new JScrollPane(view));
        add(viewControl,BorderLayout.PAGE_START);
        add(statusBar,BorderLayout.PAGE_END);
    }
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            t.interrupt();
            try{
                client.by();
                client.close();
                client.socket = null;                                
            } catch(Exception ex){
                statusBar.setText(ex.getMessage());
            }
        }
        
    };
    
    Thread t;
    
    void waitServer(){
        t = new Thread(){

            @Override
            public void run() {
                try{
                    while(!isInterrupted()){
                        client.run();
                        join(1000);
                        System.out.println("next");
                    }
                } catch(InterruptedException e){
                    System.out.println("interapted");
                }
            }
                                    
        };
        t.start();
    }
            
    public void showInFrame(String titel){
        JFrame frame = new JFrame(titel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        waitServer();
        frame.addWindowListener(adapter);
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new WebSocketFrame2().showInFrame("Client");
            }
        });
    }
    
}
