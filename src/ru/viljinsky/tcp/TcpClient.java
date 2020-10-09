/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author viljinsky
 */
public class TcpClient {
    
    Socket client;
    BufferedReader in;
    PrintWriter out;

    public TcpClient(String host,int port) throws Exception{
        client = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(client.getInputStream(),"utf-8"));
        out = new PrintWriter(client.getOutputStream(),true);
    }
    
    
    public String sendMessage(String message) throws Exception{
        out.println(message);
        String line;
        StringBuilder result = new StringBuilder();
        while((line = in.readLine())!=null){
            result.append(line).append("\n");
            if (!in.ready()) break;
        }
        return result.toString();
    };
    
    public void close() throws Exception{
        in.close();
        out.close();
        client.close();
    }
    
    public static void main(String[] args) throws Exception{
        
        String[] messages = {"command1","command2","command3"};
        
        TcpClient client = new TcpClient("localhost",3345);
        try{
            for(String s: messages){
                String responce  = client.sendMessage(s);        
                System.out.println(responce);
            }
        } finally {
            client.close();
        }
        
        
    }
    
}
