/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author viljinsky
 */
public class TcpServer {
    
    
    public String responce(Socket client,String request){
        return request;
    }
    
    public void onStart(ServerSocket server){
    }
    
    public void onStop(ServerSocket server){
    }
    
    class ClientHandler extends Thread{
        BufferedReader in;
        PrintWriter out;
        Socket client;

        public ClientHandler(Socket client) throws Exception {
            this.client = client;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream(),"utf-8"));
        }

        @Override
        public void run() {
            String line ;
            try{
                
                responce(client, "client connected");
                
                
//                int n;
//                while(true){
//                    StringBuilder stringBuilder = new StringBuilder();
//                    byte[] buf = new byte[1024];
//                    while((n = in.read(buf))>=0){
//                        stringBuilder.append(new String(buf,0,n,"utf-8"));
//                    }
//                    line = stringBuilder.toString();
//                    if(".".equals(line)){
//                        out.println("by");
//                        break;
//                    }
//                    out.println(line);
//                    
//                }
                StringBuilder sb = new StringBuilder("**\n");
                while((line = in.readLine())!=null){
                    System.out.println(line);
                    sb.append(line).append("\n");
                    if (".".equals(line)){
                        out.println("by");
                        break;
                    }
                    out.println(responce(client,line));
                    if(in.ready()) continue;
                }
                System.out.println(sb.append("\n**").toString());
            } catch(Exception e){
                responce(client, "ERROR : "+e.getMessage());
            } finally{
                try{
                    in.close();
                    out.close();
                    client.close();
                    responce(client, "clent closed");
                } catch (Exception e){
                }
            }
        }
        
    }
    
    public void start(int port){
        try(ServerSocket server = new ServerSocket(port);){
            onStart(server);
            while(true){
                System.out.println("listen");
                Socket client = server.accept();
                new ClientHandler(client).start();
            }
            
//            onStop(server);
            
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    
    
    public static void main(String[] args) throws Exception{
        TcpServer server = new TcpServer();
        server.start(3345);
    }
    
}
