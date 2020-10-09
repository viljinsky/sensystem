/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.sql.Connection;
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
public class ReplRecordset extends Recordset implements IDataModel {
    
    public static final String NEW_TEACHER_ID = "new_teacher_id";
    public static final String NEW_ROOM_ID = "new_room_id";
    public static final String NEW_SUBJECT_ID = "new_subject_id";
    
    int iJournalID;
    int iDetailID;
    int iParentID;
    int iSubjectID;
    int iNewSubjectID;
    int iTeacherID;
    int iNewTecherID;
    int iRoomID;
    int iNewRoomID;
    int iFlag;

    Object[] findChild(Recordset recordset, Object[] data) {
        for (Object[] p : recordset) {
            if(p[iParentID]!=null){
                if (p[iParentID].equals(data[iJournalID]) && p[iDetailID].equals(data[iDetailID])) {
                    Object[] result = data.clone();
                    result[iNewSubjectID] = p[iSubjectID];
                    result[iNewRoomID] = p[iRoomID];
                    result[iNewTecherID] = p[iTeacherID];
                    return result;
                }
            }
        }
        return null;
    }
    static final String SQL = "select a.date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.teacher_id,a.room_id,a.subject_id,b.flag,a.journal_id,a.detail_id,b.parent_id from journal_detail a inner join journal b using (journal_id)";

    public ReplRecordset(Connection con) throws Exception {
        Recordset recordset = DataModel.query(con, SQL).left(new Recordset(NEW_TEACHER_ID, NEW_ROOM_ID, NEW_SUBJECT_ID));
        columns = recordset.columns;
        iFlag = columnIndex(FLAG);
        iJournalID = columnIndex(JOURNAL_ID);
        iDetailID = columnIndex(DETAIL_ID);
        iParentID = columnIndex(PARENT_ID);
        iSubjectID = columnIndex(SUBJECT_ID);
        iNewSubjectID = columnIndex(NEW_SUBJECT_ID);
        iTeacherID = columnIndex(TEACHER_ID);
        iNewTecherID = columnIndex(NEW_TEACHER_ID);
        iRoomID = columnIndex(ROOM_ID);
        iNewRoomID = columnIndex(NEW_ROOM_ID);
        for (Object[] p : recordset) {
            int flag = (int)p[iFlag];            
            Object[] n = findChild(recordset, p);
            if(n==null){
                if(p[iParentID]==null)
                    add(p);
            } else{
                add(n);
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
            grid.setRecordset(new ReplRecordset(con).select(DATE,DAY_ID,BELL_ID,DEPART_ID,GROUP_ID,SUBJECT_ID,FLAG,NEW_ROOM_ID,NEW_TEACHER_ID,NEW_SUBJECT_ID));
        });
    }
    
    
}
