/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.json.JSONObject;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.StatusBar;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.sensystem.ValuesPanel;
import static ru.viljinsky.server.MyServer.SERVER_DATA;
import ru.viljinsky.tcp.HttpClient;
import ru.viljinsky.tcp.HttpResponce;


class TeacherFilter extends JComponent implements IDataModel{

    String displayName = LAST_NAME;
    Object keyName= TEACHER_ID;
    ScheduleView view;
    
    public TeacherFilter(ScheduleView view) {
        this.view = view;
        setLayout(new FlowLayout());
    }
    
    JButton createButtom(Values values){
        JButton button = new JButton(values.getString(LAST_NAME));
        return button;
    }
    public void setValues(Recordset recordset){
        removeAll();
        for(Iterator<Values> it = recordset.getIterator();it.hasNext();){
            add(createButtom(it.next()));
        }
    }
}



/**
 *
 * @author viljinsky
 */
public class LocalClient extends JPanel implements IDataModel{
    
    String host = "http://localhost:3345/page1";
    String login = "admin";
    String password = "sensystem";
    
    
    static final String RELOAD  = "reload";
    static final String CONNECT = "connect";
    
   
    
    ScheduleView scheduleView=new ScheduleView();
    SkillFilter skillFilter = new SkillFilter(scheduleView);
    TeacherFilter teacherFilter = new TeacherFilter(scheduleView);
    ViewControl viewControl = new ViewControl(scheduleView);
    
    StatusBar statusBar = new StatusBar();
    CommandBar commandBar = new CommandBar(RELOAD,CONNECT){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case CONNECT:
                        connect();
                        break;
                    case RELOAD:
                        reload1();
                        break;
                }
            } catch (Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };

    public LocalClient() {
        setLayout(new BorderLayout());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(scheduleView));        
        panel.add(teacherFilter,BorderLayout.PAGE_START);
        panel.add(viewControl,BorderLayout.PAGE_END);
        add(panel);
        add(statusBar,BorderLayout.PAGE_END);
        add(commandBar,BorderLayout.PAGE_START);
//        load();        
    }
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    
    class Buttons extends JPanel implements ActionListener{
        JDialog dialog;
        
        int modalResult = 0;

        public Buttons(JDialog dialog) {
            this.dialog = dialog;
            setLayout(new FlowLayout(FlowLayout.RIGHT));
            JButton button = new JButton("Готово");
            button.addActionListener(this);
            add(button);
            button = new JButton("Отмена");
            button.addActionListener(this);
            add(button);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
        }
        
    }
    
    void connect() throws Exception{
        JDialog dialog = new JDialog();
        
        ValuesPanel valuesPanel = new ValuesPanel();
        Map map = new HashMap();
        map.put("host",host);
        map.put("login",login);
        map.put("password", password);
        
        valuesPanel.setValues(map);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(500,200));
        panel.add(valuesPanel,BorderLayout.PAGE_START);
        panel.add(new Buttons(dialog),BorderLayout.PAGE_END);
        
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(getParent());
        dialog.setVisible(true);
        
        host = valuesPanel.getString("host");
        login = valuesPanel.getString("login");
        password = valuesPanel.getString("password");
        
        reload1();
        
    }
    
    Map auth(){
        HashMap map = new HashMap();
        map.put("login", login);
        map.put("password", password);
        return map;
    }
    
    void reload() throws Exception{
        Date date = scheduleView.getDate();
        HttpClient client = new HttpClient(host);
        HttpResponce responce = client.post(auth());
        if (responce.getCode()== HttpResponce.RESULT_OK){
            IDB db = new DB_JSON_decoder(responce.getText());
            scheduleView.open(db);
            scheduleView.setDate(date);
//            skillFilter.setValues(db.skill().select(SKILL_ID,SKILL_NAME));
            teacherFilter.setValues(db.teacher());
        } else {
            showMessage(responce.toString());
        }
        
    }
    
    void reload1(){
        statusBar.setStatusText("Загрузка данных...");
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        new Thread(){

            @Override
            public void run() {
                try{
                    reload();
                    statusBar.setStatusText("Готово");
                } catch (Exception e){
                    statusBar.setStatusText("Ошибка при загрузке");
                } finally{
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            
        }.start();
    }
    
    void load(){
        try{
            IDB db = null;
            Date date = new Date();
            
            HttpClient client = new HttpClient(host);
            HttpResponce responce = client.get(new HashMap());
            if (responce.getCode() == HttpResponce.RESULT_OK){
                JSONObject obj = new JSONObject(URLDecoder.decode(responce.getText(),"utf-8"));
                db = new DB_JSON_decoder(obj);
            } else {
                File file = new File(SERVER_DATA);
                if (file.exists()){
                    db = new DB_JSON_decoder(file);
                }
            }
            
            if (db!=null){
                scheduleView.open(db);
                scheduleView.setDate(date);
            }
                       
        } catch(Exception e){
            showMessage(e.getMessage());
        }
    }
    
    public static void main(String[] args){
        LocalClient panel = new LocalClient();
        JFrame frame = new JFrame("Составитель расписания (клиент)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.reload1();
                
    }
    
}
