/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 *
 * @author viljinsky
 */
public class CommandBar extends JToolBar {
    
    List<Action> actions = new ArrayList<>();

    JButton createButton(String command) {
        Action a = new AbstractAction(command) {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        actions.add(a);
        return new JButton(a);
    }

    public void doCommand(String command) {
    }

    public CommandBar() {
        setFloatable(false);
    }

    public CommandBar(String... command) {
        this();
        for (String s : command) {
            if (s == null) {
                addSeparator();
            } else {
                add(createButton(s));
            }
        }
    }
    
    public JToggleButton addGroupCommand(String command,Boolean selected){
        Action a = new AbstractAction(command) {

            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        a.putValue(Action.SELECTED_KEY, selected);
        JToggleButton button = new JToggleButton(a);
        add(button);
        actions.add(a);
        return button;
                
    }
    
    public boolean isEnabled(String command){
        return true;
    }
    
    public void updateActions(){
        for(Action a: actions){
            a.setEnabled(isEnabled((String)a.getValue(Action.NAME)));
        }
    }
    
    public void setButtons(String... commands){
        actions.clear();
        removeAll();
        for(String s: commands){
            if (s==null)
                addSeparator();
            else
                add(createButton(s));
        }
    }
    
}
