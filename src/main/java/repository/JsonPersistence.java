package repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import exceptions.PersistenceException;
import models.Course;
import models.Kelas;
import models.Student;
import repository.json.CourseData;
import repository.json.KelasData;
import repository.json.StudentData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonPersistence {
    private final ObjectMapper mapper;

    public JsonPersistence() {
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public Set<Student> loadStudents() throws PersistenceException {
        return readSet(Directories.studentData, new TypeReference<List<StudentData>>() {})
                .stream().map(StudentData::toDomain).collect(Collectors.toCollection(HashSet::new));
    }

    public void saveStudents(Set<Student> students) throws PersistenceException {
        write(Directories.studentData, students.stream().map(StudentData::new).toList());
    }

    public void saveStudents(Set<Student> students, Path path) throws PersistenceException {
        write(path, students.stream().map(StudentData::new).toList());
    }

    public Set<Course> loadCourses() throws PersistenceException {
        return readSet(Directories.courseData, new TypeReference<List<CourseData>>() {})
                .stream().map(CourseData::toDomain).collect(Collectors.toCollection(HashSet::new));
    }

    public void saveCourses(Set<Course> courses) throws PersistenceException {
        write(Directories.courseData, courses.stream().map(CourseData::new).toList());
    }

    public Set<Kelas> loadKelas(Set<Student> students, Set<Course> courses) throws PersistenceException {
        List<KelasData> data = readSet(Directories.kelasData, new TypeReference<List<KelasData>>() {});

        Map<String, Student> studentsByNim = students.stream()
                .collect(Collectors.toMap(Student::getNIM, s -> s));
        Map<String, Course> coursesById = courses.stream()
                .collect(Collectors.toMap(Course::getCourseId, c -> c));

        try {
            Set<Kelas> result = new HashSet<>();
            for (KelasData item : data) {
                result.add(item.toDomain(coursesById, studentsByNim));
            }
            return result;
        } catch (RuntimeException e) {
            throw new PersistenceException(
                    "LOAD_FAIL: " + Directories.kelasData, e);
        }
    }

    public void saveKelas(Set<Kelas> kelas) throws PersistenceException {
        write(Directories.kelasData, kelas.stream().map(KelasData::new).toList());
    }

    public void saveKelas(Kelas kelas, Path path) throws PersistenceException {
        write(path, kelas);
    }

    private <T> List<T> readSet(Path path, TypeReference<List<T>> type) throws PersistenceException {
        if (!Files.exists(path)) return List.of();
        try {
            return mapper.readValue(path.toFile(), type);
        } catch (IOException e) {
            throw new PersistenceException("LOAD_FAIL: " + path, e);
        }
    }

    private void write(Path path, Object value) throws PersistenceException {
        try {
            Path parent = path.getParent();
            if (parent != null) Files.createDirectories(parent);
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), value);
        } catch (IOException e) {
            throw new PersistenceException("SAVE_FAIL: " + path, e);
        }
    }
}
