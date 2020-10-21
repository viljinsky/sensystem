/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.Values;

/**
 *
 * @author viljinsky
 */
abstract class AbstractFilter extends JComponent implements IDataModel {
    ScheduleView view;

    public void valuesClick(Values values) {
    }

    public void doCommand(String comand) {
    }

    public AbstractFilter(ScheduleView view) {
        this.view = view;
        setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    class ValuesButton extends JButton {

        Values values;

        public ValuesButton(String name, Values values) {
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

    JButton createButton(String command) {
        Action a = new AbstractAction(command) {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        JButton button = new JButton(a);
        return button;
    }
    
}


class SkillFilter extends AbstractFilter{

    public SkillFilter(ScheduleView view) {
        super(view);
    }

    public void setValues(Recordset recordset){
        removeAll();
        add(new ValuesButton("Все", null));
        for(int i=0;i<recordset.size();i++){
            Values values = recordset.getValues(i);
            add(new ValuesButton((String)values.get(SKILL_NAME), values));
        }        
    }

    @Override
    public void valuesClick(Values values) {
//        view.setDepartFilter(values);
    }
               
}

class CurriculumFilter extends AbstractFilter{
    
    @Override
    public void valuesClick(Values values){
//        view.setDepartFilter(values);
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

}

class ProfileFilter extends JPanel{

    public ProfileFilter() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(new JButton("Profile"));
    }

}
    