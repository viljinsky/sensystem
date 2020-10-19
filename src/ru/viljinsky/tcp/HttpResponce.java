/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author viljinsky
 */
public class HttpResponce {
    
    public static final int RESULT_OK = 200;
    public static final int BAD_REQUEST = 400;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_ERROR = 500;
    
    static final Map<Integer,String> map =new HashMap<>();
    static{
        map.put(RESULT_OK,"HTTP/1.1 200 OK");
        map.put(NOT_FOUND,"HTTP/1.0 404 Not Found");
    }
    
    protected int responceCode = RESULT_OK;
    
    protected String responceText = "";
    
    protected String contentType = "text/html";
    
    public String header(){
        StringBuilder stringBuilder = new StringBuilder();
        if (map.containsKey(responceCode))
            stringBuilder.append(map.get(responceCode)).append("\n");
        else 
            stringBuilder.append("HTTP/1.1 200 OK").append("\n");
            
        stringBuilder.append("Content-Type: "+contentType+"; charset=UTF-8").append("\n");
        stringBuilder.append("Content-Length: "+responceText.length()).append("\n");        
        return stringBuilder.toString();
    }

    public HttpResponce() {
    }

    public HttpResponce(int responceCode) {
        this.responceCode = responceCode;
    }

    public HttpResponce(int responceCode, String responceText) {
        this.responceCode = responceCode;
        this.responceText = responceText;
    }
    
    public int length(){
        return responceText.length();
    }
    
    public String getText(){
        return responceText;
    }
    
    public int getCode(){
        return responceCode;
    }
    
}
