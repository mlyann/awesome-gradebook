package org.fp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadStudents {

    public static ArrayList<Student> getStudents(String studentList){
        ArrayList<Student> students = new ArrayList<>();
        String path = "src/main/DataBase/Students/" + studentList;
        String lineRead;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))){
            // Skip csv header
            reader.readLine();

            // Looping through each line in studentlist
            while((lineRead = reader.readLine()) != null){
                String[] values = lineRead.split(",");
                System.out.println(values[0] + values[1] + values[2] + values[3]);
                students.add(new Student(values[0], values[1], values[2], values[3]));
            }
        } catch (IOException e){
            System.out.println("Error: given StudentList file does not exist");
        }

        return students;
    }

}
