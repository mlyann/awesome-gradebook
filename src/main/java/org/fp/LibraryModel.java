package org.fp;

import java.lang.reflect.Array;
import java.util.*;

public class LibraryModel {
    private final HashMap<String, Student> stuID_Map;
    private final HashMap<String, Course> courseID_Map;
    private final HashMap<String, Assignment> assignmentID_Map;
    //creates a groupId, stores arrayList of students
   


    // This map record the courses enrolled by each student, student is a Key:
    // { "StudentID" : [CourseID1, CourseID2, ...] , ... }
    private final HashMap<String, ArrayList<Course>> stuID_couID_Map;
    // This map record the students enrolled in each course, course is a Key:
    // { "CourseID" : [StudentID1, StudentID2, ...] , ... }
    private final HashMap<String, ArrayList<String>> couID_stuID_Map;
    // This map record the assignments in each course, course is a Key:
    // { "CourseID" : [AssignmentID1, AssignmentID2, ...] , ... }
    private final HashMap<String, ArrayList<String>> couID_assID_Map;
    // This map record the student who submitted each assignment, assignment is a Key:
    // { "StudentID" : [AssignmentID1, AssignmentID2, ...] , ... }
    private final HashMap<String, ArrayList<String>> assID_Map_stuID_Map;
    // This map record the assignments submitted by each student, student is a Key:
    // { "AssignmentID1" : [StudentID1, StudentID2, ...] , ... }
    private final HashMap<String,HashMap<String,Score>> stuID_assID_Score_MAP;
    private final HashMap<String, ArrayList<String>> groupID_studentIDs;


    public LibraryModel() {
        this.stuID_Map = new HashMap<>();
        this.stuID_couID_Map = new HashMap<>();
        this.couID_stuID_Map = new HashMap<>();
        this.couID_assID_Map = new HashMap<>();
        this.assID_Map_stuID_Map = new HashMap<>();
        this.courseID_Map = new HashMap<>();
        this.assignmentID_Map = new HashMap<>();
        this.stuID_assID_Score_MAP=new HashMap<>();
        this.groupID_studentIDs=new HashMap<>();

    }

    // Design for function

    // Getting all courses for given student
    public ArrayList<ArrayList<String>> getCoursesString(String stuID) {
        ArrayList<Course> courses = stuID_couID_Map.get(stuID);
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (Course course : courses) {
            ArrayList<String> courseInfo = new ArrayList<>();
            courseInfo.add(course.getCourseName());
            courseInfo.add(course.getCourseDescription());
            result.add(courseInfo);
        }
        return result;
    }

    // Getting student name given id
    public String getStuName(String stuID) {
        StringBuilder result = new StringBuilder();
        Student student = stuID_Map.get(stuID);
        if (student != null) {
            result.append(student.getFirstName()).append(" ").append(student.getLastName());
        } else {
            result.append("Student not found");
        }
        return result.toString();
    }

    // Adding a student to a course
    public void addStudentToCourse(String stuID, String couID){
        couID_stuID_Map.get(couID).add(stuID);
        stuID_couID_Map.get(stuID).add(courseID_Map.get(couID));
    }

    // Remove a student from a course
    public void removeStudentFromCourse(String stuID, String couID){
        couID_stuID_Map.get(couID).remove(stuID);
        stuID_couID_Map.get(stuID).remove(courseID_Map.get(couID));
    }

    // Add assignments to a course
    public void addAssignmentToCourse(String assID, String couID){
        couID_assID_Map.get(couID).add(assID);
    }

    // Remove assignments to a course
    public void removeAssignmentFromCourse(String assID, String couID){
        couID_assID_Map.get(couID).remove(assID);
    }

    // Import list of students to add to a course
    public void importStudentAddToCourse(String studentList, String couID){
        ArrayList<Student> gottenStudents = ReadStudents.getStudents(studentList);
        ArrayList<String> stuIDs = couID_stuID_Map.get(couID);

        // Updating couID_StuID map and stuID_couID map
        for(Student s : gottenStudents){
            stuIDs.add(s.getStuID());
            stuID_couID_Map.get(s.getStuID()).add(courseID_Map.get(couID));

            // Update stuID Map because we have added new students
            stuID_Map.put(s.getStuID(), s);
        }
    }

    // View students enrolled in a course
    public ArrayList<String> viewStudentsInCourse(String couID){
        ArrayList<String> studentNames = new ArrayList<>();
        ArrayList<String> studentIds = couID_stuID_Map.get(couID);

        for (String s : studentIds){
            studentNames.add(stuID_Map.get(s).getFullName());
        }

        return studentNames;
    }

    public void SubmitGradeAssignment(String stuID, String assigID, int earned, int total) {
        HashMap<String, Score> assScoreMap = stuID_assID_Score_MAP.get(stuID);
        // If this student doesn't have any scores yet, create a new map
        if (assScoreMap == null) {
            assScoreMap = new HashMap<>();
            stuID_assID_Score_MAP.put(stuID, assScoreMap);
        }
        Score score = Score.of(earned, total);
        assScoreMap.put(assigID, score);


        // if the assigment is not already in there add it and update it
        if(!assID_Map_stuID_Map.containsKey(assigID)){
            assID_Map_stuID_Map.put(assigID,new ArrayList<>());
        }
         if (!assID_Map_stuID_Map.get(assigID).contains(stuID)){
            assID_Map_stuID_Map.get(assigID).add(stuID);
         }
    }
   


 

    // Calculate class Average for assignment
    public double getAveragePercetangeAssig(String assigID){
        int total=0;
        int maxPoints=0;
        ArrayList<String> studentsIDS=assID_Map_stuID_Map.get(assigID);
        for(String s: studentsIDS){
            HashMap<String,Score> current= stuID_assID_Score_MAP.get(s);
            Score score= current.get(assigID);
            total+=score.getEarned();
            maxPoints+=score.getTotal();
        }
        return ((double) total / maxPoints) * 100;
    }


