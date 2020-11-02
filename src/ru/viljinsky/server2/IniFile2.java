/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.viljinsky.server2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

/**
 *
 * @author viljinsky
 */
class IniFile2 extends HashMap<String, String> {
    String fileName = "inifile.ini";

    public IniFile2() {
    }

    public void read() {
        File file = new File(fileName);
        if (file.exists()) {
            read(file);
        }
    }

    public void read(File file) {
        try (final FileInputStream in = new FileInputStream(file);final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(":");
                if (values.length == 2) {
                    put(values[0].trim(), values[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void save() {
        File file = new File(fileName);
        save(file);
    }

    public void save(File file) {
        try (final FileOutputStream out = new FileOutputStream(file);final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "utf-8"))) {
            for (Object key : keySet()) {
                writer.write(key + " : " + get(key) + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void setValues(String key, ru.viljinsky.project2019.Values values) {
        StringBuilder sb = new StringBuilder();
        for (String s : values.keySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(s).append("=").append(values.get(s));
        }
        put(key, sb.toString());
    }

    public ru.viljinsky.project2019.Values getValues(String key) {
        ru.viljinsky.project2019.Values values = new ru.viljinsky.project2019.Values();
        if (containsKey(key)) {
            String s = get(key);
            String[] p = s.split(";");
            for (String ps : p) {
                if (ps.contains("=")) {
                    String[] pps = ps.split("=");
                    if (pps.length == 2) {
                        values.put(pps[0].trim(), pps[1].trim());
                    }
                }
            }
        }
        return values;
    }
    
}
