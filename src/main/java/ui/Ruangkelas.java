package ui;

import controllers.Controller;
import controllers.MainController;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import service.Services;

public class Ruangkelas extends Application {
    private static final Image rkIcon = new Image(Objects.requireNonNull(Ruangkelas.class.getResource("/imgs/rk_icon.png")).toExternalForm());
    private static final String rkCSS = Objects.requireNonNull(Ruangkelas.class.getResource("/css/cupertino-light.css")).toExternalForm();
    
    private static final URL rkHome = Ruangkelas.class.getResource("/fxml/rk_home.fxml");
    private static final URL rkCourses = Ruangkelas.class.getResource("/fxml/rk_courses.fxml");
    private static final URL rkStudents = Ruangkelas.class.getResource("/fxml/rk_students.fxml");
    private static final URL rkAttendees = Ruangkelas.class.getResource("/fxml/rk_attendees.fxml");
    private static final URL rkAttendeeView = Ruangkelas.class.getResource("/fxml/rk_attendeeview.fxml");
    private static final URL rkAssessments = Ruangkelas.class.getResource("/fxml/rk_assessments.fxml");
    private static final URL rkSessions = Ruangkelas.class.getResource("/fxml/rk_sessions.fxml");
    private static final URL rkAbout = Ruangkelas.class.getResource("/fxml/rk_about.fxml");
    private static final URL rkUsage = Ruangkelas.class.getResource("/fxml/rk_usage.fxml");
    
    private static final Services services = new Services();

    private static final Set<Stage> stages =
            Collections.newSetFromMap(new WeakHashMap<>());

    private static boolean closedFromButton = false;

    public static void setClosedFromButton(boolean newStatus) {
        closedFromButton = newStatus;
    }

    public static boolean stagesAdded() {
        return !stages.isEmpty();
    }

    public static void registerStage(Stage stage) {
        stages.add(stage);
        stage.setOnHidden(event -> stages.remove(stage));
    }

    public static void closeAllStages() {
        for (Stage stage : new ArrayList<>(stages)) {
            stage.close();
        }

        stages.clear();
    }
    
    public static Services getServices() {
        return services;
    }
    
    public static Image getRkIcon() {
        return rkIcon;
    }
    
    public static String getRkCSS() {
        return rkCSS;
    }
    
    public static URL getPage(String page) {
        return switch(page) {
            case "home" -> rkHome;
            case "courses" -> rkCourses;
            case "students" -> rkStudents;
            case "attendees" -> rkAttendees;
            case "assessments" -> rkAssessments;
            case "sessions" -> rkSessions;
            case "attendee" -> rkAttendeeView;
            case "about" -> rkAbout;
            case "usage" -> rkUsage;
            default -> throw new IllegalArgumentException("Unknown page: " + page);
        };
    }
    
    public static Stage createStage(Scene scene, String title, int width, int height) {
        Stage stage = new Stage();
        
        stage.setTitle("%s - ruangkelas".formatted(title));
        stage.getIcons().setAll(Ruangkelas.getRkIcon());
        stage.setScene(scene);
        stage.setMinWidth(width);
        stage.setMinHeight(height);
        
        return stage;
    }
    
    public static FXMLLoader getLoader(String page) {
        FXMLLoader loader = new FXMLLoader(getPage(page));

        loader.setControllerFactory(controllerClass -> {
            try {
                Object controller =
                    controllerClass.getDeclaredConstructor().newInstance();

                if (controller instanceof Controller c) {
                    c.setServices(services);
                }

                return controller;

            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        return loader;
    }
    
    public static Optional<ButtonType> showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(rkCSS);

        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.getIcons().add(rkIcon);
        
        return alert.showAndWait();
    }

    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader = getLoader("home");

        Parent root = loader.load();

        Scene scene = new Scene(root);
        
        stage.setTitle("ruangkelas");
        stage.getIcons().add(rkIcon);
        stage.setScene(scene);
        stage.show();
        
        stage.setOnCloseRequest(
            event -> {
                if (!closedFromButton) {
                    services.saveAll();
                    Platform.exit();
                }
            }
        );
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}