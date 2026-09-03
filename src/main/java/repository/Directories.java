package repository;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Directories {
    public static final Path data = Path.of(System.getProperty("user.home"), ".ruangkelas");
    public static final Path kelasData = data.resolve("rk_repos.json");
    public static final Path studentData = data.resolve("rk_students.json");
    public static final Path courseData = data.resolve("rk_courses.json");
    public static final Path downloads = Paths.get(System.getProperty("user.home"), "Downloads");
}
