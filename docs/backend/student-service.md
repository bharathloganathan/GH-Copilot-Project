# Student Backend Documentation

<!-- docsync:begin backend-service sources=src/main/java/org/example/service/StudentService.java -->
## Public methods

Source: `src/main/java/org/example/service/StudentService.java`

```java
public Student createStudent(String name, String email, String department)
public Student createStudent(String name, String email, String department, String phone)
public List<Student> getStudents()
public Optional<Student> getStudent(String id)
public boolean deleteStudent(String id)
public void validateStudentInput(String name, String email, String department)
public void validateStudentInput(String name, String email, String department, String phone)
```

Source SHA-256: `0bfbcf50dc212c5845ff237d917ffcf6de1f8ce070be4b714c938f81f42dbcfc`

Logic changes alter this digest; review source changes for behavioral meaning.
<!-- docsync:end backend-service -->

## Manual notes

The service currently keeps students in an in-memory repository.
