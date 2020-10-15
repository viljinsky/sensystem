/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import ru.viljinsky.cells7.Cell;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;

/**
 *
 * @author viljinsky
 */
public interface ViewModel{
    
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    static final SimpleDateFormat SIMPLE_DATE_FORMAT1 = new SimpleDateFormat("E dd MMM");
    
    public void init() throws Exception;
    public void setDate(Date date) throws Exception;
    public void setDate(String date) throws Exception;
    public Values attributes();
    public void setView(ScheduleView view);
    
}

abstract class AbstractViewModel implements IDataModel,ViewModel{
    
    
    ScheduleView view;

    @Override
    public void setView(ScheduleView view){
        this.view = view;
    };
        
    static class ValuesHeader{
        String caption;
        Values values;

        public ValuesHeader(String caption,Values values) {
            this.values=values;
            this.caption = caption;
        }

        @Override
        public String toString(){
            return caption;
        }

    }
    
    @Override
    public Values attributes() {
        return view.attributes;
    }
    
    public Object getRowHeaderData(int row){
        ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
        return vh.values;
    }
    
    public Object getColumnHeaderData(int col){
        ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
        return vh.values;
    }

    @Override
    public void setDate(String date) throws Exception{
        setDate(SIMPLE_DATE_FORMAT.parse(date));
    };
            
    protected Recordset  getScheculeRecordset(Date date) throws Exception{
        Recordset recordset = new ScheduleRecordset(view.schedule, view.changes,date)
                .join(view.depart, DEPART_ID)
                .join(view.subject, SUBJECT_ID)
                .left(view.room, ROOM_ID)
                .left(view.teacher, TEACHER_ID)
                .left(view.group_label, DEPART_ID,GROUP_ID,SUBJECT_ID);
        int index = recordset.columnIndex(DATE);
        for(Iterator<Object[]> it=recordset.iterator();it.hasNext();){
            Object[] p = it.next();
            Date d = SIMPLE_DATE_FORMAT.parse((String)p[index]);
            if (d.before(view.date_begin) || d.after(view.date_end)){
                it.remove();
            }
        }
        return recordset;
    }
    
}

class Model2 extends AbstractViewModel{

    @Override
    public void init() throws Exception {
        Recordset depart = view.depart.filter(view.departFilter);
        view.setDimension(0, 0);
        for (int col=0;col<view.day_list.size();col++){
            view.addColumn(new ValuesHeader((String)view.day_list.get(col)[1],view.day_list.getValues(col).getValues(DAY_ID)));
        }
        
        for(int row=0;row<view.bell_list.size();row++){
            Values v = view.bell_list.getValues(row);
            view.addRow(new ValuesHeader(v.getString(TIME_START),v.getValues(BELL_ID)));
            for(int i=0;i<depart.size();i++){
                Values v1 = depart.getValues(i);
                v1.put(BELL_ID, v.get(BELL_ID));
                view.addRow(new ValuesHeader(v1.getString(DEPART_LABEL),v1.getValues(BELL_ID,DEPART_ID)));
            }
        }
        
        view.rebuild();
    }

    Cell findCell(Values values) {
        
        int col;
        for(col=0;col<view.columnCount();col++){
            ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
            if (values.getValues(DAY_ID).equals(vh.values)){
                break;
            }
        }
        int row;
        for(row=0;row<view.rowCount();row++){
            ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
            if (values.getValues(DEPART_ID,BELL_ID).equals(vh.values)){
                break;
            }
        }
        return view.cell(col,row);
    }
        
    @Override
    public void setDate(Date date) throws Exception {
        
        Calendar c = Calendar.getInstance();
        for(int col=0;col<view.columnCount();col++){
            ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
            c.setTime(date);
            c.add(Calendar.DAY_OF_MONTH, vh.values.getInteger(DAY_ID)-1);
            vh.caption = SIMPLE_DATE_FORMAT1.format(c.getTime());
        }
        
        view.clearItems();
        // Заполнение сетки

        Recordset recordset = getScheculeRecordset(date);
        
        for(Iterator<Values> it = recordset.getIterator();it.hasNext();){
            Values values = it.next();            
            Cell cell = findCell(values);
            if (cell!=null){
                cell.addItem(view.createItem(values));
            }
        }
        view.rebuild();
        
        
    }
    
}

class Model1 extends AbstractViewModel {

