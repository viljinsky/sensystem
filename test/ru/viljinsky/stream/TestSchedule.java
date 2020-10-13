/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.data.DB;
import ru.viljinsky.project2019.replacement.Document;

class ScheduleRecordset extends Recordset implements IDataModel{
    
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public ScheduleRecordset(Recordset source,Recordset changes,Date firstDate) throws Exception{
        
        Calendar c = Calendar.getInstance();
        c.setTime(SIMPLE_DATE_FORMAT.parse(SIMPLE_DATE_FORMAT.format(firstDate)));
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date date1 = c.getTime();
        c.add(Calendar.DAY_OF_MONTH, 6);
        Date date2 = c.getTime();
        
        columns = source.columns;

        int iWeek = columnIndex(WEEK_ID);
        c.setTime(date1);
        while(!c.getTime().after(date2)){
            Date date = c.getTime();
            int day_id = c.get(Calendar.DAY_OF_WEEK);
            int week_id = c.get(Calendar.WEEK_OF_YEAR) % 2 +1;
            day_id= day_id==1?7:day_id-1;
            
            for(Object[] p: source.filter(new Values(DAY_ID,day_id))){
                if (p[iWeek].equals(0) || p[iWeek].equals(week_id)){
                    p[0]= SIMPLE_DATE_FORMAT.format(date);
                    add(p);
                }
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        
        int iDate = columnIndex(DATE),
            iBell=columnIndex(BELL_ID),
            iDepart=columnIndex(DEPART_ID),
            iSubject=columnIndex(SUBJECT_ID),
            iGroup = columnIndex(GROUP_ID),
            iFlag = columnIndex(FLAG),
//            iTeacher = columnIndex(TEACHER_ID),
//            iRoom= columnIndex(ROOM_ID),
                
            iNewSubject = changes.columnIndex("new_subject_id");
//            iNewTeacher=changes.columnIndex("new_teacher_id"),
//            iNewRoom=changes.columnIndex("new_room_id");
                
        Recordset t = new Recordset(changes);
//        iDate = changes.columnIndex(DATE);
        for(Object[] p:changes){
            Date d = SIMPLE_DATE_FORMAT.parse((String)p[iDate]);
            if (!d.before(date1) && !d.after(date2)){
                t.add(p);
            }
        }
                
        Set<Integer> set = new HashSet();
        set.add(iDate);
        set.add(iBell);
        set.add(iDepart);
        set.add(iGroup);
        
        // обновление из изменений
        for(Iterator<Object[]> it=t.iterator();it.hasNext();){
            Object[] p = it.next();
            Map<Integer,Object> m = new HashMap<>();
            for(int i:set){
                m.put(i, p[i]);
            }
            int row = locate(m);
            if(row!=-1){
//                System.out.println(row+":\n"+schedule.getValues(row));
                Object[] n = get(row);
                n[iFlag] = p[iFlag];
                if(!(n[iFlag].equals(0) || n[iFlag].equals(2))){
                    System.arraycopy(p, iNewSubject, n, iSubject, 3);
                }
                it.remove();
            }
        }
        // оставшиеся изменения в расписание
        for(Object[] p: t){
            Object[] n = new Object[columns.length];
            System.arraycopy(p, 0, n, 0, 9);
            add(n);
        }
        
        
    }
    
}

/**
 *
 * @author viljinsky
 */
public class TestSchedule extends JPanel implements IDataModel{
    Grid grid = new Grid();
    Grid grid1 = new Grid();
    
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public TestSchedule() {
        setPreferredSize(new Dimension(800,600));
        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(grid),new JScrollPane(grid1));
        splitPane.setResizeWeight(.5);
        add(splitPane);
    }
    
    public void open2(Connection con) throws Exception{
        DB  db = new DB(con);
        Recordset source = db.query("select null as date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.subject_id,a.teacher_id,a.room_id,null as flag,week_id from schedule a inner join subject_group using(depart_id,group_id,subject_id)");
        Recordset changes = new Document.Changes(con);
        grid.setRecordset(new ScheduleRecordset(source, changes, new Date()));
        grid1.setRecordset(changes);
    }
    
    public static void main(String[] args) throws Exception{
        TestSchedule panel = new TestSchedule();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        DataModel.setConnection("моё расписание.db");
        Proc.query(con->{
            panel.open2(con);
        });
    }
    
}
