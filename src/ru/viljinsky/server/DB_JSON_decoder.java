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
import org.json.JSONArray;
import org.json.JSONObject;
import ru.viljinsky.project2019.Recordset;

/**
 *
 * @author viljinsky
 */
public class DB_JSON_decoder implements IDB{

    JSONObject json ;
    JSONArray meta;
        
    void addValues(Recordset recordset,JSONObject obj){
        Object[] p = new Object[recordset.columns.length];
        for(int i=0;i<p.length;i++){
            if (obj.has(recordset.columns[i])){
                p[i] = obj.get(recordset.columns[i]);
            }
        }
        recordset.add(p);
    }
    
    Recordset getRecordset(String name) throws Exception{
        Recordset recordset = null;
        for(int i=0;i<meta.length();i++){
            JSONObject obj = meta.getJSONObject(i);
            if (obj.has(TABLE_NAME) && name.equals(obj.getString(TABLE_NAME))){
                recordset = new Recordset();
                JSONArray arr = obj.getJSONArray(COLUMNS);
                String[] columns = new String[arr.length()];
                for(int j=0;j<columns.length;j++){
                    columns[j]=arr.getString(j);
                }
                recordset.columns = columns;
                break;
            }
        }
        if (recordset==null) throw new Exception("data "+name+" not found");
        
        JSONArray arr = json.getJSONArray(name);
        for(int i=0;i<arr.length();i++){            
            addValues(recordset,arr.getJSONObject(i));
        }
        
        return recordset;
    }
    
    private void setJson(JSONObject json) throws Exception{
        if (!json.has(IDB.META)) throw new Exception ("meta not found");
        this.json = json;
        this.meta = json.getJSONArray(IDB.META);
    }
    
    public DB_JSON_decoder(JSONObject json) throws Exception{
        setJson(json);
    }

    public DB_JSON_decoder(File file) throws Exception{
        if (!file.exists()) throw new Exception("file \""+file.getName()+"\" not found");
       try(
                FileInputStream in = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
               ){
           setJson(new JSONObject(reader.readLine()));
       }
    }

    public DB_JSON_decoder(String str)throws Exception {
        setJson(new JSONObject(str));
    }
    
    
    public void print(){
        try{
        for(String s: json.keySet()){
            getRecordset(s).print();
        }
        } catch (Exception e){
            System.out.println(e.getMessage());
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
        
//        for(int i =0;i<meta.length();i++){
//            JSONObject obj = meta.getJSONObject(i);
//            String tableName = obj.getString("table_name");
//            JSONArray columns = obj.getJSONArray("columns");
//            stringBuilder.append(tableName).append("\n");
//            for(int column=0;column<columns.length();column++){
//                String columneName = columns.getString(column);
//                stringBuilder.append(columneName).append(" ");
//            }
//            stringBuilder.append("\n");
//        }
        
        try{
            for(int i=0;i<meta.length();i++){
                JSONObject obj = meta.getJSONObject(i);
                Recordset r = getRecordset(obj.getString(TABLE_NAME));
                stringBuilder.append(dump(obj.getString(TABLE_NAME),r,10));
            }
        } catch (Exception e){
            stringBuilder.append(e.getMessage()).append("\n");
        }
        return stringBuilder.toString();
        
    }
    
    
    @Override
    public Recordset day_list() throws Exception {
        return getRecordset(DAY_LIST);
    }

    @Override
    public Recordset bell_list() throws Exception {
        return getRecordset(BELL_LIST);
    }

    @Override
    public Recordset depart() throws Exception {
        return getRecordset(DEPART);
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
    public Recordset changes() throws Exception {
        return getRecordset(CHANGES);
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
        File f = new File(MyServer.SERVER_DATA);
        
        IDB db = new DB_JSON_decoder(f);
        System.out.println(db);
        
    }
    
    
}
    
    
    

