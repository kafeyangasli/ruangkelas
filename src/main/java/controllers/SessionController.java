package controllers;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

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

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import models.*;

import service.KelasService;
import service.Services;

import ui.CreatePrompt;
import ui.Ruangkelas;

public class SessionController extends Controller{

    private KelasService kelasService;

    private int index;

    private boolean updatingAttendance = false;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("MMMM, dd yyyy 'at' HH:mm:ss");

    private Kelas selectedKelas;
    private Session selectedSession;
    private Attendee selectedAttendee;
    private Attendance selectedAttendance;

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
            int number,
            Kelas kelas,
            Session session
    ) {
        index = number;
        selectedKelas = kelas;
        selectedSession = session;

        refreshList();
        refreshSessionTitles();

        sessionNoteArea
                .setText(selectedSession.getNotes());
    }

    @FXML
    private ComboBox<AttendanceRecord> attendanceComboBox;

    @FXML
    private ListView<Attendee> attendeeListView;

    @FXML
    private TextField attendeeSearchField;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label endTimeLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Button resetAttendanceBtn;

    @FXML
    private Label selectedAttendeeLabel;

    @FXML
    private TextArea sessionNoteArea;

    @FXML
    private Label sessionTitle;

    @FXML
    private Label sessionTopic;

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
                        if (selectedAttendance != null) return;
                        attendanceComboBox.requestFocus();
                        attendanceComboBox.show();
                    });
                } else {
                    resetAttendance();
                    attendanceComboBox.setValue(null);
                };
            });

        attendeeListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Attendee attendee, boolean empty) {
                super.updateItem(attendee, empty);

                if (empty || attendee == null) {
                    setText(null);
                    updateAttendeeCellStyle(this, null);
                    return;
                }

                setText(attendee.toString());

                updateAttendeeCellStyle(this, attendee);
            }
        });

        attendanceComboBox
            .getItems()
            .setAll(AttendanceRecord.values());

        attendanceComboBox
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldValue, newValue) -> {
                updateAttendanceStyle(newValue);

                if (newValue == null || updatingAttendance) {
                    return;
                }

                if (selectedAttendance == null) {
                    selectedAttendance = selectedAttendee.addAttendance(
                            selectedSession,
                            newValue
                    );
                } else {
                    selectedAttendance.setStatus(newValue);
                }

                updatingAttendance = true;

                refreshAttendeeTimestamps();

                try {
                    kelasService.saveData();
                } catch (PersistenceException e) {
                    Ruangkelas.showAlert(
                            Alert.AlertType.ERROR,
                            "Save Failed",
                            e.getMessage()
                    );
                    attendanceComboBox.setValue(oldValue);
                } finally {
                    updatingAttendance = false;
                }
            });

        attendanceComboBox.setCellFactory(comboBox -> new ListCell<>() {
            @Override
            protected void updateItem(AttendanceRecord record, boolean empty) {
                super.updateItem(record, empty);

                getStyleClass().removeAll(
                        "attendance-present", "attendance-sick", "attendance-leave"
                );

                if (empty || record == null) {
                    setText(null);
                    return;
                }

                setText(record.toString());
                getStyleClass().add(
                    switch (record) {
                        case PRESENT -> "attendance-present";
                        case SICK -> "attendance-sick";
                        case LEAVE -> "attendance-leave";
                    }
                );
            }
        });

        sessionNoteArea
            .focusedProperty()
            .addListener((obs, wasFocused, isFocused) -> {
                if (!wasFocused || isFocused || selectedSession == null || sessionNoteArea.getText() == null) {
                    return;
                }

                String oldNotes = selectedSession.getNotes();

                if (!sessionNoteArea.getText().equals(oldNotes == null ? "" : oldNotes)) {
                    setNotes(sessionNoteArea.getText());
                }
            });

        sessionNoteArea.setOnKeyPressed(event -> {
            if (event.getCode() != KeyCode.ENTER) {
                return;
            }

            if (event.isShiftDown()) {
                sessionNoteArea.replaceSelection("\n");
                event.consume();
                return;
            }

            setNotes(sessionNoteArea.getText());
            event.consume();
        });

        filteredAttendeeList = new FilteredList<>(attendeeList);

        attendeeListView
            .setItems(filteredAttendeeList);

        attendeeSearchField.textProperty().addListener(
                this::searchAttendees
        );
    }

    @FXML
    void editSessionBtn(ActionEvent event) {

        TextField sessionTopicField = new TextField();
        sessionTopicField.setPromptText("Session Topic");
        sessionTopicField.setText(selectedSession.getTopic());

        CreatePrompt window = new CreatePrompt("Edit Session Topic");

        window.addField("Session Topic", sessionTopicField);

        addStage(window.getStage());

        window.show().ifPresent(values -> {

            String sessionTopic =
                    (String) values.get("Session Topic");

            try {
                selectedSession.setTopic(sessionTopic);
                kelasService.saveData();
                refreshSessionTitles();
            } catch (PersistenceException e) {
                Ruangkelas.showAlert(
                        Alert.AlertType.ERROR,
                        "Edit Topic Failed",
                        e.getMessage()
                );
            }
        });
    }

    @FXML
    void deleteSessionBtn(ActionEvent event) {
        if (selectedSession == null) {
            return;
        }

        var result = Ruangkelas.showAlert(
                Alert.AlertType.CONFIRMATION,
                "Drop Session",
                "Remove this session? All attendance records of this session will be cleared."
        );

        if (result.isEmpty()) {
            return;
        }

        if (result.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            selectedKelas.removeSession(selectedSession);

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
    void resetAttendance() {
        if (selectedAttendee == null) {
            selectedAttendance = null;
            return;
        }

        if (selectedAttendance == null) return;

        selectedAttendee.removeAttendance(selectedSession);

        selectedAttendance = null;

        updatingAttendance = true;

        try {
            attendanceComboBox.setValue(null);
        } finally {
            updatingAttendance = false;
        }

        refreshAttendeeTimestamps();
        enableAttendanceButtons(false);

        try {
            kelasService.saveData();
            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Attendance Reset",
                    "Attendance Record was reset"
            );
        } catch (PersistenceException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Reset Failed",
                    e.getMessage()
            );
        }
    }

    private void setAttendee() {
        selectedAttendeeLabel
            .setText(selectedAttendee.getStudent().getNama());

        selectedAttendance = selectedAttendee.getAttendanceBySession(selectedSession);

        updatingAttendance = true;

        attendanceComboBox
                .setValue(
                        selectedAttendance == null
                                ? null
                                : selectedAttendance.getStatus()
                );

        refreshAttendeeTimestamps();

        updatingAttendance = false;

        enableAttendanceButtons(true);
    }

    private void setNotes(String notes) {
        try {
            selectedSession.setNotes(notes);
            kelasService.saveData();
            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Saved Notes",
                    "Notes for this session has been saved."
            );
        } catch (PersistenceException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Notes Edit Failed",
                    e.getMessage()
            );
        }
    }

    private void enableAttendanceButtons(boolean status) {
        attendanceComboBox
                .setDisable(!status);
        resetAttendanceBtn
                .setDisable(selectedAttendance == null);
    }

    private void refreshSessionTitles() {
        if (selectedSession == null) return;

        sessionTitle
            .setText("Session %d".formatted(index));

        sessionTopic
            .setText("Topic: %s".formatted(selectedSession.getTopic()));

        LocalDateTime start = selectedSession.getStartTime();
        LocalDateTime end = selectedSession.getEndTime();

        startTimeLabel.setText(
            start != null ? start.format(formatter) : "N/A"
        );

        endTimeLabel.setText(
            end != null ? end.format(formatter) : "N/A"
        );

        durationLabel.setText(
            start != null && end != null
                ? "%d minutes".formatted(
                    Duration.between(start, end).toMinutes()
                ) : "N/A"
        );
    }

    private void refreshAttendeeTimestamps() {
        recordedTimeLabel.setText(selectedAttendance == null ? "-" :
                formatter.format(selectedAttendance.getRecordedAt()));
        updateTimeLabel.setText(selectedAttendance == null ? "-" :
                formatter.format(selectedAttendance.getUpdatedAt()));
    }

    private void refreshList() {
        if (selectedKelas == null) return;

        attendeeList.setAll(selectedKelas.getAttendees());

        selectedAttendeeLabel.setText(selectedAttendee == null ? "-" : selectedAttendee.getStudent().getNama());

        refreshSessionTitles();
        refreshAttendeeTimestamps();
        refreshAvailableAttendees(attendeeSearchField.getText());
        enableAttendanceButtons(selectedAttendee != null);
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

    private void updateAttendeeCellStyle(
            ListCell<Attendee> cell,
            Attendee attendee
    ) {
        cell.getStyleClass().removeAll(
                "attendance-present",
                "attendance-sick",
                "attendance-leave"
        );

        if (attendee == null || selectedSession == null) {
            return;
        }

        Attendance attendance =
                attendee.getAttendanceBySession(selectedSession);

        if (attendance == null) {
            return;
        }

        cell.getStyleClass().add(
                switch (attendance.getStatus()) {
                    case PRESENT -> "attendance-present";
                    case SICK -> "attendance-sick";
                    case LEAVE -> "attendance-leave";
                }
        );
    }

    private void updateAttendanceStyle(AttendanceRecord record) {
        attendanceComboBox.getStyleClass().removeAll(
                "attendance-present",
                "attendance-sick",
                "attendance-leave"
        );

        if (record == null) {
            return;
        }

        attendanceComboBox.getStyleClass().add(
                switch (record) {
                    case PRESENT -> "attendance-present";
                    case SICK -> "attendance-sick";
                    case LEAVE -> "attendance-leave";
                }
        );
    }

}
