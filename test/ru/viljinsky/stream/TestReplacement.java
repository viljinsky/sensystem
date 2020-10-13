/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import java.awt.BorderLayout;
import java.sql.Connection;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ru.viljinsky.project2019.DataModel;
import ru.viljinsky.project2019.Grid;
import ru.viljinsky.project2019.IDataModel;
import ru.viljinsky.project2019.Proc;
import ru.viljinsky.project2019.Recordset;
import ru.viljinsky.project2019.replacement.Document.Changes;
import ru.viljinsky.project2019.replacement.ReplacementTab2;



/**
 *
 * @author viljinsky
 */
public class TestReplacement extends JPanel implements IDataModel{
    
    Grid grid = new Grid();
    public TestReplacement() {
        setLayout(new BorderLayout());
        add(new JScrollPane(grid));
    }
    
    
    public void open(Connection con) throws Exception{
        Recordset r = new Changes(con);
        grid.setRecordset(r);
    }
    
    
    public static void main(String[] args) throws Exception{
        DataModel.setConnection("моё расписание.db");
        ReplacementTab2 panel = new ReplacementTab2();
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        frame.setSize(1400, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Proc.query(con->{
            panel.open(con);
        });
    }
}
