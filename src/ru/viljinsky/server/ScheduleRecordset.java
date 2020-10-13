/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.replacement.Document;

/**
 *
 * @author viljinsky
 */
public class ScheduleRecordset extends Recordset implements IDataModel {
    
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public ScheduleRecordset(Recordset source, Recordset changes, Date firstDate) throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTime(SIMPLE_DATE_FORMAT.parse(SIMPLE_DATE_FORMAT.format(firstDate)));
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date date1 = c.getTime();
        c.add(Calendar.DAY_OF_MONTH, 6);
        Date date2 = c.getTime();
        columns = source.columns;
        int iWeek = columnIndex(WEEK_ID);
        c.setTime(date1);
        while (!c.getTime().after(date2)) {
            Date date = c.getTime();
            int day_id = c.get(Calendar.DAY_OF_WEEK);
            int week_id = c.get(Calendar.WEEK_OF_YEAR) % 2 + 1;
            day_id = day_id == 1 ? 7 : day_id - 1;
            for (Object[] p : source.filter(new Values(DAY_ID, day_id))) {
                if (p[iWeek].equals(0) || p[iWeek].equals(week_id)) {
                    Object[] p1 = p.clone();
                    p1[0] = SIMPLE_DATE_FORMAT.format(date);
                    add(p1);
                }
            }
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
        int iDate = columnIndex(DATE);
        int iBell = columnIndex(BELL_ID);
        int iDepart = columnIndex(DEPART_ID);
        int iSubject = columnIndex(SUBJECT_ID);
        int iGroup = columnIndex(GROUP_ID);
        int iFlag = columnIndex(FLAG);
        //            iTeacher = columnIndex(TEACHER_ID),
        //            iRoom= columnIndex(ROOM_ID),
        int iNewSubject = changes.columnIndex("new_subject_id");
        //            iNewTeacher=changes.columnIndex("new_teacher_id"),
        //            iNewRoom=changes.columnIndex("new_room_id");
        Recordset t = new Recordset(changes);
        //        iDate = changes.columnIndex(DATE);
        for (Object[] p : changes) {
            Date d = SIMPLE_DATE_FORMAT.parse((String) p[iDate]);
            if (!(d.before(date1) || d.after(date2))) {
                t.add(p);
            }
        }
        Set<Integer> set = new HashSet();
        set.add(iDate);
        set.add(iBell);
        set.add(iDepart);
        set.add(iGroup);
        // обновление из изменений
        for (Iterator<Object[]> it = t.iterator(); it.hasNext();) {
            Object[] p = it.next();
            Map<Integer, Object> m = new HashMap<>();
            for (int i : set) {
                m.put(i, p[i]);
            }
            int row = locate(m);
            if (row != -1) {
                Object[] n = get(row);
                n[iFlag] = p[iFlag];
                if (!(n[iFlag].equals(Document.FLAG_CANCEL) || n[iFlag].equals(Document.FLAG_MOVE_TO))) {
                    System.arraycopy(p, iNewSubject, n, iSubject, 3);
                }
                it.remove();
            }
        }
        // оставшиеся изменения в расписание
        for (Object[] p : t) {
            Object[] n = new Object[columns.length];
            System.arraycopy(p, 0, n, 0, 9);
            n[source.columnIndex(LESSON_NO)]=p[changes.columnIndex(LESSON_NO)];
            n[source.columnIndex(WEEK_ID)]=0;
            add(n);
        }
    }
    
}
