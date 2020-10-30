/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author viljinsky
 */
public class DateControl extends JPanel{
    
    JLabel label = new JLabel("date");
    public static final String THIS_WEEK = "this_week";
    public static final String NEXT_WEEK = "next_week";
    public static final String FIRST_WEEK = "first_week";
    public static final String PRIOR_WEEK = "prior_week";
    public static final String LAST_WEEK = "last_week";
    
    String first_date = "2020-09-01";
    String last_date = "2020-10-25";
    
    private JButton createButton(String command){
        Action a = new AbstractAction(command) {

            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        JButton button = new JButton(a);
        return button;
    }
    
    void firstWeek() throws Exception{        
        setDate(SIMPLE_DATE_FORMAT.parse(first_date));
    }
    
    void priorWeek(){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_WEEK, -7);
        setDate(c.getTime());
    }
    void nextWeek(){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_WEEK, 7);
        setDate(c.getTime());
    }
    void lastWeek() throws Exception{
        setDate(SIMPLE_DATE_FORMAT.parse(last_date));
    }
    
    void thisWeek(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        setDate(c.getTime());
    }
    
    void doCommand(String command){
        try{
            switch(command){
                case THIS_WEEK:
                    thisWeek();
                    break;
                case FIRST_WEEK:
                    firstWeek();
                    break;
                case PRIOR_WEEK:
                    priorWeek();
                    break;
                case NEXT_WEEK:
                    nextWeek();
                    break;
                case LAST_WEEK:
                    lastWeek();
                    break;
            }
        } catch (Exception e){
            JOptionPane.showMessageDialog(getParent(), e.getMessage());
        }
                
    }
    public DateControl() {
        this(new Date());
    }
    
    Date date;
    SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat SIMPLE_DATE_FORMAT2 = new SimpleDateFormat("dd MMM");
    SimpleDateFormat SIMPLE_DATE_FORMAT3 = new SimpleDateFormat("dd MMM yyyy");
    
    public void setDate(Date date){
        try{           
            this.date =SIMPLE_DATE_FORMAT.parse(SIMPLE_DATE_FORMAT.format(date));
            Calendar c = Calendar.getInstance();
            c.setTime(this.date);
            String text = "c "+SIMPLE_DATE_FORMAT2.format(c.getTime());
            c.add(Calendar.DAY_OF_MONTH, 7);
            text += " по " + SIMPLE_DATE_FORMAT3.format(c.getTime());
                    
            label.setText(text);
            change();
        } catch (Exception e){
        }
    }
    
    public Date getDate(){
        return date;
    }
    
    public void change(){
    }
    
    public DateControl(Date date) {
        
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        setDate(c.getTime());
        
        setBorder(new EmptyBorder(12,6,12,6));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(createButton(FIRST_WEEK));
        add(createButton(PRIOR_WEEK));
        add(Box.createHorizontalGlue());
        add(label);
        add(Box.createHorizontalGlue());
        add(createButton(NEXT_WEEK));
        add(createButton(LAST_WEEK));
        add(createButton(THIS_WEEK));
    }
    
    
}
