/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ru.viljinsky.server.MessagePane;
import ru.viljinsky.tcp.HttpClient;
import ru.viljinsky.tcp.HttpResponce;

/**
 *
 * @author viljinsky
 */
public class TestClient3 extends JPanel {
    MessagePane messagePane = new MessagePane();

    public TestClient3() {
        setLayout(new BorderLayout());
        add(new JScrollPane(messagePane));
        
    }
    
    public void load() throws Exception{
        String host = "http://localhost:3345/page1";
        HttpClient client = new HttpClient(host);
        Map map = new HashMap();
        HttpResponce responce = client.get(map);
        System.out.println(responce.length());
        messagePane.textOut(responce.getText());
        
    }
    
    public static void main(String[] args) throws Exception{
        TestClient3 panel = new TestClient3();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.load();
    }
    
}
