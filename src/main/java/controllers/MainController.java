package controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import events.DataUpdateListener;

import exceptions.ExportException;
import exceptions.PersistenceException;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import models.*;

import service.*;

import ui.*;

public class MainController extends Controller {

    private CourseService courseService;
    private KelasService kelasService;
    private StudentService studentService;
    private StudentImportService studentImportService;
    private KelasExportService kelasExportService;
    private StudentExportService studentExportService;
    
    private Kelas selectedKelas;
    private Attendee selectedAttendee;
    private Assessment selectedAssessment;
    private Session selectedSession;

    private boolean moveBetweenKelas = false;
    
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Attendee> attendeeList = FXCollections.observableArrayList();
    private final ObservableList<Assessment> assessmentList = FXCollections.observableArrayList();
    private final ObservableList<Session> sessionList = FXCollections.observableArrayList();

    private FilteredList<Attendee> filteredAttendeeList;

    private final DataUpdateListener kelasListener = this::refreshLists;

    @Override
    public void setServices(Services services) {
        super.setServices(services);
        // special for this controller
        this.courseService = services.getCourseService();
        this.kelasService = services.getKelasService();
        this.studentService = services.getStudentService();
        this.studentImportService = services.getStudentImportService();
        this.kelasExportService = services.getKelasExportService();
        this.studentExportService = services.getStudentExportService();

        kelasService.addChangeListener(kelasListener);
    }

    @Override
    public void cleanup() {
        kelasService.removeChangeListener(kelasListener);
    }
       
    @FXML
    private MenuItem deleteKelasMenuBtn;
    
    @FXML
    private MenuItem exportKelasMenuBtn;

    @FXML
    private MenuItem exportReportBtn;
    
    @FXML
    private TextField attendeeSearchField;

    @FXML
    private Button manageAssessmentBtn;
    
    @FXML
    private Button viewAttendeeBtn;

    @FXML
    private Button manageSessionBtn;

    @FXML
    private Button startSessionBtn;
    
    @FXML
    private Button endSessionBtn;
    
    @FXML
    private BorderPane limboPane;
    
    @FXML
    private AnchorPane kelasDetailsPane;
    
    @FXML
    private ListView<Kelas> kelasListView;
    
    @FXML
    private ListView<Attendee> attendeeListView;
    
    @FXML
    private ListView<Assessment> assessmentListView;

    @FXML
    private ListView<Session> sessionListView;

    @FXML
    private Label courseName;

    @FXML
    private Label kelasSemester;
    
