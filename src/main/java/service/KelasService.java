package service;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import exceptions.PersistenceException;
import models.*;
import repository.Directories;
import repository.JsonPersistence;
import repository.Repository;

import java.util.*;

public class KelasService extends DataService<Kelas> {

    public KelasService(
            JsonPersistence jsonPersistence,
            StudentService studentService,
            CourseService courseService
    ) {
        super(jsonPersistence);
        try {
            repository.setAll(jsonPersistence.loadKelas(
                    studentService.getAll(),
                    courseService.getAll())
            );
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveData() throws PersistenceException {
        jsonPersistence.saveKelas(repository.getAll());
        notifyChange();
    }
    
    public Kelas createKelas(String kelasId, Course course) throws DuplicateEntityException, PersistenceException {
        Kelas kelas = new Kelas(kelasId, course, Semester.semesterNow());
        for (Kelas other : getAll()) {
            if (other.similar(kelas))
                throw new DuplicateEntityException("Duplicate course within the same semester!");
        }
        
        repository.add(kelas);
        saveData();

        return kelas;
    }
    
    public void deleteKelas(Kelas kelas) throws PersistenceException {
        repository.remove(kelas);
        saveData();
    }

    public void addAssessment(Kelas kelas, String name, int percentage) throws PersistenceException {
        kelas.addAssessment(name, percentage);
        saveData();
    }
    
    public List<Kelas> getByCourse(Course course) {
        List<Kelas> toReturn = new ArrayList<>();
        for (Kelas kelas : repository.getAll()) {
            if (kelas.ofCourse(course)) toReturn.add(kelas);
        }

        return toReturn;
    }

    public List<Kelas> getByStudent(Student student) {
        List<Kelas> toReturn = new ArrayList<>();
        for (Kelas kelas : repository.getAll()) {
            if (kelas.studentEnrolled(student)) toReturn.add(kelas);
        }

        return toReturn;
    }

    public boolean kelasWithCourseExist(Course course) {
        for (Kelas kelas : repository.getAll()) {
            if (kelas.ofCourse(course)) return true;
        }
        
        return false; // does not exist
    }
    
    public boolean kelasWithStudentExist(Student student) {
        for (Kelas kelas : repository.getAll()) {
            if (kelas.studentEnrolled(student)) return true;
        }
        
        return false; // does not exist
    }
    
    public void deleteAllByCourse(Course course) throws PersistenceException  {
        List<Kelas> kelasByCourse = getByCourse(course);
        if (!kelasByCourse.isEmpty()) {
            for (Kelas kelas : kelasByCourse) {
                repository.remove(kelas);
            }

            saveData();
        }
    }
    
    public void deleteStudentEnrollments(Student student) throws PersistenceException {
        List<Kelas> kelasByStudent = getByStudent(student);
        if (!kelasByStudent.isEmpty()) {
            for (Kelas kelas : kelasByStudent) {
                dropout(kelas, student);
            }

            saveData();
        }
    }

    public int enroll(Kelas kelas, Collection<Student> students) throws PersistenceException {
        int success = 0;

        for (Student student : students) {
            if (kelas.studentEnrolled(student)) continue;
            Attendee newAttendee = new Attendee(UUID.randomUUID(), student);
            kelas.addAttendee(newAttendee);
            success++;
        }

        saveData();

        return success;
    }

    public int dropout(Kelas kelas, Student student) throws PersistenceException {
        Attendee toDrop = null;

        for (Attendee att : kelas.getAttendees()) {
            if (att.getStudent().equals(student)) {
                toDrop = att; break;
            }
        }
        
        if (toDrop != null) {
            kelas.removeAttendee(toDrop);
            saveData();
            return 1;
        }

        return 0;
    }

    public int dropout(Kelas kelas, Collection<Attendee> attendees) throws PersistenceException {
        int success = 0;

        for (Attendee attendee : attendees) {
            if (!kelas.getAttendees().contains(attendee)) continue;
            kelas.removeAttendee(attendee);
            success++;
        }

        saveData();

        return success;
    }
}
