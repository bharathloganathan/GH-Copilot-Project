package org.example;

import org.example.service.StudentService;

public class StudentServiceTest {
    public static void main(String[] args) {
        StudentService service = new StudentService();
        var created = service.createStudent("Alice", "alice@example.com", "Computer Science");
        assert "Alice".equals(created.name());
        assert service.getStudents().size() >= 1;
        assert service.getStudent(created.id()).isPresent();
        System.out.println("StudentServiceTest passed");
    }
}
