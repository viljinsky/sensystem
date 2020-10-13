/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.data.DB;
import ru.viljinsky.server.IDB;

/**
 *
 * @author viljinsky
 */
public class DB_JSON_encoder extends JSONObject implements IDataModel {

    JSONArray meta = new JSONArray();
    
    public void addMeta(Recordset recordset){
        JSONObject obj = new JSONObject();
        obj.put("table_name", recordset.getName());
        obj.put("columns", new JSONArray(recordset.columns));
        meta.put(obj);
    };

    public DB_JSON_encoder(Connection con) throws Exception{
        DB db = new DB(con);
        
        recordsetToJSON(IDB.DEPART,db.depart().select(DEPART_ID,DEPART_LABEL));
        recordsetToJSON(IDB.TEACHER,db.teacher().select(TEACHER_ID,LAST_NAME));
        recordsetToJSON(IDB.ROOM,db.room().select(ROOM_ID,ROOM_NAME));
        recordsetToJSON(IDB.SUBJECT,db.subject().select(SUBJECT_ID,SUBJECT_NAME,COLOR));
        recordsetToJSON(IDB.BUILDING,db.building().select(BUILDING_ID,BUILDING_NAME)) ;        
        recordsetToJSON(IDB.BELL_LIST,db.bell_list().select(BELL_ID,TIME_START,TIME_END));
        recordsetToJSON(IDB.DAY_LIST,db.day_list().select(DAY_ID,DAY_NAME));        
        recordsetToJSON(IDB.SCHEDULE,db.query(IDB.SQL_SCHEDULE));
//        recordsetToJSON(IDB.REPLACEMENT,db.query(IDB.SQL_REPLACEMENT));        
        recordsetToJSON(IDB.GROUP_LABEL,db.query("select * from v_subject_group_label"));        
        recordsetToJSON(IDB.ATTRIBUTES,db.attr(DATE_END,DATE_BEGIN,SCHEDULE_TYTLE,EDUCATIONAL_INSTITUTION));
        recordsetToJSON(IDB.CHANGES, db.changes());
        
        put(IDB.META, meta);
        
    }
            
    private void recordsetToJSON(String tableName,Recordset recordset){
        recordset.setName(tableName);
        addMeta(recordset);
        List<JSONObject> list = new ArrayList<>();
        for(Iterator<Values> it= recordset.getIterator();it.hasNext();){
            JSONObject jObj = new JSONObject();
            Values values = it.next();
            for(String key:values.keySet()){
                jObj.put(key, values.get(key));
            }
            list.add(jObj);
        }
        put(tableName, new JSONArray(list));
    }
    
    public void parceValues(JSONObject obj){
        Values values = new Values();
        for(String s: obj.keySet()){
            values.put(s, obj.get(s));
        }
        values.print();
    }
    
    public void parceTable(String tableName,JSONArray arr){
        for(int i=0;i<arr.length();i++){
            parceValues(arr.getJSONObject(i));
        }
    }
    
    public void parce(String jsonStr) throws Exception{
        JSONObject t = new JSONObject(jsonStr);
        for(String tableName:t.keySet()){
            parceTable(tableName, getJSONArray(tableName));
        }
    }
        
    public static void main(String[] args) throws Exception{
        DataModel.setConnection("Моё расписание.db");
        DB_JSON_encoder t = new DB_JSON_encoder(DataModel.getConnection());
        
        JSONArray a = t.getJSONArray(IDB.META);
        for(int i=0;i<a.length();i++){
            System.out.println(a.get(i));
        }
        
//        t.parce(t.toString());        
    }
    
}
