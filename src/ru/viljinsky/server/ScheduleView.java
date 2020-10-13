/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import ru.viljinsky.cells7.Cell;
import ru.viljinsky.cells7.Item;
import ru.viljinsky.cells7.View;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.replacement.IReplacement;
import static ru.viljinsky.project2019.replacement.IReplacement.FLAG_CANCEL;
import static ru.viljinsky.project2019.replacement.IReplacement.FLAG_MOVE_FROM;
import static ru.viljinsky.project2019.replacement.IReplacement.FLAG_MOVE_TO;
import static ru.viljinsky.project2019.replacement.IReplacement.FLAG_REPLACE;

/**
 *
 * @author viljinsky
 */

class StatusBar extends JComponent{
    JLabel label = new JLabel("statusbar");

    public StatusBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        add(label);
    }

    public void setStatusTest(String str){
        label.setText(str);
    }

}

interface IViewModel{
}

class ViewModel implements IDataModel{

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
    
    public Object getRowHeaderData(int row){
        ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
        return vh.values;
    }
    
    public Object getColumnHeaderData(int col){
        ValuesHeader vh = (ValuesHeader)view.getColumnHeader(col);
        return vh.values;
    }
    
    Recordset day_list,bell_list,depart,room,subject,group_label,schedule,changes,building,teacher;
    Values attributes;
    
    ScheduleView view;
    
    public ViewModel(ScheduleView view) {
        this.view = view;
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
    
    public void open(IDB db) throws Exception{
        teacher = db.teacher();
        building = db.building();
        day_list = db.day_list().select(DAY_ID,DAY_NAME);
        bell_list = db.bell_list().select(BELL_ID,TIME_START,TIME_END);
        depart = db.depart().select(DEPART_ID,DEPART_LABEL);
        subject = db.subject();
        room = db.room();
        group_label = db.group_label();
        changes = db.changes();
        schedule = db.schedule();
        attributes = new Values();
        for(Values values: db.attributes().toValues()){
            attributes.put(values.getString(PARAM_NAME),values.get(PARAM_VALUE));
        }
    }
    
    public void init(){
        view.setDimension(0, 0);
        for(int i = 0;i<day_list.size();i++){
            view.addRow(new ValuesHeader((String)day_list.get(i)[1],day_list.getValues(i).getValues(DAY_ID)));
            for (int j=0;j<bell_list.size();j++){
                Values y = bell_list.getValues(j).getValues(BELL_ID);
                y.put(DAY_ID, day_list.get(i)[0]);
                view.addRow(new ValuesHeader((String)bell_list.get(j)[1],y));
            }
        }
        for(int col=0;col<depart.size();col++){
            view.addColumn(new ValuesHeader((String)depart.get(col)[1], depart.getValues(col).getValues(DEPART_ID)));
        }
        view.rebuild();
    }
        
    public void setDate(Date date) throws Exception{
        setDate(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }
        
    public void setDate(String str) throws Exception{

        // Закголовки строк
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(str);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        for(int row=0;row<view.rowCount();row++){
            ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
            if (vh.values.containsKey(DAY_ID) && !vh.values.containsKey(BELL_ID)){
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, vh.values.getInteger(DAY_ID)-1);
                vh.caption = new SimpleDateFormat("E dd MMM").format(c.getTime());
            }            
        }
                
        // Заполнение сетки
        Recordset recordset = new ScheduleRecordset(schedule, changes,date)
                .join(subject, SUBJECT_ID).join(depart, DEPART_ID).left(room, ROOM_ID).left(teacher, TEACHER_ID).left(group_label, DEPART_ID,GROUP_ID,SUBJECT_ID);
        
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
    
}

class ScheduleTitle extends JComponent implements IDataModel{
    JLabel schedulePeriod = new JLabel("schedulePeriod");

    public ScheduleTitle() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        schedulePeriod.setHorizontalAlignment(JLabel.CENTER);
        schedulePeriod.setBorder(new EmptyBorder(12,6,12,16));
        add(schedulePeriod);
    }
    
    public void setValues(Values attributes){
        String date1 = attributes.getString(DATE_BEGIN);
        String date2 = attributes.getString(DATE_END);
        schedulePeriod.setText(date1+" "+date2);
        
    }
    
}


public class ScheduleView extends View implements IDataModel{
    
