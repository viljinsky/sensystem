/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.Component;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.values.ValuesField;
import ru.viljinsky.project2019.values.ValuesFieldInteger;
import ru.viljinsky.project2019.values.ValuesFieldString;
import ru.viljinsky.project2019.values.ValuesPanel;

/**
 *
 * @author viljinsky
 */
class ClientConnection {
    
    public static final String $HOSR = "Хост";
    public static final String $PORT = "Порт";
    public static String HOST = "host";
    public static String PORT = "port";
    public int port = 7035;
    public String host = "localhost";

    public Values getConnecteion() {
        Values values = new Values();
        values.put(HOST, host);
        values.put(PORT, port);
        return values;
    }

    public void setConnection(Values values) {
        port = values.getInteger(PORT);
        host = values.getString(HOST);
    }

    public boolean config(Component parent) throws Exception {
        ValuesPanel valuesPanel = new ValuesPanel();
        valuesPanel.setFields(new ValuesField[]{new ValuesFieldString(HOST), new ValuesFieldInteger(PORT)});
        valuesPanel.setValues(getConnecteion());
        if (valuesPanel.showModal(parent)) {
            setConnection(valuesPanel.getValues());
            return true;
        }
        return false;
    }
    
    String fileName = "connection.ini";    
    public void read(){
        
        IniFile2 iniFile2 = new IniFile2();
        iniFile2.read();
        Values v = iniFile2.getValues("web_client");
        if (v.isEmpty()){
            v.put(PORT, 7035);
            v.put(HOST, "localhost");
        } else {
            v.put(PORT, Integer.valueOf(v.getString(PORT)));
        }
        setConnection(v);
    }
    
    public void save(){
        
        IniFile2 iniFile2 = new IniFile2();
        iniFile2.read();
        iniFile2.setValues("web_client", getConnecteion());
        iniFile2.save();
                        
    }
    
}
