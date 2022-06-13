package com.redes;

import java.io.File;
import java.io.FileOutputStream;

public class Utils {
    public static void createFile(byte[] fileBytes, String path, String fileName) {
        try {
            File dir = new File(path);
            if (!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(path + "/" + fileName);
            FileOutputStream in = new FileOutputStream(file);
            in.write(fileBytes);
            in.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static String formatFileName(String fileName) {
        StringBuilder sb = new StringBuilder();
        String[] fileNameSplit =  fileName.split(" ");
        int i = 0;
        for (String st : fileNameSplit) {
            if (i > 0) sb.append("_");
            sb.append(st);
            i++;
        }
        return sb.toString();
    }
}
