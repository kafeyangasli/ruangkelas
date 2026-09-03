package controllers;

import javafx.stage.Stage;

import service.Services;

import java.util.HashSet;
import java.util.Set;

public abstract class Controller {

    protected Services services;

    protected static final Set<Stage> stages = new HashSet<>();

    protected void addStage(Stage stage) {
        stages.add(stage);
        stage.setOnHidden(event -> stages.remove(stage));
    }

    protected static void closeAllStage() {
        for (Stage stage : Set.copyOf(stages)) {
            stage.close();
        }

        stages.clear();
    }

    public void cleanup() {
        // Default: nothing to clean up
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }
}