    @FXML
    private void initialize() {
    
        enableKelasPane(false); // incase i forgor in scenebuilde
        
        kelasListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldKelas, newKelas) -> {
                if (moveBetweenKelas) return;

                if (newKelas == null) {
                    enableKelasPane(false);
                    return;
                }

                boolean res = selectKelas(newKelas);

                if (!res) {
                    moveBetweenKelas = true;

                    kelasListView
                        .getSelectionModel()
                        .select(oldKelas);

                    moveBetweenKelas = false;

                    return;
                }

                selectedKelas = newKelas;
            });
        
        attendeeListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldAttd, newAttd) -> {
                selectedAttendee = newAttd;

                enableViewAttendeeBtn(newAttd != null);
            });
        
        assessmentListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldAss, newAss) -> {
                selectedAssessment = newAss;

                enableManageAssessmentBtn(newAss != null);
            });

        sessionListView
            .getSelectionModel()
            .selectedItemProperty()
            .addListener((obs, oldSession, newSession) -> {
                selectedSession = newSession;

                updateSessionButtons();
            });

        sessionListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Session session, boolean empty) {
                super.updateItem(session, empty);

                if (empty || session == null) {
                    setText(null);
                } else {
                    int idx = getIndex() + 1;

                    setText(
                            "Session %d: %s"
                                    .formatted(idx, session.getTopic())
                    );
                }
            }
        });

        filteredAttendeeList = new FilteredList<>(attendeeList);
        
        kelasListView
            .setItems(kelasList);
        attendeeListView
            .setItems(filteredAttendeeList);
        assessmentListView
            .setItems(assessmentList);
        sessionListView
            .setItems(sessionList);
        
        refreshLists();
        
        attendeeSearchField.textProperty().addListener(
            this::searchAttendees
        );
        
    }
    
    @FXML
    private void saveKelasBtn() {
        try {
            kelasService.saveData();
            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Data saved",
                    "Kelas has successfully been saved."
            );
        } catch (PersistenceException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Save Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void closeAppBtn(ActionEvent event) {
        try {
            Ruangkelas.setClosedFromButton(true);
            services.saveAll();
            Platform.exit();
        } catch (RuntimeException e) {
            Ruangkelas.showAlert(
                Alert.AlertType.ERROR,
                "Save Failed",
                e.getMessage()
            );
            event.consume();
        }
    }
    
    @FXML
    private void manageCourseBtn() throws IOException {
        FXMLLoader loader = Ruangkelas.getLoader("courses");
        
        Parent root = loader.load();

        CourseController controller = loader.getController();

        Scene scene = new Scene(root);
        
        Stage stage = Ruangkelas.createStage(scene, "Manage Courses", 400, 400);

        stage.setOnCloseRequest(event -> controller.cleanup());
        
        stage.show();
    }
    
    @FXML
    private void manageStudentBtn() throws IOException {
        FXMLLoader loader = Ruangkelas.getLoader("students");
        
        Parent root = loader.load();

        StudentController controller = loader.getController();

        Scene scene = new Scene(root);
        
        Stage stage = Ruangkelas.createStage(scene, "Manage Students", 400, 400);

        stage.setOnCloseRequest(event -> controller.cleanup());
        
        stage.show();
    }

    @FXML
    private void importStudents(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();

        String text = clickedItem.getText();

        String description;
        String extension;

        switch (text) {
            case "From CSV" -> {
                description = "CSV Files";
                extension = "*.csv";
            }

            case "From XLSX" -> {
                description = "Excel Workbook";
                extension = "*.xlsx";
            }

            case "From JSON" -> {
                description = "JSON Files";
                extension = "*.json";
            }

            default -> {
                return;
            }
        }

        File file = FileChooserUtil.chooseOpenFile(
            clickedItem.getParentMenu()
                    .getParentPopup(),
            "Import Student Data",
            description,
            extension
        );

        if (file == null) {
            return;
        }

        Path selectedFile = file.toPath();

        try {
            StudentImportService.ImportResult result =
            switch (text) {
                case "From CSV" -> studentImportService.importCSV(selectedFile);
                case "From XLSX" -> studentImportService.importXLSX(selectedFile);
                case "From JSON" -> studentImportService.importJSON(selectedFile);
                default -> throw new IllegalArgumentException("Unsupported import format.");
            };

            StringBuilder message = new StringBuilder();

            message.append("Imported: %d student(s)\n".formatted(result.imported()));
            message.append("Skipped: %d student(s)".formatted(result.skipped()));

            if (result.hasErrors()) {
                message.append("\n\nErrors:");
                for (String error : result.errors()) {
                    message.append("\n• ").append(error);
                }
            }

            Ruangkelas.showAlert(
                result.hasErrors()
                        ? Alert.AlertType.WARNING
                        : Alert.AlertType.INFORMATION,
                "Student Import",
                message.toString()
            );

        } catch (Exception e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Student Import Failed",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "An unknown error occurred."
            );
        }
    }

    @FXML
    private void exportStudents(ActionEvent event) {
        MenuItem clickedItem = (MenuItem) event.getSource();

        String text = clickedItem.getText();

        String description;
        String extension;

        switch (text) {
            case "To CSV" -> {
                description = "CSV Files";
                extension = "*.csv";
            }

            case "To XLSX" -> {
                description = "Excel Workbook";
                extension = "*.xlsx";
            }

            case "To JSON" -> {
                description = "JSON Files";
                extension = "*.json";
            }

            default -> {
                return;
            }
        }

        File file = FileChooserUtil.chooseSaveFile(
                exportKelasMenuBtn
                        .getParentMenu()
                        .getParentPopup(),
                "Export Students Data",
                description,
                extension
        );

        if (file == null) {
            return;
        }

        Set<Student> students = studentService.getAll();

        try {
            switch (text) {
                case "To CSV" -> studentExportService.exportCSV(students, file.toPath());
                case "To XLSX" -> studentExportService.exportXLSX(students, file.toPath());
                case "To JSON" -> services.getJsonPersistence().saveStudents(
                        students,
                        file.toPath()
                );
                default -> throw new IllegalArgumentException("Unsupported import format.");
            };

            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Export Successful",
                    "Students was exported successfully."
            );

        } catch (PersistenceException | ExportException e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Export Failed",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void exportKelas(ActionEvent event) {
        if (selectedKelas == null) {
            return;
        }

        MenuItem clickedItem = (MenuItem) event.getSource();

        String text = clickedItem.getText();

        String description;
        String extension;

        switch (text) {
            case "To CSV" -> {
                description = "CSV Files";
                extension = "*.csv";
            }

            case "To XLSX" -> {
                description = "Excel Workbook";
                extension = "*.xlsx";
            }

            case "To JSON" -> {
                description = "JSON Files";
                extension = "*.json";
            }

            default -> {
                return;
            }
        }

        File file = FileChooserUtil.chooseSaveFile(
            exportKelasMenuBtn
                    .getParentMenu()
                    .getParentPopup(),
            "Export Kelas Data",
            description,
            extension
        );

        if (file == null) {
            return;
        }

        try {
            switch (text) {
                case "To CSV" -> kelasExportService.exportCSV(selectedKelas, file.toPath());
                case "To XLSX" -> kelasExportService.exportXLSX(selectedKelas, file.toPath());
                case "To JSON" -> services.getJsonPersistence().saveKelas(
                        selectedKelas,
                        file.toPath()
                );
                default -> throw new IllegalArgumentException("Unsupported import format.");
            };

            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Export Successful",
                    "Kelas data was exported successfully."
            );

        } catch (PersistenceException | ExportException e) {
            Ruangkelas.showAlert(
                Alert.AlertType.ERROR,
                "Export Failed",
                e.getMessage()
            );
        }
    }

    @FXML
    private void exportReport(ActionEvent event) {
        if (selectedKelas == null) {
            return;
        }

        File file = FileChooserUtil.chooseSaveFile(
            kelasSemester.getScene().getWindow(),
            "Export Kelas Report",
            "Excel Workbook (*.xlsx)",
            "*.xlsx",
            selectedKelas.getKelasId() + "_report.xlsx"
        );

        if (file == null) {
            return;
        }

        try {
            kelasExportService.exportReportXLSX(
                selectedKelas,
                file.toPath()
            );

            Ruangkelas.showAlert(
                Alert.AlertType.INFORMATION,
                "Export Successful",
                "Kelas report has been exported successfully."
            );

        } catch (ExportException e) {
            Ruangkelas.showAlert(
                Alert.AlertType.ERROR,
                "Export Failed",
                e.getMessage()
            );
        }
    }

    @FXML
    private void showUsageGuide() throws IOException {
        FXMLLoader loader = Ruangkelas.getLoader("usage");

        Parent root = loader.load();

        Scene scene = new Scene(root);

        Stage stage = Ruangkelas.createStage(
            scene,
            "Usage Guide",
            600,
            500
        );

        Ruangkelas.registerStage(stage);
        stage.show();
    }

    @FXML
    private void showAbout() throws IOException {
        FXMLLoader loader = Ruangkelas.getLoader("about");

        Parent root = loader.load();

        Scene scene = new Scene(root);

        Stage stage = Ruangkelas.createStage(
            scene,
            "About ruangkelas",
            500,
            420
        );

        Ruangkelas.registerStage(stage);
        stage.show();
    }
    @FXML
    private void manageAttendeesBtn() throws IOException {

        if (selectedKelas == null) {
            return;
        }

        FXMLLoader loader = Ruangkelas.getLoader("attendees");

        Parent root = loader.load();
        
        AttendeeController controller = loader.getController();
        controller.setKelas(selectedKelas);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
            Ruangkelas.getRkCSS()
        );

        Stage stage = Ruangkelas.createStage(
            scene,
            "Manage Attendees",
            600,
            500
        );
        
        Ruangkelas.registerStage(stage);
        stage.show();
    }

    @FXML
    private void viewAttendee() throws IOException {

        if (selectedKelas == null || selectedAttendee == null) {
            return;
        }

        FXMLLoader loader = Ruangkelas.getLoader("attendee");

        Parent root = loader.load();

        AttendeeViewController controller = loader.getController();
        controller.setup(selectedKelas, selectedAttendee);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
                Ruangkelas.getRkCSS()
        );

        Stage stage = Ruangkelas.createStage(
                scene,
                "Attendee %s".formatted(selectedAttendee.toString()),
                600,
                400
        );

        Ruangkelas.registerStage(stage);
        stage.show();
    }

    @FXML
    private void manageAssessmentBtn() throws IOException {

        if (selectedKelas == null || selectedAssessment == null) {
            return;
        }

        FXMLLoader loader = Ruangkelas.getLoader("assessments");

        Parent root = loader.load();

        AssessmentController controller = loader.getController();
        controller
            .setup(selectedKelas, selectedAssessment);

        Scene scene = new Scene(root);

        scene.getStylesheets().add(
            Ruangkelas.getRkCSS()
        );

        Stage stage = Ruangkelas.createStage(
                scene,
                "Manage Assessment %s".formatted(selectedAssessment.getName()),
                600,
                500
        );

        Ruangkelas.registerStage(stage);
        stage.show();
    }

    @FXML
    private void manageSessionBtn() throws IOException {

        if (selectedKelas == null || selectedSession == null) {
            return;
        }

        FXMLLoader loader = Ruangkelas.getLoader("sessions");

        Parent root = loader.load();

        SessionController controller = loader.getController();
        controller.setup(
                sessionListView.getSelectionModel().getSelectedIndex() + 1,
                selectedKelas,
                selectedSession
        );

        Scene scene = new Scene(root);
        Stage stage = Ruangkelas.createStage(
                scene,
                "Manage Session",
                600,
                400
        );

        Ruangkelas.registerStage(stage);
        stage.show();
    }
    
    @FXML
    private void createKelas() {
        
        ComboBox<Course> courseField = new ComboBox<>();
        courseField.getItems().addAll(courseService.getAll());
        
        TextField kelasIdField = new TextField();
        kelasIdField.setPromptText("Distinguish from same course class");

        CreatePrompt window = new CreatePrompt("Create Kelas");

        window
            .addField("Course", courseField)
            .addField("Kelas ID", kelasIdField);

        window.show().ifPresent(values -> {

            Course selectedCourse =
                (Course) values.get("Course");

            String kelasId =
                (String) values.get("Kelas ID");

            try {
                Kelas newKelas = kelasService.createKelas(
                    kelasId,
                    selectedCourse
                );

            } catch (Exception e) {
                 Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Failed to create Kelas",
                    e.getMessage()
                );
            }
        });
        
    }
    
    @FXML
    private void deleteKelas() {

       Optional<ButtonType> alert = Ruangkelas.showAlert(
            Alert.AlertType.CONFIRMATION,
            "Delete Confirmation",
            "All records of this Kelas will be deleted permanently."
        );

        if (alert.isPresent() && alert.get() != ButtonType.OK) {
            return;
        }
        
        try {
            kelasService.deleteKelas(selectedKelas);
            Ruangkelas.closeAllStages();
            refreshKelasList();
        } catch (Exception e) {
            Ruangkelas.showAlert(
                Alert.AlertType.ERROR,
                "Delete Failed",
                e.getMessage()
            );
        }
        
    }
    
    @FXML
    private void addAssessmentBtn() {   
        
        TextField assessmentName = new TextField();
        assessmentName.setPromptText("Assessment description");
        
        TextField assessmentWorth = new TextField();
        assessmentWorth.setPromptText("Assessment percentage worth");

        CreatePrompt window = new CreatePrompt("Add Assessment");

        window
            .addField("Description", assessmentName)
            .addField("Percentage", assessmentWorth);

        Ruangkelas.registerStage(window.getStage());

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

                kelasService.addAssessment(selectedKelas, asmName, asmWorth);
                refreshAssessmentList();
            } catch (NumberFormatException e) {
                Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Addition Failed",
                    "Percentage is not a number!"
                );
            } catch (IllegalArgumentException | PersistenceException e) {
                Ruangkelas.showAlert(
                  Alert.AlertType.ERROR,
                  "Addition Failed",
                  e.getMessage()
                );
            }
        });
        
    }

    @FXML
    private void createSessionBtn() {

        TextField topic = new TextField();
        topic.setPromptText("This session's topic");

        CreatePrompt window = new CreatePrompt("Add Assessment");

        window.addField("Session Topic", topic);

        Ruangkelas.registerStage(window.getStage());

        window.show().ifPresent(values -> {

            String sessTopic =
                    (String) values.get("Session Topic");

            try {
                selectedKelas.createSession(sessTopic);
                kelasService.saveData();

                refreshSessionList();

                Ruangkelas.showAlert(
                        Alert.AlertType.INFORMATION,
                        "Session Created",
                        "A session has been created with the topic: " + sessTopic
                );
            } catch (Exception e) {
                Ruangkelas.showAlert(
                  Alert.AlertType.ERROR,
                  "Create Failed",
                  e.getMessage()
                );
            }
        });

    }

    @FXML
    private void startSessionBtn() {
        try {
            selectedSession.start();
            kelasService.saveData();
            updateSessionButtons();
            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Session Started",
                    "Session has started."
            );
        } catch (Exception e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Session Start Fail",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void endSessionBtn() {
        try {
            selectedSession.end();
            kelasService.saveData();
            updateSessionButtons();
            Ruangkelas.showAlert(
                    Alert.AlertType.INFORMATION,
                    "Session Ended",
                    "Session has ended."
            );
        } catch (Exception e) {
            Ruangkelas.showAlert(
                    Alert.AlertType.ERROR,
                    "Session End Fail",
                    e.getMessage()
            );
        }
    }
        
    private boolean selectKelas(Kelas kelas) {
        if (Ruangkelas.stagesAdded()) {
            Optional<ButtonType> alert = Ruangkelas.showAlert(
                    Alert.AlertType.CONFIRMATION,
                    "Switch Kelas",
                    "All opened windows for this Kelas will be closed"
            );

            if (alert.isPresent() && alert.get() != ButtonType.OK) {
                return false;
            }
        }

        Ruangkelas.closeAllStages();

        selectedAttendee = null;
        selectedAssessment = null;
        selectedSession = null;

        courseName.setText(
            kelas.getCourseInfo().getCourseName() + " - " + kelas.getKelasId()
        );

        kelasSemester.setText(
            kelas.getCohortSemester().toString()
        );
        
        enableKelasPane(true);
        
        refreshLists();

        return true;
    }
    
    private void enableKelasPane(boolean limbo) {  
        limboPane
            .setVisible(!limbo);
        
        kelasDetailsPane
            .setVisible(limbo);
        
        deleteKelasMenuBtn
            .setDisable(!limbo);
        
        exportKelasMenuBtn
            .setDisable(!limbo);

        exportReportBtn
            .setDisable(!limbo);
        
    }
    
    private void enableViewAttendeeBtn(boolean status) {
        viewAttendeeBtn
            .setDisable(!status);
    }
    
    private void enableManageAssessmentBtn(boolean status) {
        manageAssessmentBtn
            .setDisable(!status);
    }
    
    private void refreshLists() {
        refreshKelasList();

        if (selectedKelas != null) {
            refreshAttendeeList();
            refreshAssessmentList();
            refreshSessionList();
        }
    }
    
    private void refreshKelasList() {
        refreshList(kelasList, kelasService.getAll());
    }
    
    private void refreshAttendeeList() {
        if (selectedKelas == null) return;
        
        refreshList(attendeeList, selectedKelas.getAttendees());
    }
    
    private void refreshAssessmentList() {
        if (selectedKelas == null) return;
        
        refreshList(assessmentList, selectedKelas.getAssessments());
    }
    
    private void refreshSessionList() {
        if (selectedKelas == null) return;
        
        refreshList(sessionList, selectedKelas.getSessions());
        sessionListView.getItems().sort(Comparator.comparing(Session::getCreatedAt));
    }

    private void updateSessionButtons() {
        startSessionBtn.setDisable(
                selectedSession == null
                    || selectedSession.hasStarted()
        );

        endSessionBtn.setDisable(
                selectedSession == null
                    || !selectedSession.hasStarted()
                    || selectedSession.hasEnded()
        );

        manageSessionBtn.setDisable(
                selectedSession == null
        );
    }
    
    <T>
    void refreshList(ObservableList<T> list, Set<T> data) {
        list.setAll(data);
    }
    
    private void refreshAvailableAttendees(String query) {
        String q = query.trim().toLowerCase();
        
        filteredAttendeeList.setPredicate(
            attendee -> attendee.getStudent().getNIM().toLowerCase().contains(q)
                    || attendee.getStudent().getNama().toLowerCase().contains(q)
        );
    }
    
    private void searchAttendees(
            ObservableValue<? extends String> observable,
            String oldValue,
            String newValue
    ) {
        refreshAvailableAttendees(newValue);
    }

}