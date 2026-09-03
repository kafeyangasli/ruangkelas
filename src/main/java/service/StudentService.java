package service;

import exceptions.DuplicateEntityException;
import exceptions.PersistenceException;
import repository.Directories;
import models.Student;
import repository.JsonPersistence;

public class StudentService extends DataService<Student> {

    public StudentService(JsonPersistence jsonPersistence) {
        super(jsonPersistence);
        try {
            repository.setAll(jsonPersistence.loadStudents());
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveData() throws PersistenceException {
        jsonPersistence.saveStudents(repository.getAll());
        notifyChange();
    }

    public void createStudent(String nim, String nama, int cohort) throws DuplicateEntityException, PersistenceException {
        Student student = new Student(nim, nama, cohort);
        if (repository.contains(student))
            throw new DuplicateEntityException("A student with this NIM already exists!");

        repository.add(student);
        saveData();

    }
    
    public void deleteStudent(Student student) throws DuplicateEntityException, PersistenceException {
        repository.remove(student);
        saveData();
    }

}
