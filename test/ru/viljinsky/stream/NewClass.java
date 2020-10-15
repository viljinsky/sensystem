/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;

/**
 *
 * @author viljinsky
 */
public class NewClass implements IDataModel{
    
    public static void main(String[] args) throws Exception{
        DataModel.setConnection("моё расписание.db");
        Proc.query(con->{
        Recordset teacher = DataModel.query(con, "select teacher_id,last_name,first_name,patronymic from teacher").addColumn2(new String[]{TEACHER_NAME}).calc(new Recordset.CalcField() {

            @Override
            public Object[] onCalc(Object[] p) {
                String firstName = (String)p[1];
                String lastName = (String)p[2];
                String patronymic = (String)p[3];
                if (!lastName.isEmpty()) lastName = lastName.substring(0,1);
                if (!patronymic.isEmpty()) patronymic = patronymic.substring(0,1);
                p[p.length-1] = String.format("%s %s. %s.", firstName,lastName,patronymic);
                return p;
            }
        });
        teacher.print();
        });
    }
    
}
