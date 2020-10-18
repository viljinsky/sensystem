/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.tcp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author viljinsky
 */
public class HttpRequest {
    
    public String path;
    public String method;
    String uri;
    String protocol;
    public Map<String,String> headers;
    public Map<String,Object> values = new HashMap<>();
    
    static final Pattern p  = Pattern.compile("(.+):(.*)");
    
    
    String headerValues(String header,String values){
        Pattern p1 = Pattern.compile(values+"(\\S*)");
        Matcher m = p1.matcher(header);
        if (m.find()){
            return m.group(1).trim();
        }
        return null;
    }
            
    void query(String line) throws Exception{
        String[] s = line.split("\\s");
        if (s.length!=3) throw new Exception("bad query");
        method = s[0];
        uri = s[1];
        protocol = s[2];
    }
    
    public void parseHeader(BufferedReader reader) throws Exception{
        headers = new HashMap<>();
        String line;
        while((line = reader.readLine())!=null){
            if(line.isEmpty()) return;
            Matcher m = p.matcher(line);
            if (m.find()){
                headers.put(m.group(1).trim(), m.group(2).trim());
            }
        }
    }
    
    public void parceValue(BufferedReader reader,String boundary) throws Exception{
        String line ;
        Map<String,String> map = new HashMap<>();
        boolean isBody = true;
        while((line=reader.readLine())!=null){
            if(line.isEmpty()){
                if(isBody) isBody = false;
                if (!reader.ready()) break;
                continue;
            }
            if (line.startsWith("--"+boundary)){
                isBody = true;
                if (!reader.ready()) break;
                continue;
            }
            
            if (isBody){
                Matcher m = p.matcher(line);
                if (m.find()){
                    map.put(m.group(1).trim(), m.group(2).trim());
                }
//                System.out.println("header : "+line);
            } else {
                String name = headerValues(map.get("Content-Disposition"), "name=");
                values.put(name.substring(1, name.length()-1), line);
//                System.out.println("name"+name +  " values : "+line);
            }
            if (reader.ready()) continue;
            break;
        }
    }
    
    public void pacePost(BufferedReader reader) throws Exception{
        String line;
        
        String boundary = headerValues(headers.get("Content-Type"), "boundary=");
        if (boundary == null){
//        if (headers.get("Content-Type").equals("application/x-www-form-urlencoded")){
            char[] buf = new char[1024];
            int size = reader.read(buf);
            String s = String.copyValueOf(buf, 0, size);
            String[] p = s.split("&");
            for(String ss: p){
                String[] pp = ss.split("=");
                if (pp.length==2){
                    values.put(URLDecoder.decode(pp[0],"utf-8"),URLDecoder.decode(pp[1],"utf-8"));
                }
            }
           
        } else 
            
            while((line=reader.readLine())!=null){
                if (line.startsWith("--"+boundary)){
                    parceValue(reader,boundary);
                }
                if (reader.ready()) continue;
                break;
            }
    }
    
    public void parceGet(){
        if (uri.contains("?")){
            String[] p  = uri.split("\\?");
            path = p[0];
            if (p.length==2){
                for(String s: p[1].split("&")){
                    String[] ss = s.split("=");
                    try{
                        values.put(URLDecoder.decode(ss[0],"utf-8"), ss.length==2? URLDecoder.decode(ss[1],"utf-8") : null);
                    } catch (Exception e){
                    }
                }
            }
        } else {
            path = uri;
        }
    }
    
    private void parce(BufferedReader reader) throws Exception{
        query(reader.readLine());

        parseHeader(reader);
        parceGet();        
        switch(method){
            case "POST":
                pacePost(reader);
                break;
            case "GET":
//                parceGet(reader);
                return;
            default:
                throw new Exception("bad request method");                        
        }
    }
    
    public HttpRequest(InputStream stream,String charset) throws Exception{
        
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset))){
            parce(reader);
        }
    }
    
    public HttpRequest(BufferedReader reader) throws Exception{
        parce(reader);
    }
        
    @Override
    public String toString(){
        return String.format("%s %s %s ",method,uri,protocol);
    }
    
    public boolean hasParamByName(String name){
        return values.containsKey(name);
    }
    
    public String paramByName(String name){
        if (values.containsKey(name)){
            return (String)values.get(name);
        }
        return null;
    }
        
    public static void main(String[] args) throws Exception{
        
        File file = new File("new 1.txt");
        try(InputStream inputStream = new FileInputStream(file);){
            HttpRequest t = new HttpRequest(inputStream,"utf-8");
            System.out.println(t);
            
            for(String key : t.values.keySet()){
                System.out.println(key+" "+t.paramByName(key));
            }
            
        }
        
    }
    
}
