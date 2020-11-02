/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.Component;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.values.ValuesFieldInteger;
import ru.viljinsky.project2019.values.ValuesPanel;

/**
 *
 * @author viljinsky
 */
class ServerConnection {
    
    int port = 7035;
    static final String PORT = "port";

    public void setConnection(Values valeus) {
        port = valeus.getInteger(PORT);
    }

    public Values getConnection() {
        Values values = new Values();
        values.put(PORT, port);
        return values;
    }

    public boolean config(Component parent) throws Exception {
        ValuesPanel valuesPanel = new ValuesPanel();
        valuesPanel.setFields(new ValuesFieldInteger("port"));
        valuesPanel.setValues(getConnection());
        if (valuesPanel.showModal(parent)) {
            setConnection(valuesPanel.getValues());
            return true;
        }
        return false;
    }
    
    public void read(){
        IniFile2 iniFile2 = new IniFile2();
        iniFile2.read();
        Values v = iniFile2.getValues("web_server");
        if (!v.isEmpty()){
            v.put(PORT, Integer.valueOf(v.getString(PORT)));
        } else {
            v.put(PORT, 7035);
        }
        setConnection(v);                    
    }
    
    public void save(){
        IniFile2 iniFile2 = new IniFile2();
        iniFile2.read();
        iniFile2.setValues("web_server", getConnection());
        iniFile2.save();
    }
    
}
