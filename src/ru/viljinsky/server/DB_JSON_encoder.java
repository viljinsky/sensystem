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
    

    public DB_JSON_encoder(Connection con) throws Exception{
        DB db = new DB(con);
        
        put(IDB.DEPART, recordsetToJSON(db.depart().select(DEPART_ID,DEPART_LABEL)));
        put(IDB.TEACHER, recordsetToJSON(db.teacher().select(TEACHER_ID,LAST_NAME)));
        put(IDB.ROOM, recordsetToJSON(db.room().select(ROOM_ID,ROOM_NAME)));
        put(IDB.SUBJECT, recordsetToJSON(db.subject().select(SUBJECT_ID,SUBJECT_NAME,COLOR)));
        put(IDB.BUILDING,recordsetToJSON(db.building().select(BUILDING_ID,BUILDING_NAME))) ;
        
        put(IDB.BELL_LIST, recordsetToJSON(db.bell_list().select(BELL_ID,TIME_START,TIME_END)));
        put(IDB.DAY_LIST, recordsetToJSON(db.day_list().select(DAY_ID,DAY_NAME)));
        
//        put("shift", recordsetToJSON(db.shift()));
//        put("shift_detail", recordsetToJSON(db.shift_detail()));
//        put("shift_type", recordsetToJSON(db.shift_type()));
        
        put(IDB.SCHEDULE, recordsetToJSON(db.query(IDB.SQL_SCHEDULE)));//db.query("select schedule.*,schedule.bell_id -(select min(bell_id) from shift_detail inner join depart using(shift_id)  where depart_id=schedule.depart_id )+1 as lesson_no from schedule")));

//        put("curriculum", recordsetToJSON(db.curriculum()));
//        put("curiculum_detail", recordsetToJSON(db.curriculum_detail()));
//        put("subject_group",recordsetToJSON(db.subject_group()));
        
        put(IDB.REPLACEMENT,recordsetToJSON(db.query(IDB.SQL_REPLACEMENT)));//db.query("select a.date,a.day_id,a.bell_id,a.depart_id,a.group_id,a.subject_id,a.teacher_id,a.room_id,b.flag,a.journal_id,a.detail_id,b.parent_id from journal_detail a inner join journal b")));
        
        put(IDB.GROUP_LABEL,recordsetToJSON(db.query("select * from v_subject_group_label")));
        
        put(IDB.ATTRIBUTES,recordsetToJSON(db.attr(DATE_END,DATE_BEGIN,SCHEDULE_TYTLE,EDUCATIONAL_INSTITUTION)));
        
    }
            
    private JSONArray recordsetToJSON(Recordset recordset){
        List<JSONObject> list = new ArrayList<>();
        if(!recordset.isEmpty()){
            for(Iterator<Values> it= recordset.getIterator();it.hasNext();){
                JSONObject jObj = new JSONObject();
                Values values = it.next();
                for(String key:values.keySet()){
                    jObj.put(key, values.get(key));
                }
                list.add(jObj);
            }
        } else {
            JSONObject obj = new JSONObject();
            for(String s:recordset.columns){
                obj.put(s, NULL);
            }
            list.add(obj);
        }
        return new JSONArray(list);
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
        t.parce(t.toString());        
    }
    
}
