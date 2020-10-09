/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.Document;

/**
 *
 * @author viljinsky
 */
public class MessagePane extends JPanel {
    JTextArea textArea = new JTextArea();
    Action delete = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            clear();
        }
    };

    public void clear(){
        try {
            textArea.getDocument().remove(0, textArea.getDocument().getLength());
        } catch (Exception es) {
        }
    }
    public MessagePane() {
        setMinimumSize(new Dimension(400, 200));
        setLayout(new BorderLayout());
        add(new JScrollPane(textArea));
        ActionMap am = textArea.getActionMap();
        am.put("delete", delete);
        InputMap im = textArea.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.DIALOG_INPUT,Font.PLAIN,12));
    }

    public void textOut(String text) {
        try {
            Document doc = textArea.getDocument();
            doc.insertString(doc.getLength(), text, null);
            textArea.setCaretPosition(doc.getLength());
        } catch (Exception e) {
        }
    }
    
    public void saveToFile(File file) throws Exception{
        try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));){
            Document doc = textArea.getDocument();
            writer.write(doc.getText(0,doc.getLength()));
        }
    }
}
