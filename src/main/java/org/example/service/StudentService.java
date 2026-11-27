package org.example.service;

import org.example.model.Student;
import org.example.repository.StudentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StudentService {
    private final StudentRepository repository = new StudentRepository();

    public Student createStudent(String name, String email, String department) {
        return createStudent(name, email, department, "");
    }

    public Student createStudent(String name, String email, String department, String phone) {
        validateStudentInput(name, email, department, phone);
        String id = UUID.randomUUID().toString().substring(0, 8);
        Student student = new Student(id, name.trim(), email.trim(), department.trim(), phone == null ? "" : phone.trim(), "active");
        return repository.save(student);
    }

    public List<Student> getStudents() {
        return repository.findAll();
    }

    public Optional<Student> getStudent(String id) {
        return repository.findById(id);
    }

    public boolean deleteStudent(String id) {
        return repository.deleteById(id);
    }

    public void validateStudentInput(String name, String email, String department) {
        validateStudentInput(name, email, department, "");
    }

    public void validateStudentInput(String name, String email, String department, String phone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'.");
        }
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (phone == null || (!phone.isBlank() && !phone.trim().matches("(?=.{7,20}$)\\+?[0-9 -]+"))) {
            throw new IllegalArgumentException("Invalid phone number.");
        }
    }
}