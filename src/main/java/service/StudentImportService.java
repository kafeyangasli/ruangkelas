package service;

import models.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// with the help of beloved chatgpt :)

public class StudentImportService {

    private final StudentService studentService;

    public StudentImportService(StudentService studentService) {
        this.studentService = studentService;
    }
    
    /*
    * Expected format:
    *
    * nim, nama, cohort
    */
    public ImportResult importCSV(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(
                path,
                StandardCharsets.UTF_8
        )) {
            reader.readLine(); // skip first
            
            List<String[]> rows = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    rows.add(parseCSVLine(line));
                }
            }

            return importRows(rows);
        }
    }
    
    /*
    * Expected format:
    *
    * [
    *   {
    *     "nim": "240101",
    *     "nama": "Budi",
    *     "cohort": 2024
    *   }
    * ]
    */
    public ImportResult importJSON(Path path) throws IOException {
        String json = Files.readString(path, StandardCharsets.UTF_8).trim();

        if (json.isEmpty()) {
            throw new IllegalArgumentException("JSON file is empty.");
        }

        if (!json.startsWith("[") || !json.endsWith("]")) {
            throw new IllegalArgumentException(
                "JSON must contain an array of student objects."
            );
        }

        List<String[]> rows = new ArrayList<>();
        String content = json.substring(1, json.length() - 1).trim();

        if (content.isEmpty()) {
            return new ImportResult(0, 0, List.of());
        }

        List<String> objects = splitJSONObjects(content);

        for (String object : objects) {
            Map<String, String> values = parseJSONObject(object);

            String nim = getValue(
                values,
                "nim",
                "student_id",
                "studentid",
                "studentId"
            );

            String nama = getValue(
                values,
                "nama",
                "name",
                "student_name",
                "studentname",
                "studentName"
            );

            String cohort = getValue(
                values,
                "cohort",
                "cohort_year",
                "cohortyear",
                "cohortYear",
                "angkatan"
            );

            rows.add(new String[] {
                nim,
                nama,
                cohort
            });
        }

        return importRows(rows);
    }

    /*
     * XLSX
     */
    public ImportResult importXLSX(Path path) throws Exception {
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(path.toFile())) {

            Map<Integer, String> sharedStrings = readSharedStrings(zip);

            java.util.zip.ZipEntry sheetEntry =
                zip.getEntry("xl/worksheets/sheet1.xml");

            if (sheetEntry == null) {
                throw new IllegalArgumentException(
                    "Could not find the first worksheet."
                );
            }

            try (InputStream input = zip.getInputStream(sheetEntry)) {

                javax.xml.parsers.DocumentBuilderFactory factory =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();

                factory.setNamespaceAware(true);

                var document =
                    factory.newDocumentBuilder().parse(input);

                var cells =
                    document.getElementsByTagNameNS(
                        "http://schemas.openxmlformats.org/spreadsheetml/2006/main",
                        "row"
                    );

                List<String[]> rows = new ArrayList<>();

                for (int i = 0; i < cells.getLength(); i++) {

                    var row = cells.item(i);

                    var cellNodes =
                        ((org.w3c.dom.Element) row).getElementsByTagNameNS(
                            "http://schemas.openxmlformats.org/spreadsheetml/2006/main",
                            "c"
                        );

                    Map<Integer, String> rowValues = new HashMap<>();

                    for (int j = 0; j < cellNodes.getLength(); j++) {

                        var cell =
                            (org.w3c.dom.Element) cellNodes.item(j);

                        String reference =
                            cell.getAttribute("r");

                        int column =
                            columnIndex(reference);

                        String type =
                            cell.getAttribute("t");

                        var valueNodes =
                            cell.getElementsByTagNameNS(
                                "http://schemas.openxmlformats.org/spreadsheetml/2006/main",
                                "v"
                            );

                        if (valueNodes.getLength() == 0) {
                            continue;
                        }

                        String value =
                            valueNodes.item(0).getTextContent();

                        if ("s".equals(type)) {
                            value =
                                sharedStrings.getOrDefault(
                                    Integer.parseInt(value),
                                    ""
                                );
                        }

                        rowValues.put(column, value);
                    }

                    if (!rowValues.isEmpty()) {
                        int maxColumn =
                            rowValues.keySet()
                                .stream()
                                .max(Integer::compareTo)
                                .orElse(0);

                        String[] values =
                            new String[maxColumn + 1];

                        for (int column = 0; column <= maxColumn; column++) {
                            values[column] =
                                rowValues.getOrDefault(column, "");
                        }

                        rows.add(values);
                    }
                }

                return importRows(rows);
            }
        }
    }

    private ImportResult importRows(List<String[]> rows) {

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                "The import file contains no data."
            );
        }

        int startIndex = hasHeader(rows.get(0)) ? 1 : 0;

        Set<String> importedNIMs = new HashSet<>();

        int imported = 0;
        int skipped = 0;

        List<String> errors = new ArrayList<>();

        for (int i = startIndex; i < rows.size(); i++) {

            String[] row = rows.get(i);

            if (row.length < 3) {
                errors.add(
                    "Row %d: expected NIM, name and cohort."
                        .formatted(i + 1)
                );
                continue;
            }

            String nim = clean(row[0]);
            String nama = clean(row[1]);
            String cohortText = clean(row[2]);

            if (nim.isEmpty()) {
                errors.add(
                    "Row %d: NIM is empty."
                        .formatted(i + 1)
                );
                continue;
            }

            if (nama.isEmpty()) {
                errors.add(
                    "Row %d: name is empty."
                        .formatted(i + 1)
                );
                continue;
            }

            int cohort;

            try {
                cohort = Integer.parseInt(cohortText);
            } catch (NumberFormatException e) {
                errors.add(
                    "Row %d: invalid cohort '%s'."
                        .formatted(i + 1, cohortText)
                );
                continue;
            }

            if (!importedNIMs.add(nim)) {
                skipped++;
                continue;
            }

            /*
             * Student.equals() compares NIM, so this checks whether
             * the student already exists in the repository.
             */
            Student existing = findStudent(nim);

            if (existing != null) {
                skipped++;
                continue;
            }

            try {
                studentService.createStudent(
                    nim,
                    nama,
                    cohort
                );

                imported++;

            } catch (Exception e) {
                errors.add(
                    "Row %d: could not create student '%s': %s"
                        .formatted(
                            i + 1,
                            nim,
                            e.getMessage()
                        )
                );
            }
        }

        return new ImportResult(
            imported,
            skipped,
            errors
        );
    }

    private Student findStudent(String nim) {

        for (Student student : studentService.getAll()) {
            if (student.getNIM().equals(nim)) {
                return student;
            }
        }

        return null;
    }

    private boolean hasHeader(String[] row) {

        if (row.length == 0) {
            return false;
        }

        String first = clean(row[0]).toLowerCase();

        return first.equals("nim")
            || first.equals("student_id")
            || first.equals("studentid");
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }

        return value
            .trim()
            .replace("\uFEFF", "");
    }

    private String[] parseCSVLine(String line) {

        List<String> values = new ArrayList<>();

        StringBuilder current = new StringBuilder();

        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (quoted
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    quoted = !quoted;
                }

            } else if (c == ',' && !quoted) {

                values.add(current.toString());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        values.add(current.toString());

        return values.toArray(new String[0]);
    }

    private List<String> splitJSONObjects(String content) {

        List<String> objects = new ArrayList<>();

        boolean quoted = false;
        int depth = 0;
        int start = -1;

        for (int i = 0; i < content.length(); i++) {

            char c = content.charAt(i);

            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                quoted = !quoted;
                continue;
            }

            if (quoted) {
                continue;
            }

            if (c == '{') {

                if (depth == 0) {
                    start = i;
                }

                depth++;

            } else if (c == '}') {

                depth--;

                if (depth == 0 && start >= 0) {
                    objects.add(
                        content.substring(start, i + 1)
                    );
                    start = -1;
                }
            }
        }

        return objects;
    }

    private Map<String, String> parseJSONObject(String object) {

        String content =
            object
                .trim()
                .replaceAll("^\\{", "")
                .replaceAll("\\}$", "");

        Map<String, String> values = new HashMap<>();

        boolean quoted = false;
        boolean escaped = false;
        int start = 0;

        List<String> fields = new ArrayList<>();

        for (int i = 0; i < content.length(); i++) {

            char c = content.charAt(i);

            if (c == '"' && !escaped) {
                quoted = !quoted;
            }

            if (c == ',' && !quoted) {
                fields.add(content.substring(start, i));
                start = i + 1;
            }

            escaped = c == '\\' && !escaped;
        }

        fields.add(content.substring(start));

        for (String field : fields) {

            int colon = field.indexOf(':');

            if (colon < 0) {
                continue;
            }

            String key =
                cleanJSONValue(
                    field.substring(0, colon)
                );

            String value =
                cleanJSONValue(
                    field.substring(colon + 1)
                );

            values.put(key, value);
        }

        return values;
    }

    private String cleanJSONValue(String value) {

        value = value.trim();

        if (value.startsWith("\"")
                && value.endsWith("\"")
                && value.length() >= 2) {

            value =
                value.substring(
                    1,
                    value.length() - 1
                );

            value =
                value
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }

        return value;
    }

    private String getValue(
        Map<String, String> values,
        String... keys
    ) {

        for (String key : keys) {

            String value = values.get(key);

            if (value != null) {
                return value;
            }
        }

        return "";
    }

    private int columnIndex(String reference) {

        int result = 0;

        for (int i = 0; i < reference.length(); i++) {

            char c = reference.charAt(i);

            if (!Character.isLetter(c)) {
                break;
            }

            result =
                result * 26
                + (Character.toUpperCase(c) - 'A' + 1);
        }

        return result - 1;
    }

    private Map<Integer, String> readSharedStrings(
        java.util.zip.ZipFile zip
    ) throws Exception {

        Map<Integer, String> result = new HashMap<>();

        java.util.zip.ZipEntry entry =
            zip.getEntry("xl/sharedStrings.xml");

        if (entry == null) {
            return result;
        }

        try (InputStream input = zip.getInputStream(entry)) {

            var factory =
                javax.xml.parsers.DocumentBuilderFactory
                    .newInstance();

            factory.setNamespaceAware(true);

            var document =
                factory
                    .newDocumentBuilder()
                    .parse(input);

            var strings =
                document.getElementsByTagNameNS(
                    "http://schemas.openxmlformats.org/spreadsheetml/2006/main",
                    "si"
                );

            for (int i = 0; i < strings.getLength(); i++) {

                var node = strings.item(i);

                result.put(
                    i,
                    node.getTextContent()
                );
            }
        }

        return result;
    }

    public record ImportResult(
        int imported,
        int skipped,
        List<String> errors
    ) {

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}