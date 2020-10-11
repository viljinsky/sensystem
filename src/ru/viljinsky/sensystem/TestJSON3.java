/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.sensystem;

import java.sql.Connection;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.data.DB;
import ru.viljinsky.server.IDB;
import ru.viljinsky.server.ReplRecordset;

/**
 * SenSystem convertor
 * 
 * @author viljinsky
 */

class JSonRecordset extends Recordset{
    
    public JSonRecordset(Recordset source ) {
        setColumns(source.columns);
        addAll(source);        
    }

    public JSonRecordset(Recordset source,Values values ) throws Exception{
        setColumns(source.columns);
        addAll(source);        
        rename(values);
    }
}

public class TestJSON3 extends JSONObject implements IDataModel {
    
    JSONArray meta = new JSONArray();
    
    static final String ID = "id";
    static final String NAME = "name";
    static final String DESCRIPTION = "description";
    
    JSONArray recordsetToJson(Recordset recordset){
        JSONArray arr = new JSONArray();
        for(Object[] p: recordset){
            JSONObject obj = new JSONObject();
            for(int i=0;i<recordset.columns.length;i++){
                obj.put(recordset.columns[i], p[i]);
            }
            arr.put(obj);
        }
//        System.out.println(arr.toString());
        return arr;
    }
    
    void addMeta(String tableName,Recordset recordaet){
        JSONObject obj = new JSONObject();
        obj.put("table_name", tableName);
        obj.put("columns", new JSONArray(recordaet.columns));
        meta.put(obj);
    }
        
    private void addRecordset(String tableName,Recordset recordset){

        addMeta(tableName,recordset);
        put(tableName, recordsetToJson(recordset));
    }

    public TestJSON3(Connection con) throws Exception {
        
        DB db = new DB(con);
                
        Values values;
        JSonRecordset recordset;
                
        recordset = new JSonRecordset(db.attr(DATE_BEGIN,DATE_END));
        addRecordset("attributes", recordset);
        
        values = new Values();
        values.put(SKILL_ID, ID);
        values.put(SKILL_NAME, NAME);
        recordset = new JSonRecordset(db.skill(),values); 
        addRecordset("skills", recordset);
        
        values = new Values();
        values.put(SUBJECT_DOMAIN_ID, ID);
        values.put(SUBJECT_DOMAIN_NAME, NAME);
        recordset = new JSonRecordset(db.subject_domain(),values);
        addRecordset("subject_domains", recordset);
        
        values = new Values();
        values.put(BUILDING_ID,ID);
        values.put(BUILDING_NAME,NAME);
        recordset = new JSonRecordset(db.building(),values);
        addRecordset("buildings", recordset);
        
        values = new Values();
        values.put(BELL_ID, ID);
        values.put(TIME_START,"start");
        values.put(TIME_END, "end");
        recordset = new JSonRecordset(db.bell_list(),values);
        addRecordset("bells",recordset);
        
        values = new Values();
        values.put(SHIFT_ID, ID);
        values.put(SHIFT_NAME, NAME);
        
        Recordset r = db.shift_detail().join(db.shift(), SHIFT_ID).filter(new Values(SHIFT_TYPE_ID,1)).select(SHIFT_ID,BELL_ID);            
        r = db.shift().select(SHIFT_ID,SHIFT_NAME).join(r.min(BELL_ID, SHIFT_ID), SHIFT_ID).join(r.max(BELL_ID, SHIFT_ID), SHIFT_ID);                
        recordset = new JSonRecordset( r,values);
        addRecordset("shifts",recordset);
                
        values = new Values();
        values.put(SUBJECT_ID, ID);
        values.put(SUBJECT_DOMAIN_ID,"domain_id");
        values.put(SUBJECT_NAME, NAME);
        recordset = new JSonRecordset(db.subject().select(SUBJECT_ID,SUBJECT_NAME,COLOR,SUBJECT_DOMAIN_ID,"description"),values);
        addRecordset("subjects", recordset);
                        
        values = new Values();
        values.put(ROOM_ID, ID);
        values.put(ROOM_NAME, NAME);
        recordset = new JSonRecordset(db.room().select(ROOM_ID,ROOM_NAME,BUILDING_ID,"description"),values);
        addRecordset("rooms",recordset);
        
        values = new Values();
        values.put("group_key",ID);
        values.put(GROUP_LABEL, NAME);
        recordset = new JSonRecordset(db.query("select group_key,group_label from v_subject_group_label"),values);
        addRecordset("depart_groups", recordset);

        values = new Values();
        values.put(DEPART_ID,ID);
        values.put(DEPART_LABEL, NAME);
        recordset = new JSonRecordset(db.depart().select(DEPART_ID,DEPART_LABEL,SKILL_ID,SHIFT_ID,TEACHER_ID),values);
        addRecordset("departs", recordset);

        values = new Values();
        values.put(PROFILE_ID, ID);
        values.put(PROFILE_NAME, NAME);
        recordset = new JSonRecordset(db.profile().filter(new Values(PROFILE_TYPE_ID,1)).select(PROFILE_ID,PROFILE_NAME),values);
        addRecordset("teacher_profiles", recordset);
        
        values = new Values();
        values.put(TEACHER_ID, ID);
        recordset = new JSonRecordset(db.teacher().select(TEACHER_ID,LAST_NAME,FIRST_NAME,PATRONYMIC,SHIFT_ID,ROOM_ID,PROFILE_ID,PHOTO),values);
        addRecordset("teachers",recordset);
        
        values.put(ROOM_ID, ID);
        values.put(ROOM_NAME,NAME);
        recordset = new JSonRecordset(db.room().select(ROOM_ID,ROOM_NAME,BUILDING_ID,DESCRIPTION),values);
        addRecordset("rooms", recordset);
        
        r = db.schedule().left(db.query("select depart_id,subject_id,group_id,group_key from v_subject_group_label"),DEPART_ID,GROUP_ID,SUBJECT_ID);
        r = r.left(db.subject_group(), DEPART_ID,GROUP_ID,SUBJECT_ID);
        
        values = new Values();
        values.put(GROUP_KEY, "depart_group_id");
        recordset = new JSonRecordset(r.select(DAY_ID,BELL_ID,DEPART_ID,SUBJECT_ID,GROUP_KEY,TEACHER_ID,ROOM_ID,WEEK_ID),values);
        addRecordset("schedules", recordset);
        
        r = new ReplRecordset(con).left(db.group_label(),DEPART_ID,GROUP_ID,SUBJECT_ID).select(DAY_ID,BELL_ID,DEPART_ID,SUBJECT_ID,FLAG,ReplRecordset.NEW_SUBJECT_ID,ReplRecordset.NEW_TEACHER_ID,ReplRecordset.NEW_ROOM_ID,GROUP_KEY);
        values = new Values();
        values.put(GROUP_KEY, "depart_group_id");
        values.put(FLAG,"change_type");
        recordset = new JSonRecordset(r,values);
        addRecordset("changes", recordset);
        
        put(IDB.META,meta);
        
    }
    
    public void check(JSONObject json){
        for(String s: json.keySet()){
            JSONArray arr = json.getJSONArray(s);
            System.out.println(s + "   "+ arr.toString());
        }
    }

    public static void main(String[] args) throws Exception{
        DataModel.setConnection("моё расписание.db");
            
        TestJSON3 t = new TestJSON3(DataModel.getConnection());
  
        t.check(t);
                    
    }
                
}
