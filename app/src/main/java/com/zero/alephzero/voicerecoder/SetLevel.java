package com.zero.alephzero.voicerecoder;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SetLevel {

    private static String filename = "level";
    private static File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "sesler");

    public static void createFile(Context context){

        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }

        File file0 = new File(context.getFilesDir(),"level");
        if (file0.exists()){
            // do nothing, file already here
        }else{

            File file = new File(context.getFilesDir(), "level");
            writeFile(0,context);

        }
    }

    public static String getFilename() {

        return folder.getAbsolutePath() + "/";
    }
    public static int fileNumber(){

        File[] list = folder.listFiles();
        return list.length;
    }

    public static String readFile(Context context){

        FileInputStream fileInputStream;
        String fileContent = "";

        File directory = context.getFilesDir();
        File file = new File(directory, filename);

        try {

            fileInputStream = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();

            while( fileInputStream.available() > 0) {

                sb.append((char)fileInputStream.read());
            }

            if(sb != null){
                // This is your fileContent in String.
                fileContent= sb.toString();

            }
            try {
                fileInputStream.close();
            }
            catch(Exception e){
                // TODO Auto-generated catch block
               e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
             e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;

    }
    public static void writeFile(int level,Context context) {


        String fileContents =  String.valueOf(level);
        FileOutputStream outputStream;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
