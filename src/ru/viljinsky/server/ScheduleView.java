/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import ru.viljinsky.calendars.CalendarView;
import ru.viljinsky.cells7.Cell;
import ru.viljinsky.cells7.Item;
import ru.viljinsky.cells7.View;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.server.IDB;
import ru.viljinsky.tcp.CommandBar;

/**
 *
 * @author viljinsky
 */

class StatusBar extends Container{
    JLabel label = new JLabel("statusbar");

    public StatusBar() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        add(label);
    }

    public void setStatusTest(String str){
        label.setText(str);
    }

}



class ScheduleViewControl extends JPanel implements IDataModel{
    
    
    
    CommandBar commandBar = new CommandBar("RELOAD"){

        @Override
        public void doCommand(String command) {
            try{
                Proc.query(con->{
                    IDB idb = new DB_SQLITE(con);
                    view.open(idb);
                    view.setDate(date);
            });
            } catch(Exception e){
            }
        }
        
    };
    ScheduleView view;
    CalendarView calendarView = new CalendarView(){

        @Override
        public void change() {
            try{
                Date date = getSelectionDate();
                if(date!=null){
                    ScheduleViewControl.this.setDate(date);
                }
            } catch (Exception e){
            }
        }
        
    };
    JLabel label = new JLabel("date");

    public ScheduleViewControl(ScheduleView panel){
        view = panel;
        view.control = this;
        setLayout(new BorderLayout());
        add(new JScrollPane(calendarView),BorderLayout.WEST);
        add(new JScrollPane(view));
        add(commandBar,BorderLayout.PAGE_END);
        label.setBorder(new EmptyBorder(12,6, 12, 6));
        add(label,BorderLayout.PAGE_START);
    }
    
    Date date ;
    int week_id;
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    SimpleDateFormat sdf2 = new SimpleDateFormat("E dd MMM yyyy");
    
    public void setDate(Date date) throws Exception{
        setDate(sdf.format(date));
    }
    
    public void setDate(String date) throws Exception{
        this.date = sdf.parse(date);
        Calendar c = Calendar.getInstance();
        c.setTime(this.date);
        this.week_id = c.get(Calendar.WEEK_OF_YEAR);
        
        label.setText(sdf2.format(this.date)+ "  ("+(week_id % 2 == 0?"Четная неделя":"Нечётная неделя")+")");
        view.setDate(this.date);
    }
               
    public void setPeriod(Values values){
        setPeriod(values.getString(DATE_BEGIN),values.getString(DATE_END));
    }
    public void setPeriod(String date1,String date2){
        calendarView.setMonth(date1, date2);
        calendarView.setPeriod(date1, date2);
    }
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
    
    Recordset day_list,bell_list,depart,room,subject,group_label,schedule,replacement,building,teacher;
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
        schedule = db.schedule();
        schedule = schedule.join(subject, SUBJECT_ID).join(depart, DEPART_ID).left(room, ROOM_ID).left(teacher, TEACHER_ID).left(group_label, DEPART_ID,GROUP_ID,SUBJECT_ID);
        replacement = db.replacement().join(depart, DEPART_ID).join(subject, SUBJECT_ID).left(room, ROOM_ID).left(teacher,TEACHER_ID).left(group_label, DEPART_ID,GROUP_ID,SUBJECT_ID);
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
    
    // Список изменений на период
    private Recordset weekReplacement(Date date1,Date date2) throws Exception{
        Recordset tmp = new Recordset(replacement);
        int index = tmp.columnIndex(DATE);
        for(Object p[] : replacement){
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse((String)p[index]);
            if (d.before(date1) || d.after(date2)){
                continue;
            }
            tmp.add(p);      
        }
        return tmp;
    }
    
    
    public void setDate(Date date) throws Exception{
        setDate(new SimpleDateFormat("yyyy-MM-dd").format(date));
    }
    
    
    public void setDate(String str) throws Exception{
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(str);
        Date date1 = date ;
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        int week_id = 2 - c.get(Calendar.WEEK_OF_YEAR) % 2;
        Integer day_id = c.get(Calendar.DAY_OF_WEEK);
        day_id = day_id==1?day_id=7:day_id-1;
        c.add(Calendar.DAY_OF_MONTH, 6);
        Date date2 = c.getTime();
        Recordset tmp = weekReplacement(date1,date2);
        
        view.clearItems();
        Rectangle r = null;
        for(int row=0;row<view.rowCount();row++){
            ValuesHeader vh = (ValuesHeader)view.getRowHeader(row);
            if (vh.values.containsKey(DAY_ID) && !vh.values.containsKey(BELL_ID)){
                c.setTime(date);
                c.add(Calendar.DAY_OF_MONTH, vh.values.getInteger(DAY_ID)-1);
                vh.caption = new SimpleDateFormat("E dd MMM").format(c.getTime());
                if (day_id.equals(vh.values.get(DAY_ID))){
                    r = view.cell(1,row).bound;
                }
            }            
        }
        
        for(Iterator<Values> it = schedule.getIterator();it.hasNext();){
            Values values = it.next();
                if (values.getInteger(WEEK_ID)==0 || values.getInteger(WEEK_ID)==week_id){
                Cell cell = findCell(values);
                if (cell!=null){
                    cell.addItem(view.createItem(values));
                }
            }
        }
        
        for(Iterator<Values> it = tmp.getIterator();it.hasNext();){
            Values values = it.next();
            Cell cell = findCell(values);
            if (cell!=null){
                values.put(COLOR, "255 0 0");
                cell.addItem(view.createItem(values));
            }
        }
        System.out.println("week_id ->"+week_id);
        
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
    
    ScheduleViewControl control;
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
        int lesson_no;
        String hint;

        public ScheduleItem(Values data) {
            super(data);
//            depart_label = data.getString(DEPART_LABEL);
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
                g.setColor(color);
                g.fillRect(bound.x, bound.y, bound.width, bound.height);
                g.setColor(Color.BLACK);
                FontMetrics fm = g.getFontMetrics();
//                int x = bound.x;
                int y = (bound.height-fm.getHeight())/2+fm.getHeight()-fm.getDescent();
                
                g.drawString(lesson_no+".", bound.x+5, bound.y+y);
                
                g.drawString(subject_name +(group_label==null?"":group_label) , bound.x+20, bound.y+y);
                
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
        if (control!=null){
            control.setPeriod(model.attributes);
            control.setDate(date);
        } else {
            model.setDate(date);
        }
        
        setVisible(true);
        
        title.setValues(model.attributes);
        periodChange();
    }
            
    public JComponent createControl(){
        control = new ScheduleViewControl(this);
        return control;
    }
    
    
    public static void main(String[] args)throws Exception{
        
        ScheduleView view = new ScheduleView();
        JComponent p = view.createControl();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(p);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        IDB db = new DB_JSON_decoder(new File(MyServer.SERVER_DATA));
        view.open(db);
        
    }
    
}
