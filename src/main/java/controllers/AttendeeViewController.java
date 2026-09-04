package controllers;

import events.DataUpdateListener;

import exceptions.PersistenceException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import models.*;

import service.KelasService;
import service.Services;

import ui.Ruangkelas;

import java.util.*;

public class AttendeeViewController extends Controller {

    private KelasService kelasService;

    private Kelas selectedKelas;
    private Attendee selectedAttendee;

    @FXML
    private ListView<Grade> assessmentListView;

    @FXML
    private ListView<Attendance> attendanceListView;

    @FXML
    private VBox gradeBox;

    @FXML
    private Label gradeLabel;

    @FXML
    private Label namaLabel;

    @FXML
    private Label nimLabel;

    private final DataUpdateListener kelasListener = () -> {
        if (selectedAttendee == null
                || selectedKelas == null
                || !selectedKelas.getAttendees().contains(selectedAttendee)) {

            Stage stage = (Stage) namaLabel.getScene().getWindow();
            stage.close();
        }

        refresh();
    };

    @Override
    public void setServices(Services services) {
        super.setServices(services);
        kelasService = services.getKelasService();

        kelasService.addChangeListener(kelasListener);
    }

    @Override
    public void cleanup() {
        kelasService.removeChangeListener(kelasListener);
    }

    public void setup(
            Kelas kelas,
            Attendee attendee
    ) {
        selectedKelas = kelas;
        selectedAttendee = attendee;

        refresh();
    }

    @FXML
    private void initialize() {
        assessmentListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(
                    Grade grade,
                    boolean empty
            ) {
            super.updateItem(grade, empty);

            if (empty || grade == null) {
                setText(null);
                return;
            }

            Assessment assessment = grade.getAssessment();

            setText("%s  ⟶  %.2f"
                    .formatted(assessment.toString(), grade.getGrade()));
            }
        });

        attendanceListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Attendance attendance, boolean empty) {
                super.updateItem(attendance, empty);

                if (empty || attendance == null) {
                    setText(null);
                    return;
                }

                Session session = attendance.getSession();
                AttendanceRecord record = attendance.getStatus();

                setText("Session %d: %s  ⟶  %s"
                        .formatted(getIndex() + 1, session.getTopic(), record == null ? "N/A" : record.toString()));

                getStyleClass().removeAll(
                        "attendance-present", "attendance-sick", "attendance-leave"
                );

                if (record == null) return;

                getStyleClass().add(
                    switch (record) {
                        case PRESENT -> "attendance-present";
                        case SICK -> "attendance-sick";
                        case LEAVE -> "attendance-leave";
                    }
                );
            }
        });
    }

    private void refresh() {

        if (selectedAttendee == null) {
            return;
        }

        refreshStudentInfo();
        refreshAssessments();
        refreshAttendance();
        refreshOverallGrade();
    }

    private void refreshStudentInfo() {
        namaLabel.setText(selectedAttendee.getStudent().getNama());

        nimLabel.setText("NIM: " + selectedAttendee.getStudent().getNIM());
    }

    private void refreshAssessments() {

        if (selectedKelas == null) {
            assessmentListView.getItems().clear();
            return;
        }

        List<Grade> grades = new ArrayList<>(selectedKelas.getAttendeeGrades(selectedAttendee).stream().toList());

        for (Assessment assessment : selectedKelas.getAssessments()) {
            if (grades.stream().noneMatch(grade -> grade.ofAssessment(assessment)))
                grades.add(Grade.getEmptyGrade(assessment));
        }

        grades.sort(
            Comparator.comparing(grade -> grade.getAssessment().getCreatedAt())
        );

        assessmentListView
            .setItems(FXCollections.observableArrayList(grades));
    }

    private void refreshAttendance() {

        List<Attendance> attendances = new ArrayList<>(selectedAttendee.getAttendances());

        for (Session session : selectedKelas.getSessions()) {
            if (attendances.stream().noneMatch(att -> att.getSession() == session))
                attendances.add(Attendance.getEmptyAttendance(session));
        }

        attendances.sort(
            Comparator.comparing(a -> a.getSession().getCreatedAt())
        );

        attendanceListView.setItems(
            FXCollections.observableArrayList(attendances)
        );
    }

    private void refreshOverallGrade() {
        double total = selectedKelas.getAttendeeFinalGrade(selectedAttendee);

        gradeLabel.setText("%.2f".formatted(total));

        setGradeStyle(total);
    }

    private void setGradeStyle(Double grade) {

        gradeBox.getStyleClass().removeAll(
                "a-grade",
                "ab-grade",
                "b-grade",
                "bc-grade",
                "c-grade",
                "d-grade",
                "e-grade"
        );

        if (grade == null) {
            return;
        }

        String style = switch ((int) Math.floor(grade)) {
            case int n when n >= 85 -> "a-grade";
            case int n when n >= 80 -> "ab-grade";
            case int n when n >= 75 -> "b-grade";
            case int n when n >= 70 -> "bc-grade";
            case int n when n >= 65 -> "c-grade";
            case int n when n >= 50 -> "d-grade";
            default -> "e-grade";
        };

        gradeBox.getStyleClass().add(style);
    }

    @FXML
    void dropoutBtn(ActionEvent event) {

        if (selectedAttendee == null) {
            return;
        }

        var result = Ruangkelas.showAlert(
                Alert.AlertType.CONFIRMATION,
                "Drop Attendee",
                "Remove %s from this Kelas?".formatted(
                    selectedAttendee
                            .getStudent()
                            .getNama()
                )
        );

        if (result.isEmpty()) {
            return;
        }

        if (result.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            kelasService.dropout(
                selectedKelas,
                List.of(selectedAttendee)
            );

            kelasService.saveData();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (PersistenceException e) {

            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Dropout Failed",
                    e.getMessage()
            );
        }
    }
}