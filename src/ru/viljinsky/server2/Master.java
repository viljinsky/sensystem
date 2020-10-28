/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.awt.Dimension;
import java.sql.Connection;
import javax.swing.SwingUtilities;
import org.json.JSONObject;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Values;
import ru.viljinsky.project2019.values.ValuesField;
import ru.viljinsky.project2019.values.ValuesFieldString;
import ru.viljinsky.project2019.values.ValuesPanel;
import ru.viljinsky.server.DB_JSON_encoder;
import ru.viljinsky.websocket.WebSocketClient;
import ru.viljinsky.websocket.WebSocketPanel;

/**
 *
 * @author viljinsky
 */
public class Master extends WebSocketPanel{
    
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String CONFG = "confg";
    public static final String SEND = "send";
    public static final String MASTER = "master";
    
    class MasterClient extends WebSocketClient{

        public MasterClient(String host, int port) {
            super(host, port);
        }

        @Override
        public void onStateChange(int state) {
            switch(state){
                case READY:
                    setStatus("Соединение установлено");
                    try{
                        send("master");
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                    
                case WAIT:
                    setStatus("Ожидание соединения...");
                    break;
                    
                case CLOSED:
                    setStatus("Соединения разорвано");
                    break;
            }
        }                        
    }

    @Override
    public void onClosing() {
        if(client!=null)
            client.stop();
    }

    String host="localhost";
    int port=3345;
    
    MasterClient client;
    
    void config() throws Exception{
        ValuesPanel valuesPanel = new ValuesPanel();
        ValuesField[] fields = new ValuesField[]{
            new ValuesFieldString("host"),
            new ValuesFieldString("port")
        };        
        valuesPanel.setFields(fields);
        valuesPanel.setValues(new Values("host",host));
        valuesPanel.setValues(new Values("port",port));
        if (valuesPanel.showModal(getParent())){
            Values values = valuesPanel.getValues();
            host = values.getString("host");
            port = Integer.valueOf(values.getString("port"));
        };        
    }
    
    void start(){
        if(client==null){
            client = new MasterClient("localhost",3345);
            client.start();
        }
    }
    
    void stop(){
        if(client!=null){
            client.stop();
            client = null;
        }
    }
    
    void send() throws Exception{
        DataModel.setConnection("моё расписание.db");
        try(Connection con = DataModel.getConnection()){
            JSONObject obj = new DB_JSON_encoder(con);
            client.send(obj.toString());
        }
        
    }

    @Override
    public void doCommand(String command) {
        try{
            switch(command){
                case CONFG:
                    config();
                    break;
                case START:
                    start();
                    break;
                case STOP:
                    stop();
                    break;
                case SEND:
                    send();
                    break;
                case MASTER:
                    client.send("master");
                    break;
            }
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }
    
    

    public Master() {
        setPreferredSize(new Dimension(800,600));
        setTitle("Master");
        setCommand(START,STOP,null,CONFG,SEND,MASTER);
        
    }
    
    
    
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{ new Master().showInFrame(null);});
    }
    
}
