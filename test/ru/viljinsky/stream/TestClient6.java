/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URLDecoder;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import ru.viljinsky.server.MessagePane;
import ru.viljinsky.tcp.HttpClient;
import ru.viljinsky.tcp.HttpResponce;

/**
 *
 * @author viljinsky
 */
public class TestClient6 extends JPanel{
    
    void send(String message) throws Exception{
        HttpClient client = new HttpClient(message);
        HttpResponce responce = client.get();
        if (responce.getCode()==HttpResponce.RESULT_OK){
            messagePane.textOut(URLDecoder.decode(responce.getText(),"utf-8"));
        }
    }
    
    class NavBar extends JPanel implements ActionListener{
//        JTextField textField = new JTextField("http://localhost:3345/test.html");
        JTextField textField = new JTextField("http://localhost:3345/server_data.json");
        JButton button = new JButton("send");

        public NavBar() {
            setLayout(new BorderLayout());
            add(textField);
            add(button,BorderLayout.EAST);
            button.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try{
                send(textField.getText());
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
        
    }
    
    MessagePane messagePane = new MessagePane();
    NavBar navBar = new NavBar();

    public TestClient6() {
        setLayout(new BorderLayout());
        add(navBar,BorderLayout.PAGE_START);
        add(new JScrollPane(messagePane));
    }
    
    public static void main(String[] args){
        TestClient6 panel = new TestClient6();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
}
