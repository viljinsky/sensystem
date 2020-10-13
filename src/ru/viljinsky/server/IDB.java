/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import ru.viljinsky.project2019.Recordset;

/**
 *
 * @author viljinsky
 */
public interface IDB{
    
    static final String SQL_SCHEDULE = "select null as date, s.day_id,s.bell_id,s.depart_id,s.group_id,s.subject_id,s.teacher_id,s.room_id,null as flag ,subject_group.week_id,s.bell_id - (select min(bell_id) from shift_detail inner join depart using (shift_id) where depart_id=s.depart_id) + 1  as lesson_no\n" +
        "from schedule s inner join subject_group using(depart_id,group_id,subject_id)";
    
    public static final String TABLE_NAME = "table_name";
    public static final String COLUMNS = "columns";
        
    public static final String META = "МЕТА";
    public static final String DAY_LIST = "day_list";
    public static final String BELL_LIST = "bell_list";
    public static final String DEPART = "depart";
    public static final String TEACHER = "teacher";
    public static final String ROOM = "room";
    public static final String SUBJECT = "subject";
    public static final String BUILDING = "building";
    public static final String SCHEDULE = "schedule";
    public static final String REPLACEMENT = "replacement";
    public static final String GROUP_LABEL = "group_label";
    public static final String ATTRIBUTES = "attributes";
    public static final String CHANGES = "changes";
    
    public Recordset day_list() throws Exception;
    public Recordset bell_list() throws Exception;
    public Recordset depart() throws Exception;
    public Recordset teacher() throws Exception;
    public Recordset room() throws Exception;
    public Recordset building() throws Exception;
    public Recordset subject() throws Exception;
    public Recordset schedule() throws Exception;        
    public Recordset changes() throws Exception;
    public Recordset group_label() throws Exception;
    public Recordset attributes() throws Exception;
       
} 
