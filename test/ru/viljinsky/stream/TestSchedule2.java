/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import ru.viljinsky.calendars.CalendarBlock;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.replacement.Document;
import ru.viljinsky.server.DB_SQLITE;
import ru.viljinsky.server.IDB;

/**
 *
 * @author viljinsky
 */
public class TestSchedule2 extends JPanel implements IDataModel{
    
    CalendarBlock calendarView = new CalendarBlock(){

        @Override
        public void selectedChange() {
            setDate(getSelectedDate());
        }
        
    };
    
        Recordset schedule;
        Recordset changes;
    
    void setDate(Date date){
//        Recordset schedule = db.schedule();
//        Recordset changes = db.changes();
        try{
            if (date!=null){
                Recordset s = new Document.ScheduleRecordset(schedule, changes, date);

                grid1.setRecordset(s);
                grid2.setRecordset(changes);
            }
//        grid1.setRecordset(schedule);
//        grid2.setRecordset(changes);
        
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    Grid grid1 = new Grid(),grid2 = new Grid();

    public TestSchedule2() {
        setPreferredSize(new Dimension(1200,600));
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(grid1),new JScrollPane(grid2));
        splitPane.setResizeWeight(.5);
        add(splitPane);
        
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(0,18,0,18));
        panel.add(calendarView);
        
        add(new JScrollPane(panel),BorderLayout.WEST);
    }
    
    public void open(IDB db)throws Exception{
        
        Recordset attr = db.attributes();
        Values attributes = new Values();
        for(Object[] p: attr){
            attributes.put((String)p[0],p[1]);
        }
        calendarView.setMonth(attributes.getString(DATE_BEGIN),attributes.getString(DATE_END));
        
        schedule = db.schedule();
        changes = db.changes();
        
        grid1.setRecordset(schedule);
        grid2.setRecordset(changes);
//        calendarView.setSelectionDate(new Date());
        
    }
    
    public static void main(String[] args) throws Exception{
        TestSchedule2 panel = new TestSchedule2();
        
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
//        IDB db = new DB_JSON_decoder(new File(MyServer.SERVER_DATA));
//        panel.open(db);
        DataModel.setConnection("моё расписание.db");
        Proc.query(con->{;
            IDB db = new DB_SQLITE(con);
            panel.open(db);
        });
    }
    
    
}
