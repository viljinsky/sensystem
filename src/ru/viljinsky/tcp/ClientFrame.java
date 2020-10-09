/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;



/**
 *
 * @author viljinsky
 */
public class ClientFrame extends JPanel{
    
    JTextField query = new JTextField();
    MessagePane messagePane = new MessagePane();
    TcpClient client;
    
    void sendMessage(){
        sendMessage(query.getText());            
    } 
    
    void sendMessage(String message){
        try{
            String s = client.sendMessage(message);
            query.setText("");
            messagePane.textOut("responce : "+s+"\n");
        } catch (Exception e){
            messagePane.textOut("send error!!! "+e.getMessage()+"\n");
        }
        
    }
    
    Action aRequest = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    };

    public ClientFrame() {
        setPreferredSize(new Dimension(300,400));
        setLayout(new BorderLayout());
        add(query,BorderLayout.PAGE_START);
        add(messagePane);
        
        ActionMap am = query.getActionMap();
        am.put("request", aRequest);
        query.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "request");
    }

    JFrame frame;
    public void showModal(Component parent){
        frame = new JFrame("TcpClient");
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                try{
                    if(client!=null){
                        sendMessage(".");
                        client.close();
                        client = null;
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                frame.setVisible(false);
                frame.dispose();
                frame= null; 
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("window closed");
            }
            
            

            @Override
            public void windowOpened(WindowEvent e) {                
                System.out.println("window opened");
                try{
                    client = new TcpClient("localhost", 3345);
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            
        });
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setContentPane(this);
        frame.pack();
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        
    }
    
    
}
