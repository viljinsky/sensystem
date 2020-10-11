/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.sensystem;

import ru.viljinsky.server.DB_JSON_encoder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.replacement.TestDocument;
import ru.viljinsky.tcp.CommandBar;
import ru.viljinsky.tcp.MainHTTP;
import ru.viljinsky.tcp.MessagePane;
import ru.viljinsky.tcp.MultipartUtility;

/**
 *
 * @author viljinsky
 */
public class Client extends JPanel{
    
    public static final String SEND = "send";
    public static final String TEST = "json";
    public static final String CLEAR = "clear";
    public static final String SAVE = "save";
    public static final String FORMAT1 = "FORMAT1";
    public static final String FORMAT2 = "FORMAT2";
    public static final String REPACEMENT = "replacement";
    public static final String START = "start";
    
    static final String CHARSET = "utf-8";
    
    public static final String HOST = "host";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String SOURCE = "source";
    public static final String DATA = "data";
            
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    
    public static final int F1 = 1;
    public static final int F2 = 2;
    public int format = F1;
    
    String json() throws Exception{
        DataModel.setConnection(params.getString(SOURCE));
        try(Connection con = DataModel.getConnection();){
            switch(format){
                case F1:
                    return  new TestJSON3(con).toString();
                case F2:
                    return new DB_JSON_encoder(con).toString();
                default:
                    throw new Exception("Указан вневерный парсер");
            }
                        
        }        
    }
    
    void send(String data) throws Exception{
        MultipartUtility mu = new MultipartUtility(params.getString(HOST),CHARSET);
        mu.addFormField(PASSWORD, params.getString(PASSWORD));
        mu.addFormField(LOGIN, params.getString(LOGIN));
        mu.addFormField(DATA, data);
        ArrayList list = mu.finish();
        list.stream().forEach((s) -> {
            textOut((String)s+"\n");
        });
    }
    
    void test() throws Exception{
        textOut(json());
    }
    
    void save() throws Exception{
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("messagepane.txt"));
        fileChooser.setCurrentDirectory(new File("."));
        int retVal = fileChooser.showSaveDialog(getParent());
        if (retVal == JFileChooser.APPROVE_OPTION){
            messagePane.saveToFile(fileChooser.getSelectedFile());
            showMessage("save has been successfully");
        }
    }
    
    void replacement() throws Exception{
        
        DataModel.setConnection(params.getString(SOURCE));
        TestDocument replacemetTab = new TestDocument();
        replacemetTab.showInFrame();
        Proc.query(con->{
            replacemetTab.open(con);
        });
        
    }
    
    JFrame serverFrame;
    
    void start_server() throws Exception{
        if (serverFrame==null){
            serverFrame = new MainHTTP().start();
        }
    }
    
    public void doCommand(String command){
        try{
            switch(command){
                case START:
                    start_server();
                    break;
                case SAVE:
                    save();
                    break;
                case CLEAR:
                    messagePane.clear();
                    break;
                case TEST:
                    test();
                    break;
                case SEND :
                    send(json());
                    break;
                case FORMAT1:
                    format = F1;
                    break;
                case FORMAT2:
                    format = F2;
                    break;
                case REPACEMENT:
                    replacement();
                    break;
            }
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }
    
    class ConnectionParams extends ValuesPanel{
        

        public ConnectionParams() {
            addField(HOST,"http://localhost:3345");
            addField(LOGIN,"admin");
            addField(PASSWORD,"sensystem");
            addField(SOURCE,"моё расписание.db");
        }
    }
    
    MessagePane messagePane = new MessagePane();
    CommandBar commandBar = new CommandBar(TEST,SEND,SAVE,CLEAR,null,START,REPACEMENT,null){

        @Override
        public void doCommand(String command) {
            Client.this.doCommand(command);
        }
        
    };
    
    void textOut(String txt){
        messagePane.textOut(txt);
    }
    ConnectionParams params = new ConnectionParams();

    public Client() {
        setPreferredSize(new Dimension(800,600));
        setLayout(new BorderLayout());
        add(new JScrollPane(messagePane));
        add(commandBar,BorderLayout.PAGE_START);
        add(params,BorderLayout.PAGE_END);
        
        commandBar.addSeparator();
        ButtonGroup group = new ButtonGroup();
        group.add(commandBar.addGroupCommand(FORMAT1,format == F1));
        group.add(commandBar.addGroupCommand(FORMAT2,format == F2));
        

        
    }
    
    static Client panel;
    public static void main(String[] args ){
        panel= new Client();
        panel.params.readValues("params");
        JFrame frame = new JFrame("sensystem client");
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                panel.params.saveValues("params");
            }
            
        });
        frame.setMaximumSize(new Dimension(800,600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    } 
    
    
}
