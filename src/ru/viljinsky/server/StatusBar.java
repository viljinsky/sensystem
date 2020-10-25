/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author viljinsky
 */
public class StatusBar extends JComponent {
    JLabel label = new JLabel("statusbar");

    public StatusBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        add(label);
    }

    public void setText(String str) {
        label.setText(str);
    }
    
}
