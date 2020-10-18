/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.sensystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author viljinsky
 */
public class ValuesPanel extends JPanel {

    class ValuesPanelField extends JPanel implements DocumentListener {

        String fieldName;
        Object value;
        JLabel label = new JLabel();
        JTextField text = new JTextField("jkj");

        @Override
        public void insertUpdate(DocumentEvent e) {
            value = text.getText();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            value = text.getText();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            value = text.getText();
        }

        public ValuesPanelField(String fieldName) {
            setBorder(new EmptyBorder(0, 6, 12, 6));
            this.fieldName = fieldName;
            label.setText(fieldName);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(label);
            add(Box.createHorizontalStrut(6));
            add(text);
            text.getDocument().addDocumentListener(this);
        }

        public ValuesPanelField(String fieldName, Object value) {
            this(fieldName);
            setValue(value);
        }

        public void setValue(Object value) {
            this.value = value;
            if (value != null) {
                text.setText(value.toString());
            }
        }

        public Object getValue() {
            return value;
        }
    }

    public ValuesPanel() {
        setBorder(new EmptyBorder(12, 0, 0, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    
    public ValuesPanel(HashMap<String, Object> map) {
        this();
        for (String key : map.keySet()) {
            add(new ValuesPanelField(key, map.get(key)));
        }
    }
    
    public void addField(String fieldName){
        add(new ValuesPanelField(fieldName));
    }
    
    public void addField(String fieldName,Object value){
        add(new ValuesPanelField(fieldName, value));
    }
    
    public void setValues(Map<String, Object> map) {
        for (String key : map.keySet()) {
            add(new ValuesPanelField(key, map.get(key)));
        }
    }

    public Map<String, Object> getValues() {
        HashMap hashMap = new HashMap();
        for (int i = 0; i < getComponentCount(); i++) {
            ValuesPanelField vfp = (ValuesPanelField) getComponent(i);
            hashMap.put(vfp.fieldName, vfp.value);
        }
        return hashMap;
    }

    ValuesPanelField valuesPanelField(String fieldName) {
        for (int i = 0; i < getComponentCount(); i++) {
            ValuesPanelField vpf = (ValuesPanelField) getComponent(i);
            if (vpf.fieldName.equals(fieldName)) {
                return vpf;
            }
        }
        return null;
    }

    public Object getValue(String fieldName) throws Exception {
        ValuesPanelField vfp = valuesPanelField(fieldName);
        if (vfp == null) {
            throw new Exception("field " + fieldName + " not found");
        }
        return vfp.getValue();
    }

    public String getString(String fieldName) throws Exception {
        Object v = getValue(fieldName);
        if (v == null) {
            return null;
        }
        return v.toString();
    }

    public void setValue(String fieldName, Object value) {
        ValuesPanelField vfp = valuesPanelField(fieldName);
        if (vfp != null) {
            vfp.setValue(value);
        }
    }
    
//    public Map getValues(){
//        HashMap map = new HashMap();
//        return map;
//    }
    
    public void saveValues(String fileName){
        File file = new File(fileName);
        try(BufferedWriter write = new BufferedWriter(new FileWriter(file));){
            for(int i=0;i<getComponentCount();i++){
                ValuesPanelField vf = (ValuesPanelField)getComponent(i);
                write.write(String.format("%s=%s\n",vf.fieldName,vf.value==null?"null":vf.value.toString()));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void readValues(String fileName){
        File file = new File(fileName);
        if(file.exists()){
            try(BufferedReader reader = new BufferedReader(new FileReader(file));){
                String line;
                while((line=reader.readLine())!=null){
                    String[] p = line.split("=");
                    if (p.length==2){
                        setValue(p[0], p[1]);
                    }
                }                    
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    
    
}
