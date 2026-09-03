package controllers;

import events.DataUpdateListener;

import exceptions.PersistenceException;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import models.Attendee;
import models.Kelas;
import models.Student;

import service.KelasService;
import service.Services;
import service.StudentService;
import ui.Ruangkelas;

import java.util.ArrayList;
import java.util.List;

public class AttendeeController extends Controller {

    private KelasService kelasService;
    private StudentService studentService;

    private Kelas selectedKelas;

    public boolean processingEnrollments = false;
    
    private final ObservableList<Attendee> attendeeList = FXCollections.observableArrayList();
    private final ObservableList<Student> studentList = FXCollections.observableArrayList();
    
    private FilteredList<Attendee> filteredAttendeeList;
    private FilteredList<Student> filteredStudentList;

    private final DataUpdateListener listener = this::refreshLists;

    @Override
    public void setServices(Services services) {
        super.setServices(services);
        this.kelasService = services.getKelasService();
        this.studentService = services.getStudentService();

        studentService.addChangeListener(listener);
        kelasService.addChangeListener(listener);
    }

    @Override
    public void cleanup() {
        studentService.removeChangeListener(listener);
        kelasService.removeChangeListener(listener);
    }
    
    public void setKelas(Kelas kelas) {
        selectedKelas = kelas;
        refreshLists();
    }

    @FXML
    private ListView<Attendee> attendeeListView;

    @FXML
    private ListView<Student> studentListView;

    @FXML
    private TextField attendeeSearchField;
    
    @FXML
    private TextField studentSearchField;

    @FXML
    private TextArea nimTextArea;

    @FXML
    private Button addSelectedBtn;

    @FXML
    private Button dropSelectedBtn;

    @FXML
    private Button addNimBtn;

    @FXML
    private void initialize() {

        studentSearchField.textProperty().addListener(
            this::searchStudents
        );
        
        attendeeSearchField.textProperty().addListener(
            this::searchAttendees
        );

        studentListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldStudent, newStudent) -> {
                addSelectedBtn.setDisable(newStudent == null);
            });

        attendeeListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldStudent, newStudent) -> {
                dropSelectedBtn.setDisable(newStudent == null);
            });

        nimTextArea
            .textProperty()
            .addListener(
            (obs, oldText, newText) -> {
                addNimBtn.setDisable(newText == null || newText.isBlank());
            }
        );
        studentListView
            .getSelectionModel()
            .setSelectionMode(SelectionMode.MULTIPLE);

        attendeeListView
            .getSelectionModel()
            .setSelectionMode(SelectionMode.MULTIPLE);

        addSelectedBtn.setDisable(true);
        addNimBtn.setDisable(true);
        dropSelectedBtn.setDisable(true);
        
        studentList
            .setAll(studentService.getAll());
        
        filteredStudentList = new FilteredList<>(studentList);
        filteredAttendeeList = new FilteredList<>(attendeeList);
        
        studentListView
            .setItems(filteredStudentList);

        attendeeListView
            .setItems(filteredAttendeeList);
        
    }

    @FXML
    private void addSelectedAttendees() {
        List<Student> selectedStudents =
            List.copyOf(studentListView
                    .getSelectionModel()
                    .getSelectedItems());

        if (selectedStudents.isEmpty()) {
            return;
        }

        processingEnrollments = true;

        int success;

        try {
            success = kelasService.enroll(
                    selectedKelas,
                    selectedStudents
            );
        } catch (PersistenceException _) {
            success = 0;
        } finally {
            processingEnrollments = false;
        }

        refreshLists();

        showEnrollmentMessage(success, selectedStudents.size() - success);
    }

    @FXML
    private void addNimAttendees() {

        String text = nimTextArea.getText();

        if (text == null || text.isBlank()) {
            return;
        }

        processingEnrollments = true;

        String[] nims =
            text.split("\\R");

        List<Student> selectedStudents = new ArrayList<>();

        for (String rawNim : nims) {

            String nim = rawNim.trim();

            if (nim.isEmpty()) {
                continue;
            }

            Student student = findStudentByNim(nim);

            if (student == null) continue;

            if (selectedKelas.studentEnrolled(student)) continue;

            selectedStudents.add(student);
        }

        int success;

        try {
            success = kelasService.enroll(
                    selectedKelas,
                    selectedStudents
            );
        } catch (PersistenceException _) {
            success = 0;
        } finally {
            processingEnrollments = false;
        }

        nimTextArea.clear();
        refreshLists();

        showEnrollmentMessage(success, selectedStudents.size() - success);
    }

    @FXML
    private void dropSelectedAttendees() {
        List<Attendee> selectedAttendees =
                List.copyOf(attendeeListView
                        .getSelectionModel()
                        .getSelectedItems());

        if (selectedAttendees.isEmpty()) {
            return;
        }

        processingEnrollments = true;

        int success;

        try {
            success = kelasService.dropout(
                    selectedKelas,
                    selectedAttendees
            );
        } catch (PersistenceException _) {
            success = 0;
        } finally {
            processingEnrollments = false;
        }

        String message = "Dropped out: %d student(s)\n".formatted(success) +
                "Skipped: %d student(s)".formatted(selectedAttendees.size() - success);

        Alert.AlertType type =
                success > 0
                        ? Alert.AlertType.INFORMATION
                        : Alert.AlertType.WARNING;

        refreshLists();

        Ruangkelas.showAlert(
                type,
                "Student Dropouts",
                message
        );

    }

    private void showEnrollmentMessage(int success, int fails) {
        String message = "Enrolled: %d student(s)\n".formatted(success) +
                "Skipped: %d student(s)".formatted(fails);

        Alert.AlertType type =
                fails > 0
                        ? Alert.AlertType.WARNING
                        : Alert.AlertType.INFORMATION;

        Ruangkelas.showAlert(
                type,
                "Student Enrollments",
                message
        );
    }
    
    private void refreshLists() {
        if (processingEnrollments) return;

        studentListView.getSelectionModel().clearSelection();
        attendeeListView.getSelectionModel().clearSelection();

        studentList
            .setAll(studentService.getAll());
        
        if (selectedKelas == null) return;
        
        attendeeList
            .setAll(selectedKelas.getAttendees());
        
        if (filteredStudentList != null) {
            refreshAvailableStudents(studentSearchField.getText());
        }
        
        if (filteredAttendeeList != null) {
            refreshAvailableAttendees(attendeeSearchField.getText());
        }
    }
    
    private void refreshAvailableStudents(String query) {
        String q = query.trim().toLowerCase();
        
        filteredStudentList.setPredicate(
            student -> (student.getNIM().toLowerCase().contains(q)
                        || student.getNama().toLowerCase().contains(q))
                        && !selectedKelas.studentEnrolled(student)
        );
    }
    
    private void refreshAvailableAttendees(String query) {
        String q = query.trim().toLowerCase();
        
        filteredAttendeeList.setPredicate(
                attendee -> attendee.getStudent().getNIM().toLowerCase().contains(q)
                        || attendee.getStudent().getNama().toLowerCase().contains(q)
        );
    }
    
    private void searchStudents(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        refreshAvailableStudents(newValue);
    }
    
    private void searchAttendees(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        refreshAvailableAttendees(newValue);
    }

    private Student findStudentByNim(String nim) {
        for (Student student : studentService.getAll()) {
            if (student.getNIM().equals(nim)) {
                return student;
            }
        }

        return null;
    }
}