/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.awt.event.ActionEvent;
import javafx.scene.control.ToggleButton;
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

    JButton createButton(String command) {
        Action a = new AbstractAction(command) {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
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
        return button;
                
    }
    
}
