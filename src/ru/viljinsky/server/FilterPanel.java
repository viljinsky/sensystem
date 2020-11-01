/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import ru.viljinsky.project2019.BaseDialog;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;

class ItemPanel extends JPanel{

    Recordset recordset;
    String fieldName;

    class FilterItem extends JCheckBox{
        Values values ;
        public FilterItem(String caption,Values values) {
            this.values=values;
            setText(caption);
        }        
    }

    String getCaption(Values values){
        return values.getString(fieldName);
    }

    public ItemPanel(Recordset recordset,String fieldName) {
        this.recordset = recordset;
        this.fieldName = fieldName;
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        for(int i=0;i<recordset.size();i++){
            Values values = recordset.getValues(i);
            add(new FilterItem(getCaption(values),values));
        }
    }

    public void clear(){
        removeAll();
    }

    public void clearSelected(){
        for(int i=0;i<getComponentCount();i++){
            FilterItem item = (FilterItem)getComponent(i);
            item.setSelected(false);
        }
    }

    public void selectAll(){
        for(int i=0;i<getComponentCount();i++){
            FilterItem item = (FilterItem)getComponent(i);
            item.setSelected(true);
        }
    }

    Recordset getSelectedValues(){
        Recordset result = new Recordset(fieldName);
        for(int n=0;n<getComponentCount();n++){
            FilterItem item = (FilterItem)getComponent(n);
            if (item.isSelected()){
                result.add(new Object[]{item.values.get(fieldName)});
            }
        }
        return result;
    }
    
    void setSelectedValues(Recordset recordset){
        if (recordset == null) return;
        for(int i=0;i<getComponentCount();i++){
            FilterItem item = (FilterItem)getComponent(i);
            item.setSelected((!recordset.filter(item.values.getValues(fieldName)).isEmpty()));
        }
    }

}

abstract class FilterContent extends JPanel implements IDataModel{
    
    static final String APPLY = "apply";
    static final String CLERA = "clear";
    static final String SELECT_ALL = "select_all";
    String description = "Отметьте элементы, которые будут отображениы в сетке";
    
    ItemPanel itemPanel ;
    
    ScheduleView view;
    String caption = "Фильтры";
    JLabel label = new JLabel(description);
    
    public abstract ItemPanel createInnerPanel();
    public abstract void applyFilter() throws Exception;

    public FilterContent(ScheduleView view) {
        this.view = view;
        setLayout(new BorderLayout());        
        itemPanel = createInnerPanel();
        add(new JScrollPane(itemPanel));
        add(commandBar,BorderLayout.PAGE_END);       
        label.setBorder(new EmptyBorder(12,6,12,6));
        add(label,BorderLayout.PAGE_START);
        setBorder(new EmptyBorder(12,6,12,6));
    }
    
    public void setDescription(String string){
        description = string;
        label.setText(description);
    }
    
    CommandBar commandBar = new CommandBar(APPLY,CLERA,SELECT_ALL){

        @Override
        public void doCommand(String command) {
            try{
                switch(command){
                    case APPLY:
                        applyFilter();
                        break;

                    case CLERA:
                        itemPanel.clearSelected();
                        break;
                        
                    case SELECT_ALL:
                        itemPanel.selectAll();
                        break;
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    };
    
        
    JDialog dialog;
    public void showModal(Component parent){
        
        dialog = new BaseDialog();
        dialog.setTitle(caption);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
       
//        dialog.setContentPane(this);
        dialog.add(this);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

    }
        
}

class TeacherFilter extends FilterContent{

    @Override
    public ItemPanel createInnerPanel() {
        
        ItemPanel p = new ItemPanel(view.teacher,TEACHER_ID){

            @Override
            String getCaption(Values values) {
                return values.get(LAST_NAME)+" "+values.get(FIRST_NAME)+" "+values.get(PATRONYMIC);
            }

        };
        p.setSelectedValues(view.teacherFilter);
        return p;
    }

    @Override
    public void applyFilter() throws Exception{
        itemPanel.getSelectedValues().print();
        Recordset r = itemPanel.getSelectedValues();
        if (!r.isEmpty()){
            view.teacherFilter = r;
            Recordset t = view.subject_group.select(TEACHER_ID,DEPART_ID).join(r, TEACHER_ID).count(DEPART_ID);
            view.departFilter = t.select(DEPART_ID);
        } else
            view.teacherFilter = null;
        view.model.init();
        view.setDate(view.getDate());
        
    }
        
    public TeacherFilter(ScheduleView view) {
        super(view);
    }

}

class DepartFilter extends FilterContent{

    public DepartFilter(ScheduleView view) {
        super(view);
        
    }

    @Override
    public ItemPanel createInnerPanel() {
        ItemPanel p = new ItemPanel(view.depart, DEPART_ID){

            @Override
            String getCaption(Values values) {
                return values.getString(DEPART_LABEL);
            }
            
        };
        p.setSelectedValues(view.departFilter);
        return p;
    }

    @Override
    public void applyFilter() throws Exception {
        itemPanel.getSelectedValues().print();
        Recordset r = itemPanel.getSelectedValues();
        if (!r.isEmpty()){
            view.departFilter = r;
            Recordset t = view.subject_group.select(TEACHER_ID,DEPART_ID).join(r,DEPART_ID).select(TEACHER_ID).count(TEACHER_ID);
//            t.print();
            view.teacherFilter = t.select(TEACHER_ID);
        }    
        else {
            view.departFilter = null;
        }
        view.model.init();
        view.setDate(view.getDate());
    }
    
    
    
}

/**
 *
 * @author viljinsky
 */
public class FilterPanel extends JPanel implements IDataModel{
    
    ScheduleView view;
        
    static final String FILTER1 = "Классы";
    static final String FILTER2 = "Преподаватели";
//    static final String FILTER3 = "filter3";
//    static final String FILTER4 = "filter4";
    
    private JButton createButton(String command){
        Action a = new AbstractAction(command) {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        return new JButton(a);        
    }
    
    void doCommand(String command){
        switch(command){
            case FILTER1:
                
                new DepartFilter(view).showModal(getRootPane());
                break;
                
            case FILTER2:
                new TeacherFilter(view).showModal(getRootPane());
                break;
        }
    }

    public FilterPanel(ScheduleView view) {
        this.view = view;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(createButton(FILTER1));
        add(createButton(FILTER2));
//        add(createButton(FILTER3));
//        add(createButton(FILTER4));
        
    }
    
    public void showInFrame(){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    };
    
    public static void main(String[] args) throws Exception{
        ScheduleView scheduleView = new ScheduleView();
        IDB db = new DB_JSON_decoder(new File("timetabler.json"));
        scheduleView.open(db);
        FilterPanel filterPanel = new FilterPanel(scheduleView);
        filterPanel.showInFrame();
        
    }
    
}
