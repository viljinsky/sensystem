/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ru.viljinsky.calendars.CalendarView;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.tcp.HttpRequest;
import ru.viljinsky.tcp.HttpServer;

/**
 *
 * @author viljinsky
 */
public class MyServer extends JPanel implements IDataModel{
    
    ScheduleView scheduleView;
            
    public static final String RELOAD = "reload";
        
    void reload(){
        try{
            scheduleView.open(server.db);
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }
    
    void saveJson() throws Exception{
        File file = new File(SERVER_DATA);
        try(FileOutputStream out = new FileOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,"utf-8"));){
            writer.write(server.db.toString());
        }            
    }
    
    void readJson(){
        File file = new File(SERVER_DATA);
        if (file.exists()){
            try{
                scheduleView.open(new DB_JSON_decoder(file));
            } catch (Exception e){
//                e.printStackTrace();
            }
        }        
    }
    
    class Server extends HttpServer{
        
        DB_JSON_decoder db ;
                
        
        public void afterLoaded() throws Exception{
        }

        @Override
        public void onStop(HttpServer server) {
            super.onStop(server); //To change body of generated methods, choose Tools | Templates.
            scheduleView.setStatusTest("server stopped");
        }

        @Override
        public void onError(Exception e) {
            super.onError(e); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void onStart(HttpServer server) {
            super.onStart(server); //To change body of generated methods, choose Tools | Templates.
            scheduleView.setStatusTest("server started");
        }

        @Override
        public String responce(HttpRequest request) {
            try{
                
                db = new DB_JSON_decoder(request.paramByName("data"));
                
                scheduleView.open(db);
                                
                saveJson();
                
            } catch (Exception e){
                return "<p>Ошибка разбора</p>\n<p>"+e.getMessage()+"</p>\n";
            }
            return "Всё хорошо";
        }
        
    }
        
    
    CalendarView calendarCells = new CalendarView(){

        @Override
        public void change() {
            scheduleView.setDate(getSelectionDate());
            
        }
        
    };
    
    Server server = new Server(){

        @Override
        public void afterLoaded() throws Exception{
            scheduleView.setStatusTest("Данные обновлены");
        }
        
    };
    
    static final String SERVER_DATA = "server_data.json";
        
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    };
    
    JFrame f ;
    public void start(){
        if (f==null){
            f = new JFrame("Server 3345");
            f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            f.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {   
                    
                    f.setVisible(false);
                    f.dispose();
                    f=null;
                }

            });
            f.setContentPane(this);
            f.pack();
            f.setLocationRelativeTo(null);
        }
        f.setVisible(true);
    };
    
    Thread tread;
    Grid grid = new Grid();

    public MyServer(){
        
        scheduleView = new ScheduleView(){

            @Override
            public void periodChange() {
                Values values = getPeriod();
                calendarCells.setMonth(values.getString(DATE_BEGIN),values.getString(DATE_END));
                calendarCells.setPeriod(values.getString(DATE_BEGIN),values.getString(DATE_END));
            }
            
        };
        setLayout(new BorderLayout());
        calendarCells.setBorder(BorderFactory.createEtchedBorder());
        JPanel p = new JPanel(new BorderLayout());
        p.add(calendarCells,BorderLayout.WEST);
        p.add(new JScrollPane(scheduleView));
        add(p);
        add(scheduleView.title,BorderLayout.PAGE_START);
        add(scheduleView.statusBar,BorderLayout.PAGE_END);
        
        tread = new Thread(){

            @Override
            public void run() {
                try{
                    server.start(3345);
                } catch (Exception e){
                    showMessage(e.getMessage());
                }
            }

        };
        tread.start();
        
        setPreferredSize(new Dimension(800,600));

        readJson();
        }
            
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MyServer());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
}
