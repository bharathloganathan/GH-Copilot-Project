package org.example;

import org.example.service.StudentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentPhoneTest {
    @Test
    void storesOptionalPhoneAndRejectsInvalidInput() {
        StudentService service = new StudentService();
        var student = service.createStudent("Alice", "alice@example.com", "Math", "+1 123-456-7890");
        assertEquals("+1 123-456-7890", service.getStudent(student.id()).orElseThrow().phone());
        assertEquals("", service.createStudent("Bob", "bob@example.com", "Math").phone());
        assertThrows(IllegalArgumentException.class,
                () -> service.createStudent("Alice", "alice@example.com", "Math", "invalid"));
        assertThrows(IllegalArgumentException.class,
                () -> service.createStudent("Alice", "alice@example.com", "Math", "+12345678901234567890"));
    }
}
