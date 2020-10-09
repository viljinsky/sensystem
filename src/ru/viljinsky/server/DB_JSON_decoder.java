/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.viljinsky.project2019.Recordset;

/**
 *
 * @author viljinsky
 */
public class DB_JSON_decoder implements IDB{

    JSONObject json ;
    
    void addValues(Recordset recordset,JSONObject obj){
        boolean g = false;
        Object[] p = new Object[recordset.columns.length];
        for(int i=0;i<p.length;i++){
            g = false;
            if (obj.has(recordset.columns[i])){
                p[i] = obj.get(recordset.columns[i]);
                if (!p[i].equals(null)){
                    g = true;
                }
            }
        }
        if (g) {
            recordset.add(p);
        };
    }
    
    Recordset getRecordset(String name){
        Recordset recordset = new Recordset();
        JSONArray arr = json.getJSONArray(name);
        Set<String> columns = new HashSet<>();
        for(int i=0;i<arr.length();i++){
            columns.addAll(arr.getJSONObject(i).keySet());
        }
        recordset.columns = columns.toArray(new String[columns.size()]);
        for(int i=0;i<arr.length();i++){            
            addValues(recordset,arr.getJSONObject(i));
        }
        
        return recordset;
    }
    
    public DB_JSON_decoder(JSONObject json) {
        this.json = json;
    }

    public DB_JSON_decoder(File file) throws Exception{
       try(
                FileInputStream in = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
               ){
           json = new JSONObject(reader.readLine());
       }
    }

    public DB_JSON_decoder(String str)throws Exception {
        json = new JSONObject(str);
    }
    
    public void print(){
        for(String s: json.keySet()){
            getRecordset(s).print();
        }
    }
    
    String tab(Object obj){
        int length = 20;
        String s;
        if (obj == null){
            s="null";
        } else {
            s = obj.toString();
        }
        if (s.length()>length){
            s = s.substring(1,length-2)+"...";
        } else {
            for(int i=s.length();i<length;i++){
                s+=" ";
            }
        }
        
        return s;
    }
    
    StringBuilder dump(String tableName,Recordset recordset,int maxRowCount){
        int rowCount = maxRowCount;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tableName).append("\n");
        for(String columnName: recordset.columns){
            stringBuilder.append(tab(columnName)).append(" ");
        }
        stringBuilder.append("\n");
        for(int j=0;j<recordset.columnCount();j++){
            String s = "";
            for(int i=0;i<20;i++) s+="-";
            stringBuilder.append(s).append(" ");
        }

        stringBuilder.append("\n");

         for(Object[] p: recordset){
            for(Object c: p){
                stringBuilder.append(tab(c)).append(" ");
            }
            stringBuilder.append("\n");
            rowCount--;
            if (rowCount<=0){
                stringBuilder.append("\n----------------\n....Ещё "+(recordset.size()-maxRowCount)+" запись(ей).");
                break;
            }
        }

         stringBuilder.append("\n\n");
        return stringBuilder;
    }
    
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(String tableName: json.keySet()){
            Recordset recordset = getRecordset(tableName);
            stringBuilder.append(dump(tableName, recordset, 10));
//            stringBuilder.append(tableName).append("\n");
//            for(String columnName: recordset.columns){
//                stringBuilder.append(tab(columnName)).append(" ");
//            }
//            stringBuilder.append("\n");
//            for(int j=0;j<recordset.columnCount();j++){
//                String s = "";
//                for(int i=0;i<20;i++) s+="-";
//                stringBuilder.append(s).append(" ");
//            }
//            
//            stringBuilder.append("\n");
//            
//             for(Object[] p: recordset){
//                for(Object c: p){
//                    stringBuilder.append(tab(c)).append(" ");
//                }
//                stringBuilder.append("\n");
//            }
//            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
    
    
    @Override
    public Recordset day_list() throws Exception {
        return getRecordset(DAY_LIST);//.select("day_id","day_name");
    }

    @Override
    public Recordset bell_list() throws Exception {
        return getRecordset(BELL_LIST);//.select("bell_id","time_start","time_end");
    }

    @Override
    public Recordset depart() throws Exception {
        return getRecordset(DEPART);//.select("depart_id","depart_label");
    }

    @Override
    public Recordset teacher() throws Exception {
        return getRecordset(TEACHER);
    }

    @Override
    public Recordset room() throws Exception {
        return getRecordset(ROOM);
    }

    @Override
    public Recordset building() throws Exception {
        return getRecordset(BUILDING);
    }

    @Override
    public Recordset subject() throws Exception {
        return getRecordset(SUBJECT);
    }

    @Override
    public Recordset schedule() throws Exception {
        return getRecordset(SCHEDULE);
    }

    @Override
    public Recordset replacement() throws Exception {
        return getRecordset(REPLACEMENT);
    }

    @Override
    public Recordset group_label() throws Exception {
        return getRecordset(GROUP_LABEL);
    }

    @Override
    public Recordset attributes() throws Exception {
        return getRecordset(ATTRIBUTES);
    }
    
    public static void main(String[] args) throws Exception{
        File f = new File("server_data.json");
        
        IDB db = new DB_JSON_decoder(f);
        System.out.println(db);
        
    }
    
    
}
    
    
    

