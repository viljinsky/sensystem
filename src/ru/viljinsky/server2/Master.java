/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.json.JSONObject;
import ru.viljinsky.project2019.CommandManager;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.replacement.ReplacementTab;
import ru.viljinsky.server.DB_JSON_encoder;
import ru.viljinsky.server.StatusBar;
import ru.viljinsky.websocket.WebSocketClient;



/**
 *
 * @author viljinsky
 */
public class Master extends JPanel{
        
    ClientConnection cc = new ClientConnection();
            
    public void send(String message) throws Exception{
        
        new Thread(){

            @Override
            public void run() {
                statusBar.setText("Отправка...");
                try{
                    WebSocketClient client = new WebSocketClient(cc.host,cc.port);
                    client.open();
                    client.send("master",message,"by");
                    client.close();
                    
                } catch(Exception e){
                    showMessage(e.getMessage());
                } finally {
                    statusBar.setText("Отправка завершена");
                }
                
            }
            
        }.start();
        
        
        
    }
    
    public static final String FILE_OPEN = "open";
    public static final String FILE_CLOSE = "close";
    public static final String SEND = "send";
    public static final String EXIT = "exit";
    
    String result = "";
    
    String getMessage() throws Exception{
        Proc.query(con->{
            JSONObject obj = new DB_JSON_encoder(con);
            result = obj.toString();            
        });
        return result;
    }
    
    CommandManager commandManager = new CommandManager(FILE_OPEN,FILE_CLOSE,null,SEND,EXIT){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case EXIT:
                        System.exit(0);
                    case SEND:
                        if (cc.config(getParent()))
                            send(getMessage());
                        break;
                }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
    ReplacementTab replacementTab = new ReplacementTab();
    StatusBar statusBar = new StatusBar();

    public Master() {
        setLayout(new BorderLayout());
        add(replacementTab);
        add(statusBar,BorderLayout.PAGE_END);
        replacementTab.setVisible(false);
    }
    
    WindowListener adapter = new WindowAdapter() {

        @Override
        public void windowClosing(WindowEvent e) {
            onClosed();
        }

        @Override
        public void windowOpened(WindowEvent e) {
            onOpen();
        }
        
    };
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    public void onOpen(){
        try{
            DataModel.setConnection("моё расписание.db");
            Proc.query(con->{
                replacementTab.open(con);
                replacementTab.setVisible(true);
            });
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }
    
    public void onClosed(){
        replacementTab.setVisible(false);
    }
    
    JMenu getMenu1(){
        JMenu menu = new JMenu("File");
        for(Action a: commandManager.getActionList()){
            if (a == null)
                menu.addSeparator();
            else
                menu.add(a);
        }
        return menu;
    }
    
    JMenu getMenu2(){
        JMenu menu = new JMenu("Send");
        return menu;
    }
    
    public static Image createImage(Color color){
        BufferedImage image = new BufferedImage(24,24,BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        g.setColor(color);
        g.fillOval(0,0,24,24);
        g.setColor(Color.LIGHT_GRAY);
        g.drawOval(0,0,24,24);
        return image;
    }
    
    public void showInFrame(Component parent){
        JFrame frame = new JFrame("Изменения");                
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setIconImage(createImage(Color.CYAN));
        
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(getMenu1());
        menuBar.add(getMenu2());
        frame.setJMenuBar(menuBar);
        frame.setContentPane(this);
        
        frame.addWindowListener(adapter);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(parent);
        frame.setVisible(true);
        
    }
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            new Master().showInFrame(null);
        });
    }
    
}
