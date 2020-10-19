/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static ru.viljinsky.server.MyServer.SERVER_DATA;
import static ru.viljinsky.tcp.HttpResponce.NOT_FOUND;
import static ru.viljinsky.tcp.HttpResponce.RESULT_OK;

/**
 *
 * @author viljinsky
 */
public class TestClient5 {
    
    public static void main(String[] args) throws IOException{
    
        int responceCode;
        String responceText;
        
            File file = new File(SERVER_DATA);
            if (file.exists()){
                StringBuilder stringBuilder = new StringBuilder();
                try(
                    InputStream in = new FileInputStream(file);){
                    byte[] buf = new byte[200];
                    int n;
                    while((n = in.read(buf))>=0){
                        String s = new String(buf,0,n,"utf-8");
                        System.out.println(s);
                        stringBuilder.append(s);
                    }
                    responceCode = RESULT_OK;
                    responceText = stringBuilder.toString();
                    System.out.println("file size :"+ responceText.length());
                    
                    System.out.println(responceText.substring(responceText.length()-100));
                    System.out.println(responceText.substring(500,600));
                }
            } else {
                responceCode = NOT_FOUND;
                responceText = "<p>файл данных не найден</p>";
            }
        
    
    }
    
}
