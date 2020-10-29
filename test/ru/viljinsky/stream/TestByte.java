/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.stream;

/**
 *
 * @author viljinsky
 */
public class TestByte {
    public static void main(String[] args){
        byte[] data = new byte[]{0x21,0x22,0x23,0x0D,0x41,0x42,0x43,0x00};
        System.out.println(new String(data));
    }
    
}
