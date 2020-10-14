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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
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

abstract class AbstractControl extends JComponent implements IDataModel{
    
    ScheduleView view;

    public AbstractControl(ScheduleView view) {
        this.view = view;
        setLayout(new FlowLayout(FlowLayout.LEFT));
    }
    
        
    JButton createButton(String command){
        Action a = new AbstractAction(command) {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
                
        JButton button = new JButton(a);
        return button;
    }
    
    abstract void doCommand(String command);
}

class SkillFilter extends AbstractControl{

    public SkillFilter(ScheduleView view) {
        super(view);
    }

    public void setValues(Recordset recordset){
        removeAll();
        for(int i=0;i<recordset.size();i++){
            Values values = recordset.getValues(i);
            add(createButton(values.getString(IDataModel.SKILL_NAME)));
        }        
    }
       
    
    @Override
    void doCommand(String command) {
        System.out.println(command);
    }
}

class CurriculumFilter extends AbstractControl{
    
    void valuesClick(Values values){
        view.setDepartFilter(values);
    }
    
    class ValuesButton extends JButton{
        Values values;
        public ValuesButton(String name,Values values) {
            super(name);
            this.values = values;
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    valuesClick(values);
                }
            });
        }
    }
    

    public CurriculumFilter(ScheduleView view) {
        super(view);
    }
    
    public void setValues(Recordset recordset){
        removeAll();
        for(int i=0;i<recordset.size();i++){
            Values values = recordset.getValues(i);
            add(new ValuesButton((String)values.get(CURRICULUM_NAME),values.getValues(CURRICULUM_ID)));
        }
    }
            

    @Override
    void doCommand(String command) {
        System.out.println(command);
    }
}
class ViewControl extends AbstractControl{
    
    public static final String MODEL1 = "model1";
    public static final String MODEL2 = "model2";
    public static final String MODEL3 = "model3";

    public ViewControl(ScheduleView view) {
        super(view);
        add(createButton(MODEL1));
        add(createButton(MODEL2));
        add(createButton(MODEL3));
    }
    
    @Override
    void doCommand(String command){
        System.out.println(command);
        ViewModel model;
        switch(command){
            case MODEL1:
                model = new Model1();
                break;
            case MODEL2:
                model = new Model2();
                break;
            case MODEL3:
                model = new Model3();
                break;
            default:
                return;
        }
        view.setModel(model);
    }
    
}

class ScheduleTitle extends JComponent implements IDataModel{
    JLabel schedulePeriod = new JLabel("schedulePeriod");

    public ScheduleTitle() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        schedulePeriod.setHorizontalAlignment(JLabel.CENTER);
        schedulePeriod.setBorder(new EmptyBorder(12,6,12,16));
        add(schedulePeriod);
//        add(new ViewControl());
    }
    
    public void setValues(Values attributes){
        String date1 = attributes.getString(DATE_BEGIN);
        String date2 = attributes.getString(DATE_END);
        schedulePeriod.setText(date1+" "+date2);
        
    }
    
}

public class ScheduleView extends View implements IDataModel{
    
    Values departFilter = null;//new Values(CURRICULUM_ID,1);
    
    public void setDepartFilter(Values filter){
        departFilter = filter;
        try{
            model.init();
            model.setDate(date);
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }
    
    Recordset day_list,bell_list,depart,room,subject,group_label,schedule,changes,building,teacher,skill,curriculum;
    Values attributes;
    
    
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
                
                g.drawString(subject_name +(group_label==null?"":" "+group_label) , bound.x+20, bound.y+y);
                
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
//        Object p = model.getRowHeaderData(row);
//        System.out.println(p);
    }

    @Override
    public void columnClick(int column, MouseEvent e) {
//        Object p = model.getColumnHeaderData(column);
//        System.out.println(p);
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
//        model.open(idb);
        model.init();
        setVisible(true);        
        title.setValues(model.attributes());
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
