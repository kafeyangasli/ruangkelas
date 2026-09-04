package controllers;

import events.DataUpdateListener;

import exceptions.PersistenceException;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.stage.Stage;
import models.Assessment;
import models.Attendee;
import models.Grade;
import models.Kelas;

import service.Services;
import service.KelasService;
import ui.CreatePrompt;
import ui.Ruangkelas;

import javax.swing.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

public class AssessmentController extends Controller {

    private KelasService kelasService;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MMMM, dd yyyy 'at' HH:mm:ss");

    private Kelas selectedKelas;
    private Assessment selectedAssessment;
    private Attendee selectedAttendee;
    private Grade selectedGrade;

    private final ObservableList<Attendee> attendeeList = FXCollections.observableArrayList();

    private FilteredList<Attendee> filteredAttendeeList;

    private final DataUpdateListener kelasListener = this::refreshList;

    @Override
    public void setServices(Services services) {
        super.setServices(services);
        // for this controller
        kelasService = services.getKelasService();
        kelasService.addChangeListener(kelasListener);
    }

    @Override
    public void cleanup() {
        kelasService.removeChangeListener(kelasListener);
    }

    public void setup(
            Kelas kelas,
            Assessment assessment
    ) {
        selectedKelas = kelas;
        selectedAssessment = assessment;

        refreshList();
        refreshAssessmentDescriptions();
    }

    @FXML
    private Label assessmentTitle;

    @FXML
    private Label assessmentWorth;

    @FXML
    private ListView<Attendee> attendeeListView;

    @FXML
    private TextField attendeeSearchField;

    @FXML
    private TextField gradeField;

    @FXML
    private Label maxGradeLabel;

    @FXML
    private Label meanGradesLabel;

    @FXML
    private Label minGradeLabel;

    @FXML
    private Button resetGradeBtn;

    @FXML
    private Button saveGradeBtn;

    @FXML
    private Label selectedAttendeeLabel;

    @FXML
    private Label updateTimeLabel;

    @FXML
    private Label recordedTimeLabel;

