/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.sql.Connection;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.data.DB;
import ru.viljinsky.server.IDB;

/**
 *
 * @author viljinsky
 */
public class DB_SQLITE implements IDB,IDataModel{
    
    DB db;

    public DB_SQLITE(Connection con) {
        db = new DB(con);
    }
    

    @Override
    public Recordset day_list() throws Exception {
        return db.day_list().select(DAY_ID,DAY_NAME);
    }

    @Override
    public Recordset bell_list() throws Exception {
        return db.bell_list().select(BELL_ID,TIME_START,TIME_END);
    }

    @Override
    public Recordset depart() throws Exception {
        return db.depart().select(DEPART_ID,DEPART_LABEL);
    }

    @Override
    public Recordset teacher() throws Exception {
        return db.teacher().select(TEACHER_ID,LAST_NAME);
    }

    @Override
    public Recordset room() throws Exception {
        return db.room().select(ROOM_ID,ROOM_NAME);
    }

    @Override
    public Recordset building() throws Exception {
        return db.building().select(BUILDING_ID,BUILDING_NAME);
    }

    @Override
    public Recordset subject() throws Exception {
        return db.subject().select(SUBJECT_ID,SUBJECT_NAME,COLOR);
    }

    @Override
    public Recordset schedule() throws Exception {
        return db.query(SQL_SCHEDULE);
    }

    @Override
    public Recordset changes() throws Exception {
        return db.changes();
    }

    @Override
    public Recordset group_label() throws Exception {
        return db.group_label();
    }

    @Override
    public Recordset attributes() throws Exception {
        return db.attr();
    }
    
}
