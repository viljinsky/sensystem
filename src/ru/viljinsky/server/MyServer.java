/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import ru.viljinsky.calendars.CalendarView;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.tcp.HttpRequest;
import ru.viljinsky.tcp.HttpServer;

/**
 *
 * @author viljinsky
 */
public class MyServer extends JPanel implements IDataModel{
    
    public static final String SERVER_DATA = "server_data.json";
        
    CalendarView calendarView = new CalendarView(){

        @Override
        public void change() {
            Date date = getSelectionDate();
            if(date!=null){
                view.setDate(date);
            }
        }
        
    };
    
    ScheduleView view = new ScheduleView();
    
    public void open(String json) throws Exception{
        open(new DB_JSON_decoder(json));
    }
    
    public void open(IDB db) throws Exception{
        view.open(db);
        skillFilter.setValues(view.skill);
        curriculumFilter.setValues(view.curriculum);
        Recordset recordset = db.attributes();
        Values values = new Values();
        recordset.stream().forEach((p) -> {
            values.put((String)p[0],p[1]);
        });
        calendarView.setMonth(values.getString(DATE_BEGIN),values.getString(DATE_END));
        calendarView.setPeriod(values.getString(DATE_BEGIN),values.getString(DATE_END));
        calendarView.setSelectionDate(new Date());
        
        Recordset r = db.changes().count(DATE);
        for(Object[] p: r){
            calendarView.setBackground((String)p[0], Color.yellow);
        }
        
    }
    
    void setStatusText(String text){
        view.statusBar.setStatusTest(text);
    }
    
    
    
    HttpServer server = new HttpServer(){


        @Override
        public void onStop(HttpServer server) {
            setStatusText("server stopped");
        }

        @Override
        public void onError(Exception e) {
            setStatusText("server error "+e.getMessage());
        }

        @Override
        public void onStart(HttpServer server) {
//            setStatusText("server started");
        }

        @Override
        public String responce(HttpRequest request) {
            try{
                
                if (request.hasParamByName("data")){
                    open(request.paramByName("data"));                
                    File file=new File(SERVER_DATA);
                    try(
                            FileOutputStream out = new FileOutputStream(file);
                            BufferedWriter write = new BufferedWriter(new OutputStreamWriter(out,"utf-8"));){
                        write.write(request.paramByName("data"));
                    }
                       
                }
                else     
                    return "param data not found";
                setStatusText("Данные обновлены "+new SimpleDateFormat("HH:mm dd MMM yyyy").format(new Date()));
            } catch (Exception e){
                return e.getMessage();
            }
            return "OK";
        }
        
    };

    SkillFilter skillFilter = new SkillFilter(view);
    CurriculumFilter curriculumFilter = new CurriculumFilter(view);
    public MyServer() {
        setPreferredSize(new Dimension(800,600));
        setLayout(new BorderLayout());
        add(calendarView,BorderLayout.WEST);
        add(new JScrollPane(view));      
        add(view.statusBar,BorderLayout.PAGE_END);
        add(view.title,BorderLayout.PAGE_START);
        view.title.add(new ViewControl(view));
        view.title.add(skillFilter);
//        view.title.add(curriculumFilter);
    }
    
    public void showInFrame(){
        
        try{
        File file = new File(SERVER_DATA);
        if (file.exists()){
            IDB db = new DB_JSON_decoder(file);
            open(db);
            setStatusText("Загружены локальные данные");
        }
        } catch (Exception e){
        }
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        new Thread(){

            @Override
            public void run() {
                try{
                    server.start(3345);
                } catch (Exception e){
                    
                }
            }
            
        }.start();
    }
    
    
    public static void main(String[] args) throws Exception{
        
        SwingUtilities.invokeLater(() -> {
            new MyServer().showInFrame();
        });
        
       
       
    }
    
    
    
}
