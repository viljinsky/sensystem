/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.stream.Stream;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.data.DB;

/**
 *
 * @author viljinsky
 */
public class TestStream {
    
    class RS extends OutputStream{

        StringBuilder stringBuilder = new StringBuilder();
        

        @Override
        public void write(int b) throws IOException {
            stringBuilder.append(String.valueOf(b));
        }
        
        public void write(String s) throws Exception{
            char[] buf = s.toCharArray();
            for(int i=0;i<buf.length;i++){
                write(buf[i]);
            }
        }

    }

    public TestStream() {
        
    }
    
    public void execute(Connection con) throws Exception{
        DB db = new DB(con);
        RS rs = new RS();
        Recordset recordset = db.depart();
        for(Object[] p: recordset){
            rs.write(p.toString());
        }
        System.out.println(rs.stringBuilder.toString());
        
        
    }
    
    public static void main(String[] args) throws Exception{
        DataModel.setConnection("моё расписание 2.db");
        new TestStream().execute(DataModel.getConnection());
    }
    
}
