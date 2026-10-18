package org.example;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "/";
        File file = new File(filePath);
        Workbook workbook = new XSSFWorkbook();

        FileOutputStream fout = new FileOutputStream(file);
        workbook.write(fout);
        fout.close();
        System.out.println("File created");

    }
}