    // Put students in groups
    public void createGroup(String groupName, String... studentIDs) {
        ArrayList<String> stuIds = groupID_studentIDs.get(groupName);
        if (stuIds == null) {
            stuIds = new ArrayList<>();
            groupID_studentIDs.put(groupName, stuIds);
        }

        for (String id : studentIDs) {
            stuIds.add(id);
        }
    }

    // Helper method for sorting
    private ArrayList<Student> sortStudents(Comparator<Student> comparator) {
        ArrayList<Student> studentList = new ArrayList<>(stuID_Map.values());
        Collections.sort(studentList, comparator);
        return studentList;
    }

    // Sort student by first name ascending
    public ArrayList<Student> sortByFirstAscending(){
        return sortStudents(Student.firstNameAscendingComparator());
    }

    // Sort student by first name descending
    public ArrayList<Student> sortByFirstDescending(){
        return sortStudents(Student.firstNameDescendingComparator());
    }

    // Sort student by last name ascending
    public ArrayList<Student> sortByLastAscending(){
        return sortStudents(Student.lastNameAscendingComparator());
    }

    // Sort student by last name descending
    public ArrayList<Student> sortByLastDescending(){
        return sortStudents(Student.lastNameDescendingComparator());
    }

    // Sort student by username ascending
    public ArrayList<Student> sortByUserNameAscending(){
        return sortStudents(Student.userNameAscendingComparator());
    }

    // Sort student by username ascending
    public ArrayList<Student> sortByUserNameDescending(){
        return sortStudents(Student.userNameDescendingComparator());
    }

    public ArrayList<Student> sortByGradeAssignment(String assigID) {
        ArrayList<String> studentIDs = assID_Map_stuID_Map.get(assigID);
        ArrayList<Student> sortedStudents = new ArrayList<>();
        //this is not cheking it the students is not graded
        for (String id : studentIDs) {
            sortedStudents.add(stuID_Map.get(id));
        }
        Collections.sort(sortedStudents, new Comparator<Student>() {
            public int compare(Student s1, Student s2) {
                Score score1 = stuID_assID_Score_MAP.get(s1.getStuID()).get(assigID);
                Score score2 = stuID_assID_Score_MAP.get(s2.getStuID()).get(assigID);
                return score2.getLetterGrade().compareTo(score1.getLetterGrade());
            }
        });
   
        return sortedStudents;
    }


    

    // Assign final grades to students based on course averages

    // View ungraded assignments
    public ArrayList<Assignment> getUngradedAssignments(){
        ArrayList<Assignment> ungraded = new ArrayList<>();
        ArrayList<Assignment> allAssignments = new ArrayList<>(assignmentID_Map.values());
        for (Assignment a : allAssignments){
            if (!a.returnScore().isGraded()){
                ungraded.add(new Assignment(a));
            }
        }
        return ungraded;
    }

    // Choose a mode for calculating class averages

    // Set up categories of assignments with weights, allowing for dropped assignments

    // Calculate class median for assignment
    public double calculateMedianAssignment(String assiID) {
        ArrayList<Integer> gradesEarned = new ArrayList<>();
        ArrayList<String> students = assID_Map_stuID_Map.get(assiID);
   
        if (students == null || students.isEmpty()) {
             System.out.println("error assignment has no students or assignment not added");
            return -1;
        }
   
        for (String studentID : students) {
            HashMap<String, Score> assigScore = stuID_assID_Score_MAP.get(studentID);
            Score score = assigScore.get(assiID);
            if (score != null && score.isGraded()) {
                gradesEarned.add(score.getEarned());
            }
        }
        Collections.sort(gradesEarned);
        int size=gradesEarned.size();
        return gradesEarned.get(size/2);
    }
   


    // Calculate student's average

    // Gets course title
    public String getCourseTitle(String couID){
        return courseID_Map.get(couID).getCourseDescription();
    }

    public void state() {
        // Create students
        Student s1 = new Student("Ming", "Yang", "mingyang@example.com");
        Student s2 = new Student("Li", "Wei", "liwei@example.com");
        Student s3 = new Student("Zhang", "Hua", "zhanghua@example.com");

        stuID_Map.put(s1.getStuID(), s1);
        stuID_Map.put(s2.getStuID(), s2);
        stuID_Map.put(s3.getStuID(), s3);

        // Create courses
        Course c1 = new Course("Intro to Programming", "Basic programming in Java.");
        Course c2 = new Course("Data Structures", "Learn about lists, stacks, queues.");
        Course c3 = new Course("Database Systems", "Learn relational DB and SQL.");

        courseID_Map.put(c1.getCourseID(), c1);
        courseID_Map.put(c2.getCourseID(), c2);
        courseID_Map.put(c3.getCourseID(), c3);

        // Assign courses to students
        stuID_couID_Map.put(s1.getStuID(), new ArrayList<>(List.of(c1, c2)));
        stuID_couID_Map.put(s2.getStuID(), new ArrayList<>(List.of(c2, c3)));
        stuID_couID_Map.put(s3.getStuID(), new ArrayList<>(List.of(c1, c3)));

        // Assign students to courses
        couID_stuID_Map.put(c1.getCourseID(), new ArrayList<>(List.of(s1.getStuID(), s3.getStuID())));
        couID_stuID_Map.put(c2.getCourseID(), new ArrayList<>(List.of(s1.getStuID(), s2.getStuID())));
        couID_stuID_Map.put(c3.getCourseID(), new ArrayList<>(List.of(s2.getStuID(), s3.getStuID())));
    }
}
