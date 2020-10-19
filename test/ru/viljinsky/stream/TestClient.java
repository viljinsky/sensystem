/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import ru.viljinsky.tcp.HttpResponce;
import java.util.HashMap;
import java.util.Map;
import ru.viljinsky.tcp.HttpClient;


/**
 *
 * @author viljinsky
 */
public class TestClient {
    
    public static void main(String[] args) throws Exception{
        String host = "http://localhost:3345/page1";
        Map<String,Object> map = new HashMap<>();
        map.put("login","admin");
        map.put("password","sensystem");
        
        
        HttpClient client = new HttpClient(host);
        HttpResponce responce = client.get(map);
        switch(responce.getCode()){
            case HttpResponce.RESULT_OK:
                System.out.println(responce.getText().length());
                break;
            case HttpResponce.NOT_FOUND:
                break;
            case HttpResponce.BAD_REQUEST:
                break;
            default:
                System.err.println(responce.getCode()+" "+ responce.getText());
        }
        
    }
    
}
