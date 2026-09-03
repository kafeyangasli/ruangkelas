package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

// with the help of chatgpt :)

public class CreatePrompt {

    private final String title;
    private final Map<String, Node> fields = new LinkedHashMap<>();
    private final Stage stage = new Stage();

    public CreatePrompt(String title) {
        this.title = title;
    }

    public Stage getStage() {
        return stage;
    }

    public CreatePrompt addField(String name, Node field) {
        fields.put(name, field);
        return this;
    }

    public Optional<Map<String, Object>> show() {
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox form = new VBox(10);
        form.setPadding(new Insets(20));

        for (Map.Entry<String, Node> entry : fields.entrySet()) {
            Label label = new Label(entry.getKey());

            Node field = entry.getValue();

            if (field instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }

            form.getChildren().addAll(label, field);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button cancelButton = new Button("Cancel");
        Button createButton = new Button("Create");

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(
            cancelButton,
            createButton
        );

        form.getChildren().addAll(
            spacer,
            buttons
        );

        final Map<String, Object>[] result = new Map[]{null};

        cancelButton.setOnAction(event -> {
            stage.close();
        });

        createButton.setOnAction(event -> {
            if (!this.fieldsCompleted()) {
                showFieldsNotCompleted(); 
                return;
            }

            Map<String, Object> values = new LinkedHashMap<>();

            for (Map.Entry<String, Node> entry : fields.entrySet()) {
                values.put(
                    entry.getKey(),
                    getValue(entry.getValue())
                );
            }

            result[0] = values;
            stage.close();
        });

        Scene scene = new Scene(form);

        scene.getStylesheets().add(
            Ruangkelas.getRkCSS()
        );
        
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER), () -> {
            if (!this.fieldsCompleted()) {
                showFieldsNotCompleted(); 
                return;
            }

            Map<String, Object> values = new LinkedHashMap<>();

            for (Map.Entry<String, Node> entry : fields.entrySet()) {
                values.put(
                    entry.getKey(),
                    getValue(entry.getValue())
                );
            }

            result[0] = values;
            stage.close();
        });

        stage.setScene(scene);
        stage.getIcons().add(Ruangkelas.getRkIcon());

        stage.showAndWait();

        return Optional.ofNullable(result[0]);
    }
    
    private void showFieldsNotCompleted() {
        Ruangkelas.showAlert(Alert.AlertType.ERROR, "Fields Missing", "All fields must be filled.");
    }
    
    private boolean fieldsCompleted() {
        for (Map.Entry<String, Node> entry : fields.entrySet()) {
            Object value = getValue(entry.getValue());
            if (value == null) {
                return false;
            }
            
            if (value instanceof String string && string.isBlank()) {
                return false;
            }
        }
        
        return true;
    }

    private Object getValue(Node node) {

        if (node instanceof TextField textField) {
            return textField.getText().trim();
        }

        if (node instanceof ComboBox<?> comboBox) {
            return comboBox.getValue();
        }

        if (node instanceof CheckBox checkBox) {
            return checkBox.isSelected();
        }

        if (node instanceof DatePicker datePicker) {
            return datePicker.getValue();
        }

        throw new IllegalArgumentException(
            "Unsupported input type: "
            + node.getClass().getName()
        );
    }
}