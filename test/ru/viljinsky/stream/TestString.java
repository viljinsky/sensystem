/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

import javafx.beans.binding.StringBinding;

/**
 *
 * @author viljinsky
 */
public class TestString {
    public static void main(String[] args){
        StringBuilder str = new StringBuilder();
        str.append("физика");
        
        String s = str.toString();
        
        System.out.println(s);
    }
}
