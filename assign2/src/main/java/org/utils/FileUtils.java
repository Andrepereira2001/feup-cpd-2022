package org.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {

    public static Boolean existFile(File file){
        return file.exists();
    }

    public static File getFileAbsolute(String path){
        return new File(path);
    }

    public static File getFile(String path){
        return new File(Paths.get("").toAbsolutePath() + "/src/" + path);
    }

    public static void deleteFile(File file){
        file.delete();
    }

    public static File createFile(String relativePath){
        File file = new File( Paths.get("").toAbsolutePath() + "/src/" + relativePath);

        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
            }
        } catch (IOException e) {
            System.err.println("Error while creating file " + e);
            e.printStackTrace();
        }

        return file;
    }

    public static void writeToFile(File file, String message, boolean append){
        try {
            FileWriter writer = new FileWriter(file, append);
            writer.write(message);
            writer.close();
        }
        catch (IOException e) {
            System.err.println("Error while writing to file " + e);
        }
    }

    public static List<String> readFromFile(File file){
        List<String> data = new ArrayList<>();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                data.add(myReader.nextLine());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("Error while reading from file " + e);
        }

        return data;
    }

    public static void createFolder(String path){
        File file = new File( Paths.get("").toAbsolutePath() + "/src/" + path);

        try {
            if (file.mkdirs()) {
                System.out.println("File created: " + file.getName());
            }
        } catch (Exception e) {
            System.err.println("Error while creating folder " + e);
            e.printStackTrace();
        }
    }

    public static List<File> getAllFiles(String path){
        File folder = FileUtils.getFile(path);
        List<File> filesName = new ArrayList<>();
        if(folder.exists() && folder.isDirectory()) {
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isFile()) {
                    filesName.add(fileEntry);
                }
            }
        }
        return filesName;
    }
}
