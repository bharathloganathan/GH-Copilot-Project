package org.example.repository;

import org.example.model.Student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StudentRepository {
    private final Map<String, Student> students = new LinkedHashMap<>();

    public Student save(Student student) {
        students.put(student.id(), student);
        return student;
    }

    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    public Optional<Student> findById(String id) {
        return Optional.ofNullable(students.get(id));
    }

    public boolean deleteById(String id) {
        return students.remove(id) != null;
    }
}
