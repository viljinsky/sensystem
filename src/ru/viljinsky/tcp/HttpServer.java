/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author viljinsky
 */
public class HttpServer{
    
    int port;
    
    public String responce(HttpRequest request){
        StringBuilder stringBuilder = new StringBuilder();
        
        for(String key:request.values.keySet()){
            stringBuilder.append(key+" "+request.paramByName(key)+"\n");
        }
        
        return stringBuilder.toString();
    }
    
    public void onStart(HttpServer server){
        System.out.println("server started");        
    }
    
    public void onError(Exception e){
    }
    
    public void onStop(HttpServer server){
    }
    
    class ClientHandler extends Thread{
        Socket client;
        
        public ClientHandler(Socket client) {
            System.out.println("connected "+client.toString());
            this.client = client;
        }
        

        @Override
        public void run() {
            try{
//                onStart(this);
                System.out.println("client started");
                try(PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(),"utf-8"),true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(),"utf-8"))
                        ){
                    HttpRequest request = new HttpRequest(reader);
                    String responce = responce(request);
                    
                    writer.println("HTTP/1.1 200 OK");
                    writer.println("Content-Type: text/html; charset=UTF-8");
                    writer.println("Content-Length: "+responce.length());
                    writer.println();
                    writer.println(responce);                    
                    writer.write("");
                    
                } catch (Exception e){
                    onError(e);
                    System.out.println("run exception "+e.getMessage());
                }
            } finally{
                try{
                    client.close();
                } catch (Exception e){
                }
            }
                
        }
        
    }

    public void start(int port) throws Exception{
        this.port = port;
        try(ServerSocket server = new ServerSocket(port);){
            onStart(this);
            while(true){
                new ClientHandler(server.accept()).start();
            }
        } catch (Exception e){
            onError(e);
            throw new Exception("server start error : "+e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception{
        HttpServer server = new HttpServer();
        server.start(3345);
    }
    
}
