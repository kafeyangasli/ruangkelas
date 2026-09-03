package ui;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import repository.Directories;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileChooserUtil {

    private FileChooserUtil() { }

    public static File chooseOpenFile(
            Window owner,
            String title,
            String description,
            String extension
    ) {
        FileChooser chooser = createChooser(
                title,
                description,
                extension
        );

        return chooser.showOpenDialog(owner);
    }

    public static File chooseSaveFile(
            Window owner,
            String title,
            String description,
            String extension
    ) {
        FileChooser chooser = createChooser(
                title,
                description,
                extension
        );

        return chooser.showSaveDialog(owner);
    }

    public static File chooseSaveFile(
            Window owner,
            String title,
            String description,
            String extension,
            String initialFileName
    ) {
        FileChooser chooser = createChooser(
                title,
                description,
                extension
        );

        chooser.setInitialFileName(initialFileName);

        return chooser.showSaveDialog(owner);
    }

    private static FileChooser createChooser(
            String title,
            String description,
            String extension
    ) {
        FileChooser chooser = new FileChooser();

        chooser.setTitle(title);

        Path downloads = Directories.downloads;

        if (Files.isDirectory(downloads)) {
            chooser.setInitialDirectory(
                    downloads.toFile()
            );
        }

        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter(
                        description,
                        extension
                )
        );

        return chooser;
    }
}