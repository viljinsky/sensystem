/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.json.JSONObject;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Tab;
import ru.viljinsky.project2019.replacement.ReplacementTab2;
import ru.viljinsky.sensystem.Client;
import static ru.viljinsky.sensystem.Client.HOST;
import ru.viljinsky.tcp.CommandBar;
import ru.viljinsky.tcp.MultipartUtility;


/**
 *
 * @author viljinsky
 */
public class Test2 extends JPanel implements IDataModel{
    
    class TestPanel extends JPanel{
        Grid grid = new Grid();
        public TestPanel() {
            setLayout(new BorderLayout());
            add(new JScrollPane(grid));
        }
        
    }
    
    TestPanel testPanel = new TestPanel();
    
    public static final String OPEN = "open";
    public static final String SEND = "send";
    public static final String TEST = "test";
    
    void test() throws Exception{
        Proc.query(con->{
            Recordset r = new Recordset2(con);
            testPanel.grid.setRecordset(r);
//            r.print();
        });
    }
    
    public void open() throws Exception{
        DataModel.setConnection(params.getString(Client.SOURCE));
        Proc.query(con->{
            tab.open(con);
        });
        
        JFrame frame = new JFrame();
        frame.setContentPane(testPanel);
        frame.pack();
        frame.setVisible(true);
    }
    
    String data = "";
    String getData() throws Exception{
        Proc.query(con->{
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
                    case TEST:
                        test();
                        break;
                }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    
    Tab tab = new ReplacementTab2();
    Client.ConnectionParams params = new Client.ConnectionParams();

    public Test2() {
        setLayout(new BorderLayout() );
        add(tab);
        add(params,BorderLayout.PAGE_END);
        add(commandBar,BorderLayout.PAGE_START);
    }
    
    static class Recordset2 extends Recordset{

        public Recordset2(Connection con) throws Exception{
            String sql = "select a.date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.subject_id,a.teacher_id,a.room_id,\n" 
                    +  "b.flag,a.journal_id,a.detail_id,\n"
                    + "null as new_subject_id,null as new_teacher_id,null as new_room_id\n"
                    + "from journal_detail a inner join journal b using(journal_id)";
            Recordset source = DataModel.query(con, sql);
            Map<Integer,Object> map = new HashMap();
            map.put(source.columnIndex(DATE), null);
            map.put(source.columnIndex(DAY_ID), null);
            map.put(source.columnIndex(BELL_ID), null);
            map.put(source.columnIndex(DEPART_ID), null);
            map.put(source.columnIndex(GROUP_ID), null);
//            map.put(source.columnIndex(SUBJECT_ID), null);
            
            int iRoom = source.columnIndex(ROOM_ID);
            int iNewRoom = source.columnIndex("new_room_id");
            int iSubject = source.columnIndex(SUBJECT_ID);
            int iNewSubject = source.columnIndex("new_subject_id");
            int iTeacher = source.columnIndex(TEACHER_ID);
            int iNewTeacher = source.columnIndex("new_teacher_id");
            int iFlag = source.columnIndex(FLAG);
                    
            
            columns = source.columns;
            
            while(!source.isEmpty()){
                Object[] p = source.remove(0);
                Map<Integer,Object> map2 = new HashMap<>();
                for(Integer n: map.keySet()){
                    map2.put(n, p[n]);
                }
                Recordset r = source.find(map2);
                if (r.isEmpty()){
                    int flag = (Integer)p[iFlag];
                    if (flag == 3){
                        p[iNewRoom] = p[iRoom];
                        p[iNewSubject] = p[iSubject];
                        p[iNewTeacher] = p[iTeacher];
                    }
                    add(p);                    
                } else {
                    Object[] t = r.get(r.size()-1);
                    p[iFlag]=t[iFlag];
                    p[iNewRoom] = t[iRoom];
                    p[iNewSubject] = t[iSubject];
                    p[iNewTeacher] = t[iTeacher];
                    add(p);
                    source.removeAll(r);
                }
            }
        }
        
    }
    
    public static void main(String[] args) throws Exception{
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Test2());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
    }
    
    
    
}
