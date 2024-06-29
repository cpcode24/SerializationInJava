/*****************
 * @author: Constant Pagoui
 * @date: 06-28-2024
 * Just playing with JAVA.
 */

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.*;

public class Serialization {
    //public static String generatedID;
    private static AtomicInteger studentsCount = new AtomicInteger(0);

    public static void main(String... args){
        List<Student> students = new ArrayList<>();
        try(var data = Files.lines(Path.of("students.txt"))){
            data.peek(System.out::println).forEach(
                line->{
                    String[] elements = line.split(",");
                    String[] dobElts = elements[1].trim().split("-");
                    String[] startingDateElts = elements[3].trim().split("-");
                    students.add(
                        new Student(
                            elements[0].trim(),
                            getGeneratedID(), 
                            elements[2].trim(),
                            getDate(dobElts),
                            getDate(startingDateElts),
                            Integer.valueOf(elements[4].trim())
                        )
                    );
                }
            );
        }catch(IOException | IndexOutOfBoundsException | NullPointerException | NumberFormatException e){
            System.err.println("An expected exception occured: ");
            e.printStackTrace();
        }catch(Exception e){
            System.out.println("Uh-uh! this is not good! \n"+e);
        }finally{
            if(students == null || students.size() == 0)
                System.out.println("No student has been registered.");
            else{
                System.out.println("The following students data has been obtained:");
                students.forEach(System.out::println);

                System.out.println("Registering students...");
                if(registerStudents(students))
                    System.out.println("Registration successful!");
                else
                    System.out.println("Registration failed!");
            }

        }

    }

    public static boolean registerStudents(List<Student> students){
        try{
            return Student.registerStudents(students);
        }catch(Exception e){
            System.err.println("An error occured while registering students");
            e.printStackTrace();
            return false;
        }
    }

    public static LocalDate getDate(String[] date){
        return LocalDate.of(Integer.parseInt(date[2]), 
                                Integer.parseInt(date[0].charAt(0) == '0'? 
                                            ""+date[0].charAt(1):date[0]),
                                Integer.parseInt(date[1]));
    }
    public static String getGeneratedID(){
        NumberFormat formatter = new DecimalFormat("000000000");
        return formatter.format(LocalDate.now().getYear()*100000 + LocalDate.now().getMonthValue()*1000
                            + studentsCount.incrementAndGet()).toString();
    }
}

class Student implements Serializable{
    private String fullname;
    private String ID;
    private String level;
    private LocalDate DOB;
    private LocalDate startingDate;
    private int numberOfSubjects;
    private static final File RECORDS = new File("records.dat");
    private transient Map<String, Grade> grades;
    private static final long serialVersionUID = 1L;
    private static final ObjectStreamField[] serialPersistentFields = 
                                {new ObjectStreamField("Full name", String.class),
                                new ObjectStreamField("ID", String.class),
                                new ObjectStreamField("Level", String.class),
                                new ObjectStreamField("Subjects", Integer.class),
                                new ObjectStreamField("DOB", LocalDate.class),
                                new ObjectStreamField("Starting Date", LocalDate.class)};

    public Student(){
        startingDate = LocalDate.now();
        DOB = LocalDate.of(1900, 1, 1);
    }
    public Student(String fullName, String id, String lev, LocalDate dateOfBirth, LocalDate starting, int subjects){
        this.fullname =fullName;
        this.ID = id;
        this.level = lev;
        this.DOB = dateOfBirth;
        this.startingDate = starting;
        this.numberOfSubjects = subjects;
    }

    public int getNumberOfSubjects(){return this.numberOfSubjects;}

    public String getFullName(){ return this.fullname;}

    public String getLevel(){return this.level;}

    public LocalDate getStartingDate(){return this.startingDate;}

    public static boolean registerStudents(List<Student> students) throws IOException, IllegalArgumentException{
        if(students == null || students.size() == 0)
            throw new IllegalArgumentException("List of students to be registered is empty!");

        try(var out = new ObjectOutputStream(
            new BufferedOutputStream(
                new FileOutputStream(RECORDS)
            ))){
            for(Student s: students)
                out.writeObject(s);
            out.flush();
            return true;
        }
        catch(Exception e){
            System.err.println("Error occured while writing/registering students!");
            e.printStackTrace();
            return false;
        }

    }

    private void writeObject(ObjectOutputStream oos) throws Exception{
        ObjectOutputStream.PutField fields = oos.putFields();
        fields.put("Full name", fullname);
        fields.put("Level", level);
        fields.put("ID", ID);
        fields.put("Subjects", Integer.valueOf(numberOfSubjects));
        fields.put("Starting Date", startingDate);
        fields.put("DOB", DOB);
        oos.writeFields();
    }

    private void readObject(ObjectInputStream ois) throws Exception{
        ObjectInputStream.GetField fields = ois.readFields();
        this.fullname = (String) fields.get("Full name", null);
        this.level = (String) fields.get("Level", null);
        this.ID = (String) fields.get("ID", null);
        this.numberOfSubjects = (int)fields.get("Subjects", 0);
        this.DOB = (LocalDate) fields.get("DOB", null);
    }

    public synchronized Student readResolve() throws ObjectStreamException{
        // To be implemented

        return null;
    }

    public synchronized Student writeReplace() throws ObjectStreamException{
        // To be implemented

        return null;
    }

    public String toString(){
        return "Student: "+this.fullname
            +"\n\tID: "+this.ID
            +"\n\tLevel: "+this.level
            +"\n\tStarting date: "+this.startingDate
            +"\n\tNumber of courses: "+this.numberOfSubjects;
    }

}

enum Grade{
    APLUS(95), AMINUS(90), BPLUS(85), BMINUS(80), CPLUS(75), CMINUS(70), DPLUS(65), DMINUS(60), F(59);
    private final int grade;
    private Grade(int grade){
        this.grade = grade;
    } 
    public String toString(){
        return this.name()+": "+String.valueOf(grade);
    }
}