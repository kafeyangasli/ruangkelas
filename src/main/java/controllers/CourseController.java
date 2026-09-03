package controllers;

import java.util.Optional;

import events.DataUpdateListener;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import exceptions.PersistenceException;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import models.Course;

import service.CourseService;
import service.KelasService;
import service.Services;

import ui.CreatePrompt;
import ui.Ruangkelas;

public class CourseController extends Controller {

    private CourseService courseService;
    private KelasService kelasService;
    
    private Course selectedCourse;
    
    public ObservableList<Course> courseList = FXCollections.observableArrayList();
    
    public FilteredList<Course> filteredCourseList;

    private final DataUpdateListener courseListener = this::refreshList;

    @Override
    public void setServices(Services services) {
        super.setServices(services);
        // special for this controller
        this.courseService = services.getCourseService();
        this.kelasService = services.getKelasService();

        courseService.addChangeListener(courseListener);
    }

    @Override
    public void cleanup() {
        courseService.removeChangeListener(courseListener);
    }
    
    public void refreshList() {
        courseList.setAll(courseService.getAll());
        refreshAvailableCourses(courseSearchField.getText());
    }
    
    @FXML
    private ListView<Course> courseListView;

    @FXML
    private TextField courseSearchField;
    
    @FXML
    private Button addCourseBtn;
    
    @FXML 
    private Button delCourseBtn;
    
    @FXML
    private void initialize() {
        courseListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldCourse, newCourse) -> {

                this.selectedCourse = newCourse;
                if (newCourse != null) {
                    if (delCourseBtn.isDisabled()) delCourseBtn.setDisable(false);
                } else {
                    if (!delCourseBtn.isDisabled()) delCourseBtn.setDisable(true);
                }
            });
        
        filteredCourseList = new FilteredList<>(courseList);
        
        courseListView
            .setItems(filteredCourseList);

        courseSearchField.textProperty().addListener(
                this::searchCourses
        );
        
        refreshList();
    }
    
    @FXML
    private void createCourse() {
        
        TextField courseIdField = new TextField();
        courseIdField.setPromptText("Enter course ID");
        
        TextField courseNameField = new TextField();
        courseNameField.setPromptText("Enter course name");

        CreatePrompt window = new CreatePrompt("Create Course");

        window
            .addField("Course ID", courseIdField)
            .addField("Course Name", courseNameField);

        window.show().ifPresent(values -> {

            String courseId =
                (String) values.get("Course ID");

            String courseName =
                (String) values.get("Course Name");

            try {
                courseService.createCourse(
                    courseId,
                    courseName
                );
                refreshList();
            } catch (DuplicateEntityException | PersistenceException e) {
                Ruangkelas.showAlert(
                        Alert.AlertType.ERROR,
                        "Creation Failed",
                        e.getMessage()
                );
            }
        });
    }
    
    @FXML
    private void deleteCourse() {
        // confirm first
        boolean kelasExists = kelasService.kelasWithCourseExist(this.selectedCourse);

        if (kelasExists) {
            Optional<ButtonType> alert = Ruangkelas.showAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Delete Confirmation",
                    "Existing %s classes will be deleted.".formatted(this.selectedCourse.getCourseName())
            );
             
            if (alert.isPresent() && alert.get() != ButtonType.OK) {
                return;
            }
        }
        
        try {
            if (kelasExists) { // a kelas exist
                kelasService.deleteAllByCourse(this.selectedCourse); // delete all kelas first
            }

            courseService.deleteCourse(this.selectedCourse); // then delete course
            refreshList();
        } catch (PersistenceException | EntityNotFoundException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Deletion Failed",
                    e.getMessage()
            );
        }
    }

    private void searchCourses(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        refreshAvailableCourses(newValue);
    }

    private void refreshAvailableCourses(String query) {
        String q = query.trim().toLowerCase();

        filteredCourseList.setPredicate(
                course -> (course.getCourseId().toLowerCase().contains(q)
                        || course.getCourseName().toLowerCase().contains(q))
        );
    }
    
}
