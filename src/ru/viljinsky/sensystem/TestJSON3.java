/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.sensystem;

import java.sql.Connection;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.data.DB;
import ru.viljinsky.server.ReplRecordset;

/**
 * SenSystem controller
 * 
 * @author viljinsky
 */

class JSonRecordset extends Recordset{
    String json_name;
    public JSonRecordset(String json_name,Recordset source ) {
        this.json_name = json_name;
        setColumns(source.columns);
        addAll(source);        
    }

    public JSonRecordset(String json_name,Recordset source,Values values ) throws Exception{
        this.json_name = json_name;        
        setColumns(source.columns);
        addAll(source);        
        rename(values);
    }
        
    @Override
    public void print() {
        System.out.println(json_name);
        super.print(); //To change body of generated methods, choose Tools | Templates.
    }
    

    public void addToJSON(JSONObject json){
        JSONArray arr = new JSONArray();
        if (isEmpty()){
            JSONObject obj = new JSONObject();
            for(String columnName:columns){
                obj.put(columnName, "null");
            }
            arr.put(obj);
        } else {
            for(Iterator<Values> it=getIterator();it.hasNext();){
                Values values = it.next();
                JSONObject obj = new JSONObject();
                for(String key:values.keySet()){
                    obj.put(key, values.get(key));
                }
                arr.put(obj);
            }
        }
        json.put(json_name, arr);
    }
    
}
public class TestJSON3 extends JSONObject implements IDataModel {
    
    static final String ID = "id";
    static final String NAME = "name";
    static final String DESCRIPTION = "description";

