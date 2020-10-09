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
    
    static final String SQL_SCHEDULE = "select schedule.*,schedule.bell_id - (select min(bell_id) from shift_detail inner join depart using (shift_id) where depart_id=schedule.depart_id) + 1  as lesson_no ,subject_group.week_id\n" +
        "from schedule inner join subject_group using(depart_id,group_id,subject_id)";
    static final String SQL_REPLACEMENT = "select a.date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.subject_id,a.teacher_id,a.room_id,b.flag,a.journal_id,a.detail_id,b.parent_id,22 as lesson_no from journal_detail a inner join journal b using(journal_id)";
    
    
    
    
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
    
    public Recordset day_list() throws Exception;
    public Recordset bell_list() throws Exception;
    public Recordset depart() throws Exception;
    public Recordset teacher() throws Exception;
    public Recordset room() throws Exception;
    public Recordset building() throws Exception;
    public Recordset subject() throws Exception;
    public Recordset schedule() throws Exception;        
    public Recordset replacement() throws Exception;
    public Recordset group_label() throws Exception;
    public Recordset attributes() throws Exception;
       
} 
