/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author viljinsky
 */
public class Test4 {
    
    public static void main(String[] args) throws Exception{
        
        URL url = new URL("http://localhost:3345");
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        try(
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(con.getOutputStream()),true);
                InputStream in = con.getInputStream();){
            
                pw.write("hello");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line ;
                while((line= reader.readLine())!=null){
                    if (!reader.ready()) break;
                    System.out.println(line);
                }
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    
}
