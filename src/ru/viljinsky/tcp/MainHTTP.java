/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import ru.viljinsky.server.CommandBar;
import ru.viljinsky.server.MessagePane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.json.JSONException;
import ru.viljinsky.server.DB_JSON_decoder;
import ru.viljinsky.server.IDB;

/**
 * HttpServer
 * 
 * @author viljinsky
 */
public class MainHTTP extends JPanel{
    
    static final String START = "start" ;
    static final String STOP = "stop";
    static final String CLEAR = "clear";
        
    
    class SensystemServer extends HttpServer{
        
        @Override
        public void onStart(HttpServer server) {
            textOut("server "+server.port + " - started\n");
        }

        @Override
        public void onError(Exception e) {
            textOut(e.getMessage());
        }


        @Override
        public HttpResponce responce(HttpRequest request) {
            StringBuilder stringBuilder = new StringBuilder();
            try{
                stringBuilder.append(request.toString()+"\n");
                stringBuilder.append("headers:\n");
                for(String s:request.headers.keySet()){
                    stringBuilder.append("\t"+s+" "+request.headers.get(s)+"\n");
                }
                stringBuilder.append("path   : "+request.path+"\n");
                if (!request.values.isEmpty()){
                stringBuilder.append("params :\n");
                    for(String s:request.values.keySet()){
                        stringBuilder.append("\t"+s+" = \""+request.paramByName(s)+"\"\n");
                    }
                    stringBuilder.append("\n");
                }
                textOut(stringBuilder.toString());
                
                if (request.hasParamByName("data")){
                    try{
                        IDB db = new DB_JSON_decoder(request.paramByName("data"));
                        return new HttpResponce(HttpResponce.RESULT_OK, db.toString());
                    } catch(Exception e){
                        return new HttpResponce(HttpResponce.INTERNAL_ERROR,e.getMessage());
                    }
                }

                return new HttpResponce(HttpResponce.RESULT_OK,"<p>Запрос выполнен успешно</p>");
            } catch (JSONException e){
                stringBuilder.append("Ошибка формирование ответа : "+e.getMessage());
                return new HttpResponce(HttpResponce.INTERNAL_ERROR,"<p>Ошибка зазбора запроса</p><p>"+e.getMessage()+"</p>");
            }
           // return new HttpResponce(HttpResponce.RESULT_OK,"result OK");

        }
                
    }
        
    static final String UNSUPPORTED_OPERATION = "unsupported yet";
    
    void start_server() throws Exception{

        new Thread(){

            @Override
            public void run() {
                try{
                    HttpServer httpServer = new SensystemServer();
                    httpServer.start(3345);
                } catch (Exception e){
                    showMessage(e.getMessage());
                }
            }
            
        }.start();
        
    }
    
    void stop_server() throws Exception{
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }
    
    MessagePane messagePane = new MessagePane();
    CommandBar commandBar = new CommandBar(START,STOP,null,CLEAR){

        @Override
        public void doCommand(String command) {
            MainHTTP.this.doCommand(command);
        }
        
    };
    
    void textOut(String text){
        messagePane.textOut(text);
    }
    
    void showMessage(String message){
        JOptionPane.showMessageDialog(getParent(), message);
    }
    void doCommand(String command){
        try{
            switch(command){
                case CLEAR:
                    messagePane.clear();
                    break;
                case START:
                    start_server();
                    break;
                case STOP: 
                    stop_server();
                    break;
            }
        } catch (Exception e){
            showMessage(e.getMessage());
        }
    }

    public MainHTTP() {
        setPreferredSize(new Dimension(800,600));
        setLayout(new BorderLayout());
        add(commandBar,BorderLayout.PAGE_START);
        add(new JScrollPane(messagePane));
        
    }
    public JFrame start() throws Exception{
        JFrame frame = new JFrame("HttpServer");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setContentPane(this);
        frame.pack();
        frame.setLocationRelativeTo(null);        
        frame.setVisible(true);
        start_server();
        return frame;
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame("HttpServer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new MainHTTP());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
                
    }
    
}
