/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 *
 * @author viljinsky
 */

public class ViewControl extends JPanel {
    public static final String MODEL1 = "model1";
    public static final String MODEL2 = "model2";
    public static final String MODEL3 = "model3";
    public static final String MODEL4 = "model4";
    public static final String MODEL5 = "model5";
    
    ScheduleView view;
    ButtonGroup buttonGroup = new ButtonGroup();

    private JToggleButton createButton(String command) {
        
        Action a = new AbstractAction(command) {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        JToggleButton button = new JToggleButton(a);
        buttonGroup.add(button);
        return button;
    }

    public ViewControl(ScheduleView view) {
        this.view = view;
        add(createButton(MODEL1));
        add(createButton(MODEL2));
        add(createButton(MODEL3));
        add(createButton(MODEL4));
        add(createButton(MODEL5));
    }

    public void doCommand(String command) {
        System.out.println(command);
        ViewModel model;
        switch (command) {
            case MODEL1:
                model = new Model1();
                break;
            case MODEL2:
                model = new Model2();
                break;
            case MODEL3:
                model = new Model3();
                break;
            case MODEL4:
                model = new Model4();
                break;
            case MODEL5:
                model = new Model5();
                break;
            default:
                return;
        }
        new Thread(){

            @Override
            public void run() {
                view.setModel(model);
            }
            
        }.start();
    }
    
}