    public TestJSON3(Connection con) throws Exception {
        DB db = new DB(con);
                
        Values values;
        JSonRecordset recordset;
        
        
        recordset = new JSonRecordset("attributes", db.attr(DATE_BEGIN,DATE_END));
        recordset.addToJSON(this);
        
        values = new Values();
        values.put(SKILL_ID, ID);
        values.put(SKILL_NAME, NAME);
        recordset = new JSonRecordset("skills",db.skill(),values); 
        recordset.addToJSON(this);

        
        values = new Values();
        values.put(SUBJECT_DOMAIN_ID, ID);
        values.put(SUBJECT_DOMAIN_NAME, NAME);
        recordset = new JSonRecordset("subject_domains",db.subject_domain(),values);
        recordset.addToJSON(this);
        
        values = new Values();
        values.put(BUILDING_ID,ID);
        values.put(BUILDING_NAME,NAME);
        recordset = new JSonRecordset("buildings", db.building(),values);
        recordset.addToJSON(this);
        
        values = new Values();
        values.put(BELL_ID, ID);
        values.put(TIME_START,"start");
        values.put(TIME_END, "end");
        recordset = new JSonRecordset("bells",db.bell_list(),values);
        recordset.addToJSON(this);
        
        values = new Values();
        values.put(SHIFT_ID, ID);
        values.put(SHIFT_NAME, NAME);
        
        Recordset r = db.shift_detail().join(db.shift(), SHIFT_ID).filter(new Values(SHIFT_TYPE_ID,1)).select(SHIFT_ID,BELL_ID);            
        r = db.shift().select(SHIFT_ID,SHIFT_NAME).join(r.min(BELL_ID, SHIFT_ID), SHIFT_ID).join(r.max(BELL_ID, SHIFT_ID), SHIFT_ID);                
        recordset = new JSonRecordset("shifts", r,values);
        recordset.addToJSON(this);
                
        values = new Values();
        values.put(SUBJECT_ID, ID);
        values.put(SUBJECT_DOMAIN_ID,"domain_id");
        values.put(SUBJECT_NAME, NAME);
        recordset = new JSonRecordset("subjects", db.subject().select(SUBJECT_ID,SUBJECT_NAME,COLOR,SUBJECT_DOMAIN_ID,"description"),values);
        recordset.addToJSON(this);
                        
        values = new Values();
        values.put(ROOM_ID, ID);
        values.put(ROOM_NAME, NAME);
        recordset = new JSonRecordset("rooms",db.room().select(ROOM_ID,ROOM_NAME,BUILDING_ID,"description"),values);
        recordset.addToJSON(this);
        
        values = new Values();
        values.put("group_key",ID);
        values.put(GROUP_LABEL, NAME);
        recordset = new JSonRecordset("depart_groups", db.query("select group_key,group_label from v_subject_group_label"),values);
        recordset.addToJSON(this);

        values = new Values();
        values.put(DEPART_ID,ID);
        values.put(DEPART_LABEL, NAME);
        recordset = new JSonRecordset("departs", db.depart().select(DEPART_ID,DEPART_LABEL,SKILL_ID,SHIFT_ID,TEACHER_ID),values);
        recordset.addToJSON(this);

        values = new Values();
        values.put(PROFILE_ID, ID);
        values.put(PROFILE_NAME, NAME);
        recordset = new JSonRecordset("teacher_profiles", db.profile().filter(new Values(PROFILE_TYPE_ID,1)).select(PROFILE_ID,PROFILE_NAME),values);
        recordset.addToJSON(this);
        
        values = new Values();
        values.put(TEACHER_ID, ID);
        recordset = new JSonRecordset("teachers",db.teacher().select(TEACHER_ID,LAST_NAME,FIRST_NAME,PATRONYMIC,SHIFT_ID,ROOM_ID,PROFILE_ID,PHOTO),values);
        recordset.addToJSON(this);
        
        values.put(ROOM_ID, ID);
        values.put(ROOM_NAME,NAME);
        recordset = new JSonRecordset("rooms", db.room().select(ROOM_ID,ROOM_NAME,BUILDING_ID,DESCRIPTION),values);
        recordset.addToJSON(this);
        
        
        r = db.schedule().left(db.query("select depart_id,subject_id,group_id,group_key from v_subject_group_label"),DEPART_ID,GROUP_ID,SUBJECT_ID);
        r = r.left(db.subject_group(), DEPART_ID,GROUP_ID,SUBJECT_ID);
        
        values = new Values();
        values.put(GROUP_KEY, "depart_group_id");
        recordset = new JSonRecordset("schedules", r.select(DAY_ID,BELL_ID,DEPART_ID,SUBJECT_ID,GROUP_KEY,TEACHER_ID,ROOM_ID,WEEK_ID),values);
        recordset.addToJSON(this);
        
//        r = new Recordset(DAY_ID,BELL_ID,DATE,DEPART_ID,SUBJECT_ID,"depart_group_id",TEACHER_ID,ROOM_ID,"change_type_id");
        r = new ReplRecordset(con).left(db.group_label(),DEPART_ID,GROUP_ID,SUBJECT_ID).select(DAY_ID,BELL_ID,DEPART_ID,SUBJECT_ID,FLAG,ReplRecordset.NEW_SUBJECT_ID,ReplRecordset.NEW_TEACHER_ID,ReplRecordset.NEW_ROOM_ID,GROUP_KEY);
        values = new Values();
        values.put(GROUP_KEY, "depart_group_id");
        values.put(FLAG,"change_type");
        recordset = new JSonRecordset("changes", r,values);
        recordset.addToJSON(this);
        
    }
    
    public void check(JSONObject json){
        for(Object s: json.names()){
            System.out.println(s);
            System.out.println("------------------------");
            JSONArray arr = json.getJSONArray((String)s);
            for(int i=0;i<arr.length();i++){
                JSONObject obj = arr.getJSONObject(i);
                for(Object fieldName:obj.names()){
                    System.out.println(fieldName+" "+obj.get((String)fieldName));
                }
                System.out.println();
            }

        }
    }

    public static void main(String[] args) throws Exception{
        DataModel.setConnection("моё расписание.db");
            
        TestJSON3 t = new TestJSON3(DataModel.getConnection());
        
        t.check(t);
                    
    }
                
}
