package org.fp;

//Student s = new Student("20251234", "Ming", "Yang", "mingyang@example.com");
//System.out.println(s);  // output: Ming Yang (20251234): mingyang@example.com

import java.util.Comparator;

public class Student {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String stuID;

    public Student(String firstName, String lastName, String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.stuID = IDGen.getUniqueID();

        if(stuID == null){
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getStuID(){
        return stuID;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", getFullName(), getStuID(), email);
    }

    public static Comparator<Student> firstNameAscendingComparator(){
        return new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return s1.firstName.compareTo(s2.firstName);
            }
        };
    }

    public static Comparator<Student> firstNameDescendingComparator() {
        return firstNameAscendingComparator().reversed();
    }

    public static Comparator<Student> lastNameAscendingComparator(){
        return new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return s1.lastName.compareTo(s2.lastName);
            }
        };
    }

    public static Comparator<Student> lastNameDescendingComparator() {
        return lastNameAscendingComparator().reversed();
    }

    public static Comparator<Student> userNameAscendingComparator(){
        return new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return s1.email.compareTo(s2.email);
            }
        };
    }

    public static Comparator<Student> userNameDescendingComparator() {
        return userNameAscendingComparator().reversed();
    }
}