    ScheduleTitle title = new ScheduleTitle();
    ViewModel model;
    
    StatusBar statusBar = new StatusBar();

    public void setStatusTest(String str) {
        statusBar.setStatusTest(str);
    }
    
    
    Date date = new Date();
    
    public void setDate(Date date){
        try{

            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            this.date=c.getTime();
            model.setDate(this.date);
            title.schedulePeriod.setText(new SimpleDateFormat("E dd MMM yyyy").format(this.date));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public Item createItem(Values values){
        return new ScheduleItem(values);
    }
    
    private class ScheduleItem extends Item<Values>{
        String depart_label;
        String group_label;
        String room_name;
        String subject_name;
        Color color;
        int flag;
        int lesson_no;
        String hint;

        public ScheduleItem(Values data) {
            super(data);
//            depart_label = data.getString(DEPART_LABEL);
            flag = data.isValue(FLAG)?data.getInteger(FLAG):-1;
            group_label = data.getString(GROUP_LABEL);
            room_name = data.getString(ROOM_NAME);
            subject_name = data.getString(SUBJECT_NAME);
            lesson_no = data.getInteger(LESSON_NO);
            String ss = data.getString(COLOR);
            String[] c = ss.split("\\s");
            color = new Color(Integer.valueOf(c[0]),Integer.valueOf(c[1]),Integer.valueOf(c[2]));
            hint = data.getValues(DEPART_LABEL,GROUP_LABEL,SUBJECT_NAME,ROOM_NAME,LAST_NAME).toHtml();
        }

        @Override
        public void draw(Graphics g) {
            if (bound!=null){
                if (flag == FLAG_CANCEL || flag == IReplacement.FLAG_MOVE_TO){
                    g.setColor(Color.WHITE);
//                    g.setFont(g.getFont().deriveFont(Font.ITALIC));
                } else
                    g.setColor(color);
                    
                g.fillRect(bound.x, bound.y, bound.width-1, bound.height-1);
                FontMetrics fm = g.getFontMetrics();
//                int x = bound.x;
                int y = (bound.height-fm.getHeight())/2+fm.getHeight()-fm.getDescent();
                
                switch(flag){
                    case FLAG_CANCEL:
                    case FLAG_MOVE_TO:
                        g.setColor(Color.LIGHT_GRAY);
                        break;
                    case FLAG_REPLACE:
                    case FLAG_MOVE_FROM:    
                        g.setColor(Color.RED);
                        break;
                    default:
                        g.setColor(Color.BLACK);
                }
                
                g.drawString(lesson_no+".", bound.x+5, bound.y+y);
                
                g.drawString(subject_name +(group_label==null?"":group_label) , bound.x+20, bound.y+y);
                
                if(room_name!=null)
                    g.drawString(room_name, bound.x+(bound.width-40), bound.y+y);
                
                
            }
        }

        @Override
        public String toString() {
            return hint;
        }
        
        
    }

    @Override
    public void rowClick(int row, MouseEvent e) {
        Object p = model.getRowHeaderData(row);
        System.out.println(p);
    }

    @Override
    public void columnClick(int column, MouseEvent e) {
        Object p = model.getColumnHeaderData(column);
        System.out.println(p);
    }
                
    public ScheduleView() {
        setRowHeaderWidth(120);
        setRowHeight(20);
        setColumnWidth(200);
        this.model = new ViewModel(this);
    }
    
    public void setModel(ViewModel model){
        this.model = model;
    }
    
    public void periodChange(){
    }
    
    public Values getPeriod(){
        return model.attributes;
    }
    
    public void open(IDB idb) throws Exception{
        
        setVisible(false);
        
        model.open(idb);
        model.init();
        setVisible(true);        
        title.setValues(model.attributes);
        periodChange();
    }
            
    public static void main(String[] args)throws Exception{
        
        ScheduleView view = new ScheduleView();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        IDB db = new DB_JSON_decoder(new File(MyServer.SERVER_DATA));
        view.open(db);
        
    }
    
}
