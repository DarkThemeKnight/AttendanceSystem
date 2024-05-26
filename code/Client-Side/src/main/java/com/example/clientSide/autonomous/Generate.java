package com.example.clientSide.autonomous;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Generate {
    public Generate() throws IOException {
        File copyTo = new File("FillHerUp");
        File from= new File("/home/omotola/Documents/code/lfw_funneled");
        List<File> files = Arrays.stream(Objects.requireNonNull(from.listFiles()))
                .filter(File::isDirectory)
                .filter(file -> Objects.requireNonNull(file.listFiles()).length > 1)
                .toList();
        files = files.subList(0,30);
        File mainDir = new File(copyTo,"students");
        mainDir.mkdirs();
        File attendanceDir = new File(copyTo,"attendance");
        attendanceDir.mkdirs();
        for (File f: files){
            File file =new File(mainDir,f.getName());
            file.mkdirs();
            List<File> fx = new java.util.ArrayList<>(Arrays.stream(Objects.requireNonNull(f.listFiles())).toList());
            File v = fx.remove(0);
            Files.copy(v,new File(attendanceDir.getAbsolutePath()+"/"+v.getName()));
            for (File x:fx){
                Files.copy(x,new File(file.getAbsoluteFile()+"/"+x.getName()));
            }
        }


    }

    public static void main(String[] args) throws IOException {
        new Generate();
    }
}
