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
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.data.DB;
import ru.viljinsky.project2019.replacement.Document;


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
        Recordset source = db.query("select null as date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.subject_id,a.teacher_id,a.room_id,null as flag,week_id,0 as lesson_no from schedule a inner join subject_group using(depart_id,group_id,subject_id)");
//        source.print();
        Recordset changes = new Document.Changes(con);
//        changes.print();
        grid.setRecordset(new Document.ScheduleRecordset(source, changes, new Date()));
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
