# Gradebook App (CSC 335 Final Project)
[![Java][Java]][Java-url] [![IntelliJ IDEA][IntelliJ]][IntelliJ-url]

Welcome to the **Gradebook App**! This console-based Java application allows you to manage a Gradebook, calculating GPAs, add/remove assignments, grade the assignments, and customize different weights for each quizzes. It is a project for **CSC 335 Final Project**, taught by **Instructor Malenie Lotz** with the assistance of **TA Paulina**. The authors of this project are **[Haocheng Cao](https://github.com/Boldthinkingcat)**, **[Minglai Yang](https://ymingl.com)**, **[Jerry Yu]()** and **[Alan Soto]()**. This project will be open-source after the grades released.

![Gradebook App](REV10-ML+Logo.png)

---

## Table of Contents
1. [Overview](#overview)
2. [Features](#features)
3. [Project Structure](#project-structure)
4. [Getting Started](#getting-started)
5. [How to Use](#how-to-use)
6. [Sample Flow](#sample-flow)
7. [Design](#design)
8. [Author & Acknowledgments](#contact)
9. [License](#license)

---

## Overview

The Gradebook System is a command-line (CLI) application that mirrors the essentials of a D2L-style gradebook for both students and teachers:
-	Separate views for students and teachers, each exposing only the actions relevant to that role.
-	Flexible grading logic supporting both total-points and category-weighted schemes, with optional dropped assignments.
-	Rich analytics – class averages, medians, GPA, per-category statistics, and identification of ungraded work.
-	Roster and group utilities to add, remove, sort, or batch-import students.

---

## Features
### 1.	Course Management
   - View current and completed courses (both roles).
   - 	Teachers: add / remove courses, mark courses as completed.

### 2.	Roster Management
-	Teachers:
-	Import students from text / CSV files.
-	Add / remove individual students.
-	Sort roster by first name, last name, username, or assignment grade.
-	Create and manage student groups.

### 3.	Assignment Management
-	Teachers:
-	Add or delete assignments (with points or category tagging).
-	Bulk list “ungraded” assignments for quick grading.
-	Students: view all graded / ungraded assignments per course.

### 4.	Grading & Analytics
-	Two grading modes per course:
-	Total-points: Final Grade(%) = total points earned / total points possible.
  -	Category-weighted: weight-based aggregation with optional lowest-n drops per category.

  **Teachers**:
-   Enter / edit grades.
-	Compute class average and median on any assignment.
-	View per-student current average and overall course statistics.
-	Assign final letter grades (A–E) from computed averages.

**Students**:
-	Compute class average on completed work.
-	Calculate GPA across all completed courses or selected subsets.
### 5.	Student View
-	List enrolled courses with completion status.
-	Drill down to assignment details, grades, and teacher feedback.
-	On-demand calculation of class average and cumulative GPA.
### 6.	Teacher View
-	Dashboard of all taught courses with quick links to roster, assignments, analytics, and grading setup.
-	Dedicated “Analytics” panel summarizing class distribution, category stats, dropped assignments, and ungraded items.
### 7.	Grading Setup
-	Interactive CLI wizard for selecting grading mode, defining categories, setting weights (%), and configuring drop rules.
-	All the other points will be extra credits!

---

## Project Structure
```
org.fp/
├── AdminUI.java           // Admin interface
├── Assignment.java        // Represents an assignment with metadata
├── BaseController.java    // Shared functionality for controllers
├── Course.java            // Represents a course with roster, assignments, grading config
├── DataStore.java         // Handles saving/loading course and user data
├── DecryptVIC.java        // Decrypt logic for secure data
├── EncryptVIC.java        // Encrypt logic for secure data
├── GPT.java               // Handles GPT-powered feedback integration
├── Grade.java             // Enum or class for final letter grades
├── GradeCalculator.java   // Handles grading logic
├── IDGen.java             // Unique ID generation utility
├── LibraryModel.java      // Central model containing all student/teacher/course/assignment data
├── LibraryUsers.java      // Utility for managing users
├── LoginUI.java           // CLI login menu and user routing
├── ProgressBar.java       // Utility for showing progress indicators
├── Score.java             // Represents a student’s score for a specific assignment
├── Student.java           // Student entity with personal and course-related data
├── StudentController.java // Logic layer between student UI and model
├── StudentUI.java         // CLI for students
├── TablePrinter.java      // Formats and prints tables to console
├── Teacher.java           // Teacher entity with managed courses
├── TeacherController.java // Logic layer between teacher UI and model
├── TeacherUI.java         // CLI for teachers
├── VICData.java           // Handles encrypted data configuration
└── VICOperations.java     // Encryption/decryption operations for secure data handling
```

- **TeacherUI, StudentUI, LoginUI – The View / UI**  
  Display menus, accept input, show results.

- **TeacherController / StudentController – The Controllers**  
  Utility class used for Act as intermediaries between UI (e.g., TeacherUI, StudentUI) and LibraryModel. and printing lists of data in a table-like structure to the console.

- **LibraryModel – The Model**  
  Manages the state of the system (students, teachers, courses, assignments, grades, relationships, grading logic).


---

## Getting Started

### Prerequisites
- **Java 8** or higher.
- A Java-compatible IDE (e.g., IntelliJ, Eclipse, VS Code) or the ability to compile via the command line. We use IntelliJ for this project!!

### Installation & Compilation
1. Clone or download this repository:
   ```bash
   git clone https://github.com/mlyann/awesome-gradebook.git
   ```
2.	Open the project in your preferred IDE or navigate into the project folder via terminal:
```bash
cd awesome-gradebook
```
3.	Ensure that the package structure (fp) is respected if you are using an IDE.
4.	Compile the code (if using command line):
```bash
javac fp/*.java
```
5.	Running the Application
After compilation, run the main application class:
```bash
java fp.LoginUI
```

---

## Sample Flow

Below is a brief example of how a typical session might proceed in the console:

1. **Main Menu**  
```aiignore
1) Register  2) Login  3) Exit
👉 Choice: 2
Username：Ming Yang
Password：****************
✅ Login success：TEACHER/STUDENT
```

2. **Teacher Menu**
It will show the teacher menu if you are a teacher. And give a list of your courses.
```aiignore
===================================================
           🎉 Courses taught by 2 2 (sorted by none) 🎉              
===================================================
+-----+-------------+--------------+
| No. | Course Name | Description  |
+-----+-------------+--------------+
| 1   | CS252       | ASM          |
| 2   | CS335       | Obj-Oriented |
+-----+-------------+--------------+
1) 🔍 Select a course
m) 🛠️ Course Management
s) 🔀 Change sort
0) 🚪 Exit
👉 Choice:
```
You can pick either a course or go to the course management menu. If you select a course, it will show the course menu.

```aiignore
===================================================
           🎉 Assignments for Course: CS252 (sorted by none) 🎉              
===================================================
+-----+-----------------+------------+------------+------------------------------------------+-------------+----------------------------+------------+
| No. | Assignment Name | Assigned   | Due        | Progress                                 | Submissions | Graded                     | Published? |
+-----+-----------------+------------+------------+------------------------------------------+-------------+----------------------------+------------+
| 1   | HW 1            | 2025-04-01 | 2025-04-06 | [####----------------] ⏳ 4 days left     | 5/7         | [####################] 5/5 | ❌ No     |
| 2   | HW 2            | 2025-04-01 | 2025-04-06 | [####----------------] ⏳ 4 days left     | 5/7         | [################----] 4/5 | ❌ No     |
| 3   | HW 3            | 2025-04-01 | 2025-04-06 | [####----------------] ⏳ 4 days left     | 7/7         | [##############------] 5/7 | ❌ No     |
| 4   | HW 4            | 2025-04-01 | 2025-04-06 | [####----------------] ⏳ 4 days left     | 6/7         | [################----] 5/6 | ❌ No     |
| 5   | Project 1       | 2025-04-01 | 2025-04-11 | [##------------------] ⏳ 9 days left     | 6/7         | [################----] 5/6 | ❌ No     |
| 6   | Project 2       | 2025-04-01 | 2025-04-11 | [##------------------] ⏳ 9 days left     | 6/7         | [################----] 5/6 | ❌ No     |
| 7   | Quiz 1          | 2025-04-01 | 2025-04-03 | [##########----------] ⏳ 1 day left      | 7/7         | [##############------] 5/7 | ❌ No     |
| 8   | Quiz 2          | 2025-04-01 | 2025-04-03 | [##########----------] ⏳ 1 day left      | 5/7         | [########------------] 2/5 | ❌ No     |
| 9   | Quiz 3          | 2025-04-01 | 2025-04-03 | [##########----------] ⏳ 1 day left      | 4/7         | [###############-----] 3/4 | ❌ No     |
+-----+-----------------+------------+------------+------------------------------------------+-------------+----------------------------+------------+
```

3. **Teacher Operations**
```aiignore
a) 📄 Assignments    r) 👥 Roster    g) 🏁 Final Grades    c) ⚙️ Grading setup    s) 🔍 Search    f) 🧮 Filter    
o) 🔀 Sort    d) 🛠️ Assignments Manage    n) 📊 Analytics    m) ✅ Mark Completed    0) 🔙 Back```
  ```

4. **Student Menu**
```aiignore
===================================================
           🎉 Courses of Aria Griffin (sorted by none) 🎉              
===================================================
+-----+-------------+--------------+-------0------+
+-----+-------------+--------------+--------------+
| 1   | CS335       | MALENIE LOTZ | ✅ Completed |
| 2   | CS252       | ASM          | ✅ Completed |
+-----+-------------+--------------+--------------+
```

5. **Student Operations**
```aiignore
1) 🔍 Select a course    s) 🔀 Change sort    p) 🤖 Personal feedback    g) 📈 GPA    0) 🚪 Exit
👉 Choice: 
```
You can pick either a course or go to the GPA menu. If you select a course, it will show the course menu.
Below is a sample for Student's GPA report. Here Student can also call GPT for feedback.
```aiignore
==================================================
 Course       │ Percent   │ Pts   │ Grade 
--------------------------------------------------
 CS252        │   92.60% │   4   │ A
 CS335        │  164.08% │   4   │ A
--------------------------------------------------
 OVERALL      │  128.34% │ 4.00 │ A
==================================================

===================================================
           🎉 Courses of Aria Griffin (sorted by none) 🎉              
===================================================
+-----+-------------+--------------+-------------+
| No. | Course Name | Description  | Status      |
+-----+-------------+--------------+-------------+
| 1   | CS335       | MALENIE LOTZ | ✅ Completed |
| 2   | CS252       | ASM          | ✅ Completed |
+-----+-------------+--------------+-------------+
```

## Design
1. **Clear separation of concerns between front and backend code**

| Layer | Folder                                 |
|-------|----------------------------------------|
| **Model**       | `LibraryModel`                         |
| **View**        | `StudentUI, TeacherUI`                 |
| **Controller**  | `StudentController, TeacherController` |
##### Model (`LibraryModel`)
- Hold state
- Contain business rules & calculations

##### View (`StudentUI, TeacherUI`)
- Present data already prepared by Controller
- Collect raw user input

##### Controller (`StudentController, TeacherController`)
- Convert UI actions into model calls
- Enforce validation & transactions

2. **Data structures and Java library features**
## 2  Data-structures & Java-library features we rely on

| Feature | Why we chose it                                                                   | Where you can see it |
|---------|-----------------------------------------------------------------------------------|----------------------|
| **`List` / `ArrayList`** | Order-preserving, maps naturally to tables (rosters, assignments).                | `TeacherController.cachedStudents`, `StudentController.cachedAssignments` |
| **`Map<K,V>` / `HashMap`** | O(1) look-ups by ID; perfect for **course → assignments** or **student → scores**. | `LibraryModel.courseMap`, `Course.categoryWeights`, `TeacherController.groupedAssignments` |
| **`Set` / `HashSet`** | Fast membership tests without duplicates                                          | `TeacherController.deletedAssignmentIDs`, `deletedStudentIDs` |
| **Enums** | no illegal strings, also small and FLYWEIGHT                                      | `Assignment.SubmissionStatus`, `Grade`, `TeacherUI.ViewMode` |
| **`Comparator*`** | Multi-key sorts with classes.                                                     | `StudentController.sortCachedAssignmentsByName()` |
| **`switch` expressions** | concise branching on enums.                                                       | `TeacherUI.nextRosterSort()` |
| **Try-with-resources** | Auto-closing I/O, operations.                                                     | `LibraryModel.loadStudentsFromDirectory()` |
| **`Collections.unmodifiableMap`** | Expose **read-only** for DEEPCOPY.                        | `Course.getCategoryWeights()` |
| **`DirectoryStream<Path>` (NIO2)** | Fast, memory-light directory walks with glob filters.                             | `LibraryModel.loadStudentsFromDirectory()` |

---

### Key usage 
```java
// TeacherController
groupedAssignments = model.getAssignmentsInCourse(courseID)
                           .stream()
                           .collect(Collectors.groupingBy(Assignment::getAssignmentName));

private double computeWeightedPercentage(String sid, String cid) {
    Map<String, List<Score>> byCat = new HashMap<>();

    // bucket scores per category
    for (Assignment a : getAssignmentsForStudentInCourse(sid, cid)) {
        var s = getScoreForAssignment(a.getAssignmentID());
        if (s != null) byCat.computeIfAbsent(a.getCategory(), _ -> new ArrayList<>()).add(s);
    }
    return course.getCategoryWeights().entrySet().stream()
                 .mapToDouble(e -> {
                     var list = byCat.getOrDefault(e.getKey(), List.of());
                     list.sort(Comparator.comparingDouble(Score::getPercentage));   // low→high
                     var kept  = list.stream().skip(course.getDropCountForCategory(e.getKey()));
                     int earned = kept.mapToInt(Score::getEarned).sum();
                     int total  = kept.mapToInt(Score::getTotal).sum();
                     return total == 0 ? 0
                                       : e.getValue() * earned / total;
                 }).sum() * 100;
}

```
#### Using Dates to Calculate Progress
```java
static String fullBar(LocalDate start, LocalDate due, LocalDate today) {
    long total = DAYS.between(start, due);
    long done  = DAYS.between(start, today);
    int filled = (int) Math.max(0, Math.min(20, 20 * done / total));
    return "[" + "#".repeat(filled) + "-".repeat(20 - filled) + "] "
           + (today.isAfter(due) ? "⌛ Done" : "");
}
```


3. **Correct and thoughtful use of composition, inheritance, and/or interfaces**


4. **Encapsulation**


5. **Avoidance of antipatterns**


6. **Use of design patterns**


7. **Input validation**



8. **Explain what any AI-generated code**




## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request



## Contact

Haocheng Cao - [@Haocheng](https://x.com/@boldthinkingcat) - cao8@arizona.edu

Minglai Yang - [@Ming](https://x.com/iammilleryang) - mingly@arizona.edu

Jerry Yu - [@Jerry]() - jerryyu1@arizona.edu

Project Link: [https://github.com/mlyann/music-store](https://github.com/mlyann/music-store)

## Acknowledgments
We deeply appreciate the guidance and support of our instructor Malenie Lotz and TA Jenny Yu throughout this journey. 😊 

**Mentors**:
 - Instructor: [Malenie Lotz](https://www.cs.arizona.edu/person/melanie-lotz) (CSC 335)
 - Teaching Assistant: Paulina


## License
This project is licensed under the MIT License. We warmly welcome collaboration on our project! 

<!-- PROJECT LINKS -->
[contributors-shield]: https://img.shields.io/github/contributors/mlyann/music-store.svg?style=for-the-badge
[contributors-url]: https://github.com/mlyann/music-store/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/mlyann/music-store.svg?style=for-the-badge
[forks-url]: https://github.com/mlyann/music-store/network/members
[stars-shield]: https://img.shields.io/github/stars/mlyann/music-store.svg?style=for-the-badge
[stars-url]: https://github.com/mlyann/music-store/stargazers
[issues-shield]: https://img.shields.io/github/issues/mlyann/music-store.svg?style=for-the-badge
[issues-url]: https://github.com/mlyann/music-store/issues
[license-shield]: https://img.shields.io/github/license/mlyann/music-store.svg?style=for-the-badge
[license-url]: https://github.com/mlyann/music-store/blob/main/LICENSE



<!-- BADGE LINKS -->
[Java]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white
[Java-url]: https://www.java.com/
[IntelliJ]: https://img.shields.io/badge/IntelliJ-000000?style=for-the-badge&logo=intellij-idea&logoColor=white
[IntelliJ-url]: https://www.jetbrains.com/idea/
