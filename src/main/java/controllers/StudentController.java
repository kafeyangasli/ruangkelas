package controllers;

import java.time.LocalDateTime;
import java.util.Optional;

import events.DataUpdateListener;

import exceptions.DuplicateEntityException;
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

import models.Student;

import service.KelasService;
import service.Services;
import service.StudentService;

import ui.CreatePrompt;
import ui.Ruangkelas;

public class StudentController extends Controller {

    private StudentService studentService;
    private KelasService kelasService;

    private final ObservableList<Student> studentList = FXCollections.observableArrayList();

    private FilteredList<Student> filteredStudentList;

    private final DataUpdateListener studentListener = this::refreshList;
    
    public void setServices(Services services) {
        super.setServices(services);
        // special for this controller
        this.studentService = services.getStudentService();
        this.kelasService = services.getKelasService();

        studentService.addChangeListener(studentListener);
    }

    @Override
    public void cleanup() {
        studentService.removeChangeListener(studentListener);
    }
    
    public void refreshList() {
        studentList.setAll(studentService.getAll());
        refreshAvailableStudents(studentSearchField.getText());
    }
    
    @FXML
    private ListView<Student> studentListView;

    @FXML
    private TextField studentSearchField;
    
    @FXML 
    private Button delStudentBtn;
    
    private Student selectedStudent;
    
    @FXML
    private void initialize() {
        studentListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldStudent, newStudent) -> {

                this.selectedStudent = newStudent;
                if (newStudent != null) {
                    if (delStudentBtn.isDisabled()) delStudentBtn.setDisable(false);
                } else {
                    if (!delStudentBtn.isDisabled()) delStudentBtn.setDisable(true);
                }
            });

        filteredStudentList = new FilteredList<>(studentList);

        studentListView.setItems(filteredStudentList);

        studentSearchField.textProperty().addListener(
                this::searchStudents
        );
        
        refreshList();
    }
    
    @FXML
    private void createStudent() {
        
        TextField nimField = new TextField();
        nimField.setPromptText("Enter student ID (NIM)");
        
        TextField namaField = new TextField();
        namaField.setPromptText("Enter student name");

        TextField cohortField = new TextField();
        cohortField.setPromptText("Enter cohort year");
        cohortField.setText("%d".formatted(LocalDateTime.now().getYear()));
        
        CreatePrompt window = new CreatePrompt("Create Student");

        window  
            .addField("Student ID", nimField)
            .addField("Student Name", namaField)
            .addField("Student Cohort Year", cohortField);

        window.show().ifPresent(values -> {

            String nim =
                (String) values.get("Student ID");

            String name =
                (String) values.get("Student Name");
            
            String cohortYear =
                (String) values.get("Student Cohort Year");

            try {
                studentService.createStudent(
                    nim,
                    name,
                    Integer.parseInt(cohortYear)
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
    private void deleteStudent() {
        // confirm first
        boolean studentExists = kelasService.kelasWithStudentExist(this.selectedStudent);

        if (studentExists) {
            Optional<ButtonType> alert = Ruangkelas.showAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Delete Confirmation",
                    "%s enrollments, including attendance and assessment records, will be deleted.".formatted(this.selectedStudent.getNama())
            );
             
            if (alert.isPresent() && alert.get() != ButtonType.OK) {
                return;
            }
        }
        
        try {
            kelasService.deleteStudentEnrollments(selectedStudent);
            studentService.deleteStudent(selectedStudent);
            
            refreshList();

        } catch (Exception e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Deletion Failed",
                    e.getMessage()
            );
        }
    }

    private void searchStudents(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        refreshAvailableStudents(newValue);
    }

    private void refreshAvailableStudents(String query) {
        String q = query.trim().toLowerCase();

        filteredStudentList.setPredicate(
            student -> (student.getNIM().toLowerCase().contains(q)
                    || student.getNama().toLowerCase().contains(q))
        );
    }
    
}