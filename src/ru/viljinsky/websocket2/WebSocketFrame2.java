/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.websocket2;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import ru.viljinsky.server.DB_JSON_decoder;
import ru.viljinsky.server.IDB;
import ru.viljinsky.server.ScheduleView;
import ru.viljinsky.server.ViewControl;

/**
 *
 * @author viljinsky
 */
public class WebSocketFrame2 extends JPanel{
    
    WebSocketClient client = new WebSocketClient("localhost",3345){

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
    JLabel statusBar = new JLabel("status bar");

    public WebSocketFrame2() {
        setLayout(new BorderLayout());
        add(new JScrollPane(view));
        add(viewControl,BorderLayout.PAGE_START);
        statusBar.setBorder(new EmptyBorder(12,6,12,16));
        add(statusBar,BorderLayout.PAGE_END);
    }
    
    WindowAdapter windowAdapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            try{
                client.by();
            } catch(Exception ex){
                statusBar.setText(ex.getMessage());
            }
            client.close();
        }
        
    };
    
    public void showInFrame(String titel){
        JFrame frame = new JFrame(titel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(this);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(windowAdapter);
        frame.setVisible(true);
        client.run();
    }
    
    public static void main(String[] args){
        WebSocketFrame2 frame = new WebSocketFrame2();
        frame.showInFrame("Составитель расписания (клиент)");
    }
    
}
