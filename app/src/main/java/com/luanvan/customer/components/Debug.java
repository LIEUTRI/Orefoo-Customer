package com.luanvan.customer.components;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Debug {
    public static void writeLog(String text){
        File logFile = new File(Environment.getExternalStorageDirectory()+"/Android/media/consumer","log.txt");
        logFile.getParentFile().mkdirs();

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
            bufferedWriter.append(text);
            bufferedWriter.newLine();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
