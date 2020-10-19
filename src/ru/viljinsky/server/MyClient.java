/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.json.JSONObject;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Tab;
import ru.viljinsky.project2019.replacement.ReplacementTab;
import ru.viljinsky.sensystem.Client;
import ru.viljinsky.tcp.MultipartUtility;


/**
 *
 * @author viljinsky
 */
public class MyClient extends JPanel implements IDataModel{
    
    
    public static final String OPEN = "open";
    public static final String SEND = "send";
    public static final String TEST = "test";
    
    
    public void open() throws Exception{
        DataModel.setConnection(params.getString(Client.SOURCE));
        Proc.query(con->{
            tab.open(con);
        });        
    }
    
    String data = "";
    String getData() throws Exception{
        Proc.query(con->{
//            JSONObject json = new DB_JSON_encoder(con);
//            JSONObject json = new TestJSON3(con);
            JSONObject json = new DB_JSON_encoder(con);
            data = json.toString();
        });
        return data;
    }
    
    void textOut(String str){
        System.out.print(str);
    }
    
    public void send() throws Exception{
        
        
        MultipartUtility mu = new MultipartUtility(params.getString(Client.HOST),Client.CHARSET);
        mu.addFormField(Client.PASSWORD, params.getString(Client.PASSWORD));
        mu.addFormField(Client.LOGIN, params.getString(Client.LOGIN));
        mu.addFormField(Client.DATA, getData());
        ArrayList list = mu.finish();
        list.stream().forEach((s) -> {
            textOut((String)s+"\n");
        });
        
    }
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    CommandBar commandBar = new CommandBar(OPEN,SEND,TEST){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case SEND:
                        send();
                        break;
                    case OPEN:
                        open();
                        break;
                }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
    Tab tab = new ReplacementTab();
    
    Client.ConnectionParams params = new Client.ConnectionParams();

    public MyClient() {
        setLayout(new BorderLayout() );
        add(tab);
        add(params,BorderLayout.PAGE_END);
        add(commandBar,BorderLayout.PAGE_START);
    }
    
    public static void main(String[] args) throws Exception{
        
        MyClient p = new MyClient();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(p);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        p.open();
        
    }
    
    
    
}
