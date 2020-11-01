/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

public class ScheduleView extends View implements IDataModel{
    
    Date date_begin,date_end,date = new Date();;
    
    Recordset teacherFilter;
    Recordset departFilter;
    
    Recordset day_list,bell_list,depart,room,subject,group_label,schedule,changes,building,teacher,skill,curriculum,profile,subject_group;
    
    Values attributes;
       
//    ScheduleTitle title = new ScheduleTitle();
    
    public ViewModel model;
    
    StatusBar statusBar = new StatusBar();

    public void setStatusTest(String str) {
        statusBar.setText(str);
    }
    
    public Date getDate(){
        return date;
    }
    
    public void setDate(Date date){
        
        try{
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            this.date=c.getTime();
            model.setDate(this.date);
//            title.setDate(this.date);//schedulePeriod.setText(new SimpleDateFormat("E dd MMM yyyy").format(this.date));
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
                
                g.drawString(subject_name +(group_label==null?"":" "+group_label) , bound.x+20, bound.y+y);
                
                if(room_name!=null)
                    g.drawString(room_name, bound.x+(bound.width-40), bound.y+y);
                if (selected){
                    g.setColor(Color.BLUE);
                    g.drawRect(bound.x+2,bound.y+2,bound.width-5,bound.height-5);
                }
                
            }
        }

        @Override
        public String toString() {
            return hint;
        }
        
        
    }

    @Override
    public void cellClick(Cell cell, MouseEvent e) {
        System.out.println(cell);
       model.getColumnData(cell.col).print();
       model.getRowData(cell.row).print();
       for(Cell c:cells()){
           if (c.col==cell.col || c.row==cell.row){
               c.background = Color.LIGHT_GRAY;
           } else {
               c.background = null;
           }
       }
       repaint();

    }
    
    

    @Override
    public void rowClick(int row, MouseEvent e) {
        model.getRowData(row).print();

        for(Item item:items()){
            item.selected = item.cell.row == row;
        }
        
        repaint();
    }

    @Override
    public void columnClick(int column, MouseEvent e) {
        model.getColumnData(column).print();
    }
                
    public ScheduleView() {
        setRowHeaderWidth(120);
        setRowHeight(20);
        setColumnWidth(200);
        this.model = new Model1();
        
        
    }
    
    public ScheduleView(ViewModel model){
        setRowHeaderWidth(120);
        setRowHeight(20);
        setColumnWidth(200);
        this.model = model;
    }
    
    public void setModel(ViewModel model){
        this.model = model;
        try{
            model.setView(this);
            model.init();
            model.setDate(date);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public void periodChange(){
    }
    
    public Values getPeriod(){
        return model.attributes();
    }
    
    public void open(IDB db) throws Exception{
        
        setVisible(false);
        model.setView(this);
        teacher = db.teacher();
        building = db.building();
        day_list = db.day_list().select(DAY_ID,DAY_NAME);
        bell_list = db.bell_list().select(BELL_ID,TIME_START,TIME_END);
        depart = db.depart();//.select(DEPART_ID,DEPART_LABEL);
        subject = db.subject();
        room = db.room();
        group_label = db.group_label();
        changes = db.changes();
        schedule = db.schedule();
        attributes = new Values();
        for(Values values: db.attributes().toValues()){
            attributes.put(values.getString(PARAM_NAME),values.get(PARAM_VALUE));
        }
        skill = db.skill();
        curriculum = db.curiculum();
        profile = db.profile();
        
        date_begin = new SimpleDateFormat("yyyy-MM-dd").parse(attributes.getString(DATE_BEGIN));
        date_end = new SimpleDateFormat("yyyy-MM-dd").parse(attributes.getString(DATE_END));
        subject_group = db.subject_group();
                
        
        model.init();
        setVisible(true);        
//        title.setValues(model.attributes());
        
        periodChange();
    }
    
    static ScheduleView view;
            
    public static void main(String[] args)throws Exception{
               
        view = new ScheduleView();
        ViewControl viewControl = new ViewControl(view);
        JFrame frame = new JFrame();        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(view));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(viewControl);
        panel.add(new FilterPanel(view));
        
        frame.add(panel,BorderLayout.PAGE_START);
                
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        
        frame.add(new DateControl(){

            @Override
            public void change() {
                view.setDate(getDate());
            }
            
        },BorderLayout.PAGE_END);
        
//        IDB db = new DB_JSON_decoder(new File(MyServer.SERVER_DATA));
        IDB db = new DB_JSON_decoder(new File("timetabler.json"));
        view.open(db);
        view.setDate(new Date());
        
        frame.setVisible(true);
        
    }
    
}
