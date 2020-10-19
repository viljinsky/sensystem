/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.awt.BorderLayout;
import java.util.HashMap;
import javax.management.remote.JMXConnectorFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.json.JSONObject;
import ru.viljinsky.server.DB_JSON_decoder;
import ru.viljinsky.server.IDB;
import ru.viljinsky.server.ScheduleView;
import ru.viljinsky.tcp.HttpClient;
import ru.viljinsky.tcp.HttpResponce;

/**
 *
 * @author viljinsky
 */
public class TestClient2 extends JPanel{
    ScheduleView scheuleView=new ScheduleView();

    public TestClient2() {
        setLayout(new BorderLayout());
        add(new JScrollPane(scheuleView));
        load();        
    }
    
    void load(){
        try{
            HttpClient client = new HttpClient("http://localhost:3345/page1");
            HttpResponce responce = client.get(new HashMap());
            if (responce.getCode() == HttpResponce.RESULT_OK){
                JSONObject obj = new JSONObject(responce.getText());
            } else {
                JOptionPane.showMessageDialog(getParent(), responce.getText());
            }
            
            
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args){
        TestClient2 panel = new TestClient2();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
                
    }
    
}
