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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import ru.viljinsky.tcp.HttpResponce;
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
        view.statusBar.setText(text);
    }

    public byte[] file_contents(File file) throws Exception {

        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int n;
        byte[] data = new byte[1024];
        while((n = in.read(data))!=-1){
            out.write(data, 0, n);
        }
        return out.toByteArray();
    }
    
    class Page1 extends HttpResponce{

        public Page1() throws Exception{
            File file = new File(SERVER_DATA);
            if (file.exists()){
                responce = file_contents(file);
            } else {
                responce = new String("<p>файл данных не найден</p>").getBytes();
            }
        }
        
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
        public HttpResponce responce(HttpRequest request) {
            try{                                
                
                switch(request.method){
                    case "POST":                                                
                        
                        switch(request.path){
                            case "/":
                                if (request.hasParamByName("data")){
                                    open(request.paramByName("data"));                
                                    File file=new File(SERVER_DATA);
                                    try(
                                        FileOutputStream out = new FileOutputStream(file);
                                        BufferedWriter write = new BufferedWriter(new OutputStreamWriter(out,"utf-8"));){
                                        write.write(request.paramByName("data"));
                                    }

                                }
                                return new HttpResponce(HttpResponce.BAD_REQUEST,"param data not found");
                            case "/page1":
                                File file = new File(SERVER_DATA);
                                if (file.exists())
                                    return new HttpResponce(file_contents(file));
                                else 
                                    return new HttpResponce(HttpResponce.NOT_FOUND,"<p>not found</p>");
                            
                        }
                        
                        
                    case "GET":
                        
                        String path = request.path;
                        switch (path){
                            case "/page1":
                                return new Page1();//new HttpResponce(HttpResponce.RESULT_OK,"<p>page1</p>");
                            case "/page2":
                                return new HttpResponce(HttpResponce.RESULT_OK,"<p>page2</p>");
                            case "/page3":
                                return new HttpResponce(HttpResponce.RESULT_OK,"<p>page3</p>");                                
                            default:
                                File file = new File(".",path);
                                if (file.exists()){
                                    return new HttpResponce(file_contents(file));
                                }
                                return new HttpResponce(HttpResponce.NOT_FOUND,"<p>not found</p>");
                        }                        
                        //break;
                }
                setStatusText("Данные обновлены "+new SimpleDateFormat("HH:mm dd MMM yyyy").format(new Date()));
            } catch (Exception e){
                return new HttpResponce(HttpResponce.INTERNAL_ERROR,e.getMessage());
            }
            return new HttpResponce(HttpResponce.RESULT_OK, "OK");
        }
        
    };

//    CurriculumFilter curriculumFilter = new CurriculumFilter(view);
    
    public MyServer() {
        setPreferredSize(new Dimension(800,600));
        setLayout(new BorderLayout());
        add(calendarView,BorderLayout.WEST);
        add(new JScrollPane(view));      
        add(view.statusBar,BorderLayout.PAGE_END);
        add(view.title,BorderLayout.PAGE_START);
        view.title.add(new ViewControl(view));
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
        
        JFrame frame = new JFrame("Составитель расписания (сервер)");
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
