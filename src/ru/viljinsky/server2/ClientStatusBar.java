/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import ru.viljinsky.websocket.WebSocketClient;

/**
 *
 * @author viljinsky
 */
class ClientStatusBar extends JPanel {
    JLabel label = new JLabel("statusbar");
    List<Action> actions = new ArrayList<>();

    private JButton createButtom(String name) {
        Action a = new AbstractAction(name) {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCommand(e.getActionCommand());
            }
        };
        actions.add(a);
        return new JButton(a);
    }

    public ClientStatusBar() {
        setBorder(new EmptyBorder(12, 6, 12, 6));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(label);
        add(Box.createHorizontalGlue());
        add(createButtom("start"));
        add(createButtom("stop"));
        add(createButtom("reload"));
        add(createButtom("config"));
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void doCommand(String command) {
        System.out.println(command);
    }

    public void updateButtons(int state) {
        for (Action a : actions) {
            switch ((String) a.getValue(Action.ACTION_COMMAND_KEY)) {
                case "start":
                    a.setEnabled(state == WebSocketClient.CLOSED);
                    break;
                case "stop":
                    a.setEnabled(state == WebSocketClient.READY);
                    break;
                case "reload":
                    a.setEnabled(state == WebSocketClient.READY);
                    break;
            }
        }
    }
    
}
