package service;

import exceptions.ExportException;
import models.Student;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class StudentExportService implements ExportService<Set<Student>> {

    public void exportCSV(Set<Student> students, Path path) throws ExportException {
        Objects.requireNonNull(students, "students");
        Objects.requireNonNull(path, "path");

        try {
            createParentDirectory(path);

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writeCSVRow(writer, "nim", "nama", "cohort");

                for (Student student : sortedStudents(students)) {
                    writeCSVRow(
                            writer,
                            student.getNIM(),
                            student.getNama(),
                            student.getCohort()
                    );
                }
            }
        } catch (IOException e) {
            throw new ExportException(
                    "Failed to export students to CSV.",
                    e
            );
        }
    }

    private void writeCSVRow(BufferedWriter writer, Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                writer.write(",");
            }

            Object value = values[i];

            writer.write(
                    csvEscape(
                            value == null ? "" : value.toString()
                    )
            );
        }

        writer.newLine();
    }

    private String csvEscape(String value) {
        boolean needsQuotes =
                value.contains(",")
                        || value.contains("\"")
                        || value.contains("\n")
                        || value.contains("\r");

        if (!needsQuotes) {
            return value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public void exportXLSX(Set<Student> students, Path path) throws ExportException {
        Objects.requireNonNull(students, "students");
        Objects.requireNonNull(path, "path");

        try {
            createParentDirectory(path);

            List<List<Object>> rows = studentRows(students);
            List<String> sharedStrings = buildSharedStrings(rows);

            try (
                    OutputStream output = Files.newOutputStream(path);
                    ZipOutputStream zip = new ZipOutputStream(output)
            ) {
                writeEntry(zip, "[Content_Types].xml", contentTypes());
                writeEntry(zip, "_rels/.rels", rootRelationships());
                writeEntry(zip, "xl/workbook.xml", workbook());
                writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRelationships());
                writeEntry(zip, "xl/styles.xml", styles());
                writeEntry(zip, "xl/sharedStrings.xml", sharedStrings(sharedStrings));
                writeEntry(zip, "xl/worksheets/sheet1.xml", sheet(rows, sharedStrings));
            }
        } catch (IOException e) {
            throw new ExportException(
                    "Failed to export students to XLSX.",
                    e
            );
        }
    }

    private List<Student> sortedStudents(Set<Student> students) {
        return students.stream()
                .sorted(
                        Comparator.comparing(
                                Student::getNIM,
                                Comparator.nullsFirst(String::compareTo)
                        )
                )
                .toList();
    }

    private List<List<Object>> studentRows(Set<Student> students) {
        List<List<Object>> rows = new ArrayList<>();

        rows.add(List.of(
                "nim",
                "nama",
                "cohort"
        ));

        for (Student student : sortedStudents(students)) {
            rows.add(List.of(
                    student.getNIM(),
                    student.getNama(),
                    student.getCohort()
            ));
        }

        return rows;
    }

    private List<String> buildSharedStrings(List<List<Object>> rows) {
        List<String> strings = new ArrayList<>();

        for (List<Object> row : rows) {
            for (Object value : row) {
                if (value instanceof String string && !strings.contains(string)) {
                    strings.add(string);
                }
            }
        }

        return strings;
    }

    private void writeEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));

        zip.write(
                content.getBytes(StandardCharsets.UTF_8)
        );

        zip.closeEntry();
    }

    private String contentTypes() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>

                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">

                  <Default
                    Extension="rels"
                    ContentType="application/vnd.openxmlformats-package.relationships+xml"/>

                  <Default
                    Extension="xml"
                    ContentType="application/xml"/>

                  <Override
                    PartName="/xl/workbook.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>

                  <Override
                    PartName="/xl/styles.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>

                  <Override
                    PartName="/xl/sharedStrings.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>

                  <Override
                    PartName="/xl/worksheets/sheet1.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>

                </Types>
                """;
    }

    private String rootRelationships() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>

                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">

                  <Relationship
                    Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    Target="xl/workbook.xml"/>

                </Relationships>
                """;
    }

    private String workbook() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>

                <workbook
                  xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">

                  <sheets>

                    <sheet
                      name="Students"
                      sheetId="1"
                      r:id="rId1"/>

                  </sheets>

                </workbook>
                """;
    }

    private String workbookRelationships() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>

                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">

                  <Relationship
                    Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                    Target="worksheets/sheet1.xml"/>

                  <Relationship
                    Id="rId2"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
                    Target="styles.xml"/>

                  <Relationship
                    Id="rId3"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings"
                    Target="sharedStrings.xml"/>

                </Relationships>
                """;
    }

    private String styles() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>

                <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">

                  <fonts count="1">
                    <font>
                      <sz val="11"/>
                      <name val="Arial"/>
                    </font>
                  </fonts>

                  <fills count="2">

                    <fill>
                      <patternFill patternType="none"/>
                    </fill>

                    <fill>
                      <patternFill patternType="gray125"/>
                    </fill>

                  </fills>

                  <borders count="1">
                    <border>
                      <left/>
                      <right/>
                      <top/>
                      <bottom/>
                      <diagonal/>
                    </border>
                  </borders>

                  <cellStyleXfs count="1">
                    <xf
                      numFmtId="0"
                      fontId="0"
                      fillId="0"
                      borderId="0"/>
                  </cellStyleXfs>

                  <cellXfs count="1">
                    <xf
                      numFmtId="0"
                      fontId="0"
                      fillId="0"
                      borderId="0"
                      xfId="0"/>
                  </cellXfs>

                </styleSheet>
                """;
    }

    private String sharedStrings(List<String> strings) {
        StringBuilder xml = new StringBuilder();

        xml.append("""
                <?xml version="1.0" encoding="UTF-8"?>
                <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                """);

        xml.append(" count=\"")
                .append(strings.size())
                .append("\"");

        xml.append(" uniqueCount=\"")
                .append(strings.size())
                .append("\">");

        for (String string : strings) {
            xml.append("<si><t>")
                    .append(xmlEscape(string))
                    .append("</t></si>");
        }

        xml.append("</sst>");

        return xml.toString();
    }

    private String sheet(List<List<Object>> rows, List<String> sharedStrings) {
        StringBuilder xml = new StringBuilder();

        xml.append("""
                <?xml version="1.0" encoding="UTF-8"?>

                <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">

                  <sheetData>
                """);

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<Object> row = rows.get(rowIndex);
            int excelRow = rowIndex + 1;

            xml.append("<row r=\"")
                    .append(excelRow)
                    .append("\">");

            for (int column = 0; column < row.size(); column++) {
                Object value = row.get(column);

                String reference =
                        columnName(column) + excelRow;

                if (value instanceof String string) {
                    int index = sharedStrings.indexOf(string);

                    xml.append("<c r=\"")
                            .append(reference)
                            .append("\" t=\"s\">");

                    xml.append("<v>")
                            .append(index)
                            .append("</v>");

                    xml.append("</c>");

                } else if (value != null) {
                    xml.append("<c r=\"")
                            .append(reference)
                            .append("\">");

                    xml.append("<v>")
                            .append(xmlEscape(value.toString()))
                            .append("</v>");

                    xml.append("</c>");
                }
            }

            xml.append("</row>");
        }

        xml.append("""
                  </sheetData>

                </worksheet>
                """);

        return xml.toString();
    }

    private String columnName(int index) {
        StringBuilder result = new StringBuilder();
        int value = index + 1;

        while (value > 0) {
            int remainder = (value - 1) % 26;

            result.insert(
                    0,
                    (char) ('A' + remainder)
            );

            value = (value - 1) / 26;
        }

        return result.toString();
    }

    private String xmlEscape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private void createParentDirectory(Path path) throws IOException {
        Path parent =
                path.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}