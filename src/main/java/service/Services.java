package service;

import exceptions.PersistenceException;
import repository.JsonPersistence;

public class Services {

    private final JsonPersistence jsonPersistence = new JsonPersistence();

    private final CourseService courseService = new CourseService(jsonPersistence);
    private final StudentService studentService = new StudentService(jsonPersistence);
    private final KelasService kelasService = new KelasService(jsonPersistence, studentService, courseService);

    private final StudentImportService studentImportService = new StudentImportService(studentService);

    private final KelasExportService kelasExportService = new KelasExportService();
    private final StudentExportService studentExportService = new StudentExportService();
    
    public KelasService getKelasService() {
        return this.kelasService;
    }
    
    public CourseService getCourseService() {
        return this.courseService;
    }
    
    public StudentService getStudentService() {
        return this.studentService;
    }
    
    public StudentImportService getStudentImportService() {
        return this.studentImportService;
    }

    public KelasExportService getKelasExportService() { return this.kelasExportService; }

    public StudentExportService getStudentExportService() { return this.studentExportService; }

    public JsonPersistence getJsonPersistence() { return this.jsonPersistence; }

    public void saveAll() {
        try {
            kelasService.saveData();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }

        try {
            studentService.saveData();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }

        try {
            courseService.saveData();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }
}