    @FXML
    private void initialize() {
        attendeeListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldAttd, newAttd) -> {
                selectedAttendee = newAttd;
                if (newAttd != null) {
                    setAttendee();
                    Platform.runLater(() -> {
                        if (selectedGrade != null) return;
                        gradeField.requestFocus();
                    });
                } else {
                    enableEditButtons(false);
                    selectedAttendeeLabel.setText("-");
                };
            });

        filteredAttendeeList = new FilteredList<>(attendeeList);

        attendeeListView
            .setItems(filteredAttendeeList);

        attendeeSearchField.textProperty().addListener(
            this::searchAttendees
        );
    }

    @FXML
    void editAssessmentBtn(ActionEvent event) {

        TextField assessmentName = new TextField();
        assessmentName.setPromptText("Assessment description");
        assessmentName.setText(selectedAssessment.getName());

        TextField assessmentWorth = new TextField();
        assessmentWorth.setPromptText("Assessment percentage worth");
        assessmentWorth.setText(String.valueOf(selectedAssessment.getPercentage()));

        CreatePrompt window = new CreatePrompt("Edit Assessment");

        window
                .addField("Description", assessmentName)
                .addField("Percentage", assessmentWorth);

        addStage(window.getStage());

        window.show().ifPresent(values -> {

            String asmName =
                    (String) values.get("Description");

            try {
                int asmWorth = Integer.parseInt(
                        (String) values.get("Percentage")
                );

                if (asmWorth <= 0 || asmWorth > 100) {
                    throw new IllegalArgumentException(
                        "Percentage must be between 1 and 100."
                    );
                }

                selectedAssessment.setName(asmName);
                selectedAssessment.setPercentage(asmWorth);
                kelasService.saveData();
                refreshAssessmentDescriptions();
            } catch (NumberFormatException e) {
                Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                        "Edit Assessment Failed",
                    "Percentage is not a number!"
                );
            } catch (PersistenceException | IllegalArgumentException e) {
                Ruangkelas.showAlert(
                        Alert.AlertType.ERROR,
                        "Edit Assessment Failed",
                        e.getMessage()
                );
            }
        });

    }

    @FXML
    void deleteAssessmentBtn(ActionEvent event) {
        if (selectedAssessment == null) {
            return;
        }

        var result = Ruangkelas.showAlert(
                Alert.AlertType.CONFIRMATION,
                "Drop Assessment",
                "Remove Assessment %s? All grades of this assessment will be cleared.".formatted(selectedAssessment.getName())
        );

        if (result.isEmpty()) {
            return;
        }

        if (result.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            selectedKelas.removeAssessment(selectedAssessment);

            kelasService.saveData();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();

        } catch (PersistenceException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Deletion Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    void saveGrade(ActionEvent event) {
        try {
            double number = Double.parseDouble(gradeField.getText());

            if (Double.isNaN(number)) {
                throw new IllegalArgumentException("Grade is not a number!");
            }

            if (number < 0 || number > 100) {
                throw new NumberFormatException("Grade cannot be negative or more than 100!");
            }

            if (selectedGrade == null) {
                selectedGrade = new Grade(selectedAssessment, selectedAttendee, number);
                selectedKelas.addGrade(selectedGrade);
            }

            selectedGrade.setGrade(number);
            kelasService.saveData();

            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Success",
                    "Grade is successfully set"
            );

            enableEditButtons(true);
            refreshAttendeeTimestamps();

        } catch (IllegalArgumentException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Invalid Grade",
                    e.getMessage()
            );

        } catch (PersistenceException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Persistence Error",
                    e.getMessage()
            );
        }
    }

    @FXML
    void resetGrade(ActionEvent event) {
        try {
            selectedKelas.resetGradeByAssessment(selectedAssessment, selectedAttendee);
            kelasService.saveData();

            selectedGrade = null;

            Ruangkelas.showAlert(
                Alert.AlertType.INFORMATION,
                "Success",
                "Grade is successfully reset"
            );

            enableEditButtons(true);
        } catch (PersistenceException e) {
            Ruangkelas.showAlert(
                Alert.AlertType.ERROR,
                "Reset Failed",
                e.getMessage()
            );
        }

    }

    private void refreshAssessmentDescriptions() {
        if (selectedAssessment == null) return;

        assessmentTitle
                .setText("Assessment %s".formatted(selectedAssessment.getName()));

        assessmentWorth
                .setText("worth %d%%".formatted(selectedAssessment.getPercentage()));
    }

    private void refreshList() {
        if (selectedAssessment == null) return; // in case IDK

        attendeeList.setAll(selectedKelas.getAttendees());
        selectedAttendeeLabel.setText(selectedAttendee == null ? "-" : selectedAttendee.getStudent().getNama());
        refreshAttendeeTimestamps();
        refreshAvailableAttendees(attendeeSearchField.getText());

        Map<String, Object> statistics = selectedKelas.getGradeStatistics(selectedAssessment);
        Optional<Grade> gradeMax = Optional.ofNullable((Grade) statistics.get("max")),
                        gradeMin = Optional.ofNullable((Grade) statistics.get("min"));
        double gradeMean = (double) statistics.get("mean");


        maxGradeLabel
                .setText("Max : %.3f (owned by: %s)".formatted(
                        gradeMax.map(Grade::getGrade).orElse(0.0),
                        gradeMax.isPresent() ? gradeMax.get().getAttendee().getStudent().getNIM() : "-"
                ));
        minGradeLabel
                .setText("Min : %.3f (owned by: %s)".formatted(
                        gradeMin.map(Grade::getGrade).orElse(0.0),
                        gradeMin.isPresent() ? gradeMin.get().getAttendee().getStudent().getNIM() : "-"
                ));
        meanGradesLabel
                .setText("Mean : %.3f".formatted(gradeMean));
    }

    private void setAttendee() {
        selectedAttendeeLabel
                .setText(selectedAttendee.getStudent().getNama());

        selectedGrade = selectedKelas.getGradeByAssessment(selectedAssessment, selectedAttendee);
        gradeField.setText(selectedGrade == null ? "" : String.valueOf(selectedGrade.getGrade()));

        enableEditButtons(true);
        refreshAttendeeTimestamps();
    }

    private void enableEditButtons(boolean selected) {
        saveGradeBtn.setDisable(!selected);
        resetGradeBtn.setDisable(!selected || selectedGrade == null);
        gradeField.setDisable(!selected);
        if (!selected) gradeField.setText("");
    }

    private void searchAttendees(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        refreshAvailableAttendees(newValue);
    }

    private void refreshAvailableAttendees(String query) {
        String q = query.trim().toLowerCase();

        filteredAttendeeList.setPredicate(
                attendee -> attendee.getStudent().getNIM().toLowerCase().contains(q)
                        || attendee.getStudent().getNama().toLowerCase().contains(q)
        );
    }

    private void refreshAttendeeTimestamps() {
        recordedTimeLabel.setText(selectedGrade == null ? "-" :
                formatter.format(selectedGrade.getRecordedAt()));
        updateTimeLabel.setText(selectedGrade == null ? "-" :
                formatter.format(selectedGrade.getUpdatedAt()));
    }

}