    @Override
    public void init(){
        view.setDimension(0, 0);
        Recordset depart = view.depart.filter(view.departFilter);
        for(int i = 0;i<view.day_list.size();i++){
            view.addRow(new ValuesHeader((String)view.day_list.get(i)[1],view.day_list.getValues(i).getValues(DAY_ID)));
            for (int j=0;j<view.bell_list.size();j++){
                Values y = view.bell_list.getValues(j).getValues(BELL_ID);
                y.put(DAY_ID, view.day_list.get(i)[0]);
                view.addRow(new ValuesHeader((String)view.bell_list.get(j)[1],y));
            }
        }
        for(int col=0;col<depart.size();col++){
            view.addColumn(new ValuesHeader((String)depart.get(col)[1], depart.getValues(col).getValues(DEPART_ID)));
        }
                
        view.rebuild();
    }
        
    @Override
    public void setDate(Date date) throws Exception{
        Calendar c = Calendar.getInstance();
        // Закголовки строк
        for(int row=0;row<view.rowCount();row++){
            ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
            if (vh.values.containsKey(DAY_ID) && !vh.values.containsKey(BELL_ID)){
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, vh.values.getInteger(DAY_ID)-1);
                vh.caption = SIMPLE_DATE_FORMAT1.format(c.getTime());
            }
        }
        
        // Заполнение сетки

        Recordset recordset = getScheculeRecordset(date);
        view.clearItems();
        for(Iterator<Values> it=recordset.getIterator();it.hasNext();){
            Values values = it.next();        
            Cell cell = findCell(values);
            if (cell!=null){
                cell.addItem(view.createItem(values));            
            }        
        }                
        view.rebuild();
        
    }
        
    
    Cell findCell(Values values){
        
        Values v = values.getValues(DAY_ID,BELL_ID);
        int row;
        for(row=0;row<view.rowCount();row++){
            ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
            if (vh.values.equals(v)){
                break;
            }                 
        }        
        v = values.getValues(DEPART_ID);
        int col;
        for(col=0;col<view.columnCount();col++){
            ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
            if (vh.values.equals(v)){
                break;
            }
        }       
        return view.cell(col,row);
    }
       
}

/**
 * Колонки - даты
 * Строки  классы-время
 * @author viljinsky
 */
class Model3 extends AbstractViewModel{

    @Override
    public void init() throws Exception {
        Recordset depart = view.depart.filter(view.departFilter);
        ValuesHeader vh;
        view.setDimension(0, 0);
        for(Iterator<Values> it=view.day_list.getIterator();it.hasNext();){
            Values values = it.next();
            vh = new ValuesHeader(values.getString(DAY_NAME),values.getValues(DAY_ID));
            view.addColumn(vh);
        }
        for(Iterator<Values> it = depart.getIterator();it.hasNext();){
            Values values = it.next();
            vh = new ValuesHeader(values.getString(DEPART_LABEL), values.getValues(DEPART_ID));
            view.addRow(vh);
            for(Iterator<Values> it2 = view.bell_list.getIterator();it2.hasNext();){
                Values v = it2.next();
                v.put(DEPART_ID, values.get(DEPART_ID));
                vh = new ValuesHeader(v.getString(TIME_START), v.getValues(BELL_ID,DEPART_ID));
                view.addRow(vh);
            }
        }
        view.rebuild();
    }
    
    Cell findCell(Values values){
        int col;
        Values v = values.getValues(DAY_ID);
        for(col=0;col<view.columnCount();col++){
            ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
            if (vh.values.equals(v)){
                break;
            }
        }
        v = values.getValues(BELL_ID,DEPART_ID);
        int row;
        for(row=0;row<view.rowCount();row++){
            ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
            if (vh.values.equals(v)){
                break;
            }
        }
        return view.cell(col, row);
    }

    @Override
    public void setDate(Date date) throws Exception {
        Calendar c = Calendar.getInstance();
        for(int col=0;col<view.columnCount();col++){
            ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
            c.setTime(date);
            c.add(Calendar.DAY_OF_MONTH, vh.values.getInteger(DAY_ID)-1);
            vh.caption = SIMPLE_DATE_FORMAT1.format(c.getTime());
        }
        
        
        view.clearItems();
        for(Iterator<Values> it = getScheculeRecordset(date).getIterator();it.hasNext();){
            Values values = it.next();
            Cell cell = findCell(values);
            if (cell!=null){
                cell.addItem(view.createItem(values));
            }
        }
        view.rebuild();
    }

}
