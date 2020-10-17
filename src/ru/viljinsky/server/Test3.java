/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.util.List;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.json.JSONObject;
import ru.viljinsky.project2019.BaseDialog;
import ru.viljinsky.project2019.CommandManager;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Tab;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.data.DataManager;
import ru.viljinsky.project2019.replacement.ReplacementTab;
import ru.viljinsky.project2019.values.ValuesFieldCombo;
import ru.viljinsky.project2019.values.ValuesFieldFile;
import ru.viljinsky.project2019.values.ValuesFieldString;
import ru.viljinsky.project2019.values.ValuesPanel;
import ru.viljinsky.sensystem.TestJSON3;
import ru.viljinsky.tcp.MultipartUtility;

/**
 *
 * @author viljinsky
 */
public class Test3 extends Tab implements CommandManager.CommandListener{
    
    public static final String HOST = "host";
    public static final String PASSWORD = "password";
    public static final String LOGIN = "login";
    public static final String DATA = "data";
    public static final String FORMAT = "format";
    
    public static final int F_TIMETABLER = 1;
    public static final int F_SENSYSTEM = 2;
    
    public void send() throws Exception{
        ValuesPanel valuesPanel = new ValuesPanel();
        valuesPanel.setFields(new ValuesFieldString(HOST));
        valuesPanel.setFields(new ValuesFieldString(LOGIN));
        valuesPanel.setFields(new ValuesFieldString(PASSWORD));
        valuesPanel.setFields(new ValuesFieldFile(DATA));
        Recordset r = new Recordset("id","caption");
        r.add(new Object[]{F_TIMETABLER,"Составитель расписания"});
        r.add(new Object[]{F_SENSYSTEM,"Sensystem"});
        
        valuesPanel.setFields(new ValuesFieldCombo(FORMAT, "id", "caption", r.toValues()));
        valuesPanel.getField(FORMAT).setData(F_TIMETABLER);
        Values values = new Values();
        values.put(HOST, "http://localhost:3345");
        values.put(LOGIN, "admin");
        values.put(PASSWORD, "sensystem");        
        values.put(DATA, DataModel.file);
        valuesPanel.setValues(values);
        if(valuesPanel.showModal(getParent())){
            Values v = valuesPanel.getValues();
            MultipartUtility mu = new MultipartUtility(v.getString(HOST), "utf-8");
            mu.addFormField(LOGIN,v.getString(LOGIN));
            mu.addFormField(PASSWORD, v.getString(PASSWORD));
            Proc.query(con->{
                switch(v.getInteger(FORMAT)){
                    case F_TIMETABLER:
                        mu.addFormField("data", new DB_JSON_encoder(con).toString());
                        break;
                    case F_SENSYSTEM:
                        mu.addFormField("data", new TestJSON3(con).toString());
                        break;
                    default:
                        throw new Exception("bad format");
                }
            });
            List list = mu.finish();
            StringBuilder sb = new StringBuilder();
            for(Object p:list){
                sb.append(p.toString()).append("\n");
            }
            JOptionPane.showMessageDialog(getParent(), sb.toString());
            
        };
    }
    
    static final String OPEN = "open";
    static final String SEND = "send";
    static final String EXIT = "exit";
    static final String START = "start";
    
    CommandManager commandManager = new CommandManager(OPEN,SEND,START,null,EXIT);
    
    ReplacementTab replacementTab = new ReplacementTab();

    public Test3() {
        setLayout(new BorderLayout());
        add(replacementTab);
        add(new StatusBar(),BorderLayout.PAGE_END);
        JMenuBar menuBar= new JMenuBar();
        JMenu menu = new JMenu("File");
        for(Action a: commandManager.getActionList()){
            if (a==null)
                menu.addSeparator();
            else
                menu.add(new JMenuItem(a));
        }
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
        commandManager.addCommandListener(this);
        commandManager.updateActionList();
    }
    static JFrame frame;
    
    public static void main(String[] args){
        System.setProperty("home",".");
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Test3());
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void open(Connection con) throws Exception {
        replacementTab.open(con);
    }
    
    

    @Override
    public void doCommand(String command) {
        try{
            switch(command){
                case START:
                    Runtime.getRuntime().exec("myserver.bat");
                    break;
                case OPEN:
                    DataManager dataManager = new DataManager(this);
                    dataManager.openData();
                    break;
                case SEND:
                    send();
                    break;
                case EXIT:
                    System.exit(0);
                    break;
            }
        } catch(Exception e){
            showErrorMessage(e);
        }
    }

    @Override
    public boolean updateAction(String command) {
        return true;
    }
    
}
