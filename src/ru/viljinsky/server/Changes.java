/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;

/**
 * Прочтение журнала
 *
 * @author viljinsky
 */
public class Changes extends Recordset implements IDataModel {
    
    public static final String NEW_ROOM_ID = "new_room_id";
    public static final String NEW_TEACHER_ID = "new_teacher_id";
    public static final String NEW_SUBJECT_ID = "new_subject_id";
    
    static final String SQL_CHANGES = "select a.date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.subject_id,a.teacher_id,a.room_id,\n" 
                    +  "b.flag,a.journal_id,a.detail_id,\n"
                    + "null as new_subject_id,null as new_teacher_id,null as new_room_id\n"
                    + "from journal_detail a inner join journal b using(journal_id)";
    
    public Changes(Connection con) throws Exception{
        Recordset source = DataModel.query(con, SQL_CHANGES);
        Map<Integer,Object> map = new HashMap();
        map.put(source.columnIndex(DATE), null);
        map.put(source.columnIndex(DAY_ID), null);
        map.put(source.columnIndex(BELL_ID), null);
        map.put(source.columnIndex(DEPART_ID), null);
        map.put(source.columnIndex(GROUP_ID), null);

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
    
    public static void main(String[] args) throws Exception{
        JPanel panel = new JPanel(new BorderLayout());
        Grid grid = new Grid();
        panel.add(new JScrollPane(grid));
        DataModel.setConnection("моё расписание.db");
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(1400, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Proc.query(con->{
            grid.setRecordset(new Changes(con).select(DATE,DAY_ID,BELL_ID,DEPART_ID,GROUP_ID,SUBJECT_ID,FLAG,NEW_ROOM_ID,NEW_TEACHER_ID,NEW_SUBJECT_ID));
        });
    }
    
    
}
