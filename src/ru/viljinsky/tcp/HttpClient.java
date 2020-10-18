/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ru.viljinsky.sensystem.ValuesPanel;

/**
 *
 * @author viljinsky
 */
public class HttpClient extends JPanel{
    static final String POST = "POST";
    static final String GET = "GET";
    static final String START = "START";
    
    String host = "http://localhost:3345/page21";
    
    void textOut(String line){
        messagePane.textOut(line);
    }
    
    String query(Map values){
        StringBuilder sb = new StringBuilder();
        values.forEach((key,value)->{
            if (sb.length()!=0) sb.append("&");
            try {
                sb.append(URLEncoder.encode(String.valueOf(key),"utf-8"));
            } catch (UnsupportedEncodingException ex) {
//                Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(String.valueOf(value),"utf-8"));
            } catch (UnsupportedEncodingException ex) {
//                Logger.getLogger(HttpClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return sb.toString();
    }
    
    public void get(Map values) throws Exception{
        String query = query(values);
        URL url = new URL(host+(query.isEmpty()?"":"?"+query));
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        InputStream in = con.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in,"utf-8"));
        String line ;
        while ((line=reader.readLine())!=null){
            textOut(line+"\n");
        }
        reader.close();
        in.close();
    }
    
    public void post(Map values) throws Exception{
        URL url = new URL(host);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");        
        con.setDoOutput(true);
        OutputStream out = con.getOutputStream();
        String query = query(values);
        out.write(query.getBytes());
        out.flush();
        
        byte[] buf = new byte[1024];
        InputStream in = con.getInputStream();
        int n = in.read(buf);
        
        out.close();
        in.close();
        con.disconnect();
       
        
    }
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    CommandBar commandBar = new CommandBar(START,null,POST,GET){

        @Override
        public void doCommand(String command) {
            try{
            switch(command){
                case START:
                    startServer();
                    break;
                case POST:
                    post(valuesPanel.getValues());
                    break;
                case GET:
                    get(valuesPanel.getValues());
                    break;
            }
            } catch(Exception e){
                showMessage(e.getMessage());
            }
        }
        
    };
    MessagePane messagePane = new MessagePane();
    ValuesPanel valuesPanel;

    public HttpClient() {
        setLayout(new BorderLayout());
        add(new JScrollPane(messagePane));
        add(commandBar,BorderLayout.PAGE_START);
        HashMap<String,Object> map = new HashMap<>();
        valuesPanel = new ValuesPanel();
        valuesPanel.addField("param1","Вася");
        valuesPanel.addField("param2","Васильчиков");
        valuesPanel.addField("param3","value3");
        valuesPanel.addField("param4","value4");
        valuesPanel.setValues(map);
        add(valuesPanel,BorderLayout.PAGE_END);
    }
    
    
    void startServer() throws Exception{
        
        Thread t = new Thread(){

            @Override
            public void run() {
                try{
                    HttpServer server = new HttpServer(){

                        @Override
                        public void onStart(HttpServer server) {
                            textOut("server starte\n");
                        }
                                                
                        @Override
                        public String responce(HttpRequest request) {
                            textOut(request.path+"\n");
                            textOut(request.method+"\n");
                            for(String k:request.values.keySet()){
                                textOut(k+" "+request.paramByName(k)+"\n");
                            }
                            return "OK";
                        }

                    };
                    server.start(3345);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
            }
            
        };
        t.start();
        
    }
    
    
    public static void main(String[] args) throws Exception{
        HttpClient testHTTP = new HttpClient();
        JFrame frame = new JFrame("Test HTTP POST/GET");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(testHTTP);
        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
//        testHTTP.startServer();
    }
    
}
