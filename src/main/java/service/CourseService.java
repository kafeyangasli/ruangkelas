package service;

import java.util.HashSet;
import java.util.Set;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import exceptions.PersistenceException;
import repository.Directories;
import repository.JsonPersistence;
import repository.Repository;

import models.Course;

public class CourseService extends DataService<Course> {

    public CourseService(JsonPersistence jsonPersistence) {
        super(jsonPersistence);
        try {
            repository.setAll(jsonPersistence.loadCourses());
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveData() throws PersistenceException {
        jsonPersistence.saveCourses(repository.getAll());
        notifyChange();
    }

    public void createCourse(String courseId, String courseName) throws DuplicateEntityException, PersistenceException {
        Course course = new Course(courseId, courseName);
        if (repository.contains(course))
            throw new DuplicateEntityException("A course with this ID already exists!");

        repository.add(course);
        saveData();
    }
    
    public void deleteCourse(Course course) throws EntityNotFoundException, PersistenceException {
        repository.remove(course);
        saveData();
    }
}
