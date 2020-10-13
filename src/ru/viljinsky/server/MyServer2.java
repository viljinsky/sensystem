/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
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
public class MyServer2 extends JPanel implements IDataModel{
    
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
        Recordset recordset = db.attributes();
        Values values = new Values();
        recordset.stream().forEach((p) -> {
            values.put((String)p[0],p[1]);
        });
        calendarView.setMonth(values.getString(DATE_BEGIN),values.getString(DATE_END));
        calendarView.setPeriod(values.getString(DATE_BEGIN),values.getString(DATE_END));
        calendarView.setSelectionDate(new Date());
        
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
            setStatusText("server started");
        }

        @Override
        public String responce(HttpRequest request) {
            try{
                
                if (request.hasParamByName("data")){
                    open(request.paramByName("data"));                
                    File file=new File(MyServer.SERVER_DATA);
                    try(
                            FileOutputStream out = new FileOutputStream(file);
                            BufferedWriter write = new BufferedWriter(new OutputStreamWriter(out,"utf-8"));){
                        write.write(request.paramByName("data"));
                    }
                       
                }
                else     
                    return "param data not found";
                setStatusText("data has bean uploaded");
            } catch (Exception e){
                return e.getMessage();
            }
            return "OK";
        }
        
    };

    public MyServer2() {
        setPreferredSize(new Dimension(800,600));
        setLayout(new BorderLayout());
        add(calendarView,BorderLayout.WEST);
        add(new JScrollPane(view));      
        add(view.statusBar,BorderLayout.PAGE_END);
        add(view.title,BorderLayout.PAGE_START);
    }
    
    public void showInFrame(){
        
        try{
        File file = new File(MyServer.SERVER_DATA);
        if (file.exists()){
            IDB db = new DB_JSON_decoder(file);
            open(db);
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
            new MyServer2().showInFrame();
        });
        
       
       
    }
    
    
    
}
