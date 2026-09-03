package service;

import exceptions.ExportException;
import models.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KelasExportService implements ExportService<Kelas> {

    public void exportCSV(Kelas kelas, Path path) throws ExportException {
        Objects.requireNonNull(kelas, "kelas");
        Objects.requireNonNull(path, "path");

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8
                     )) {

            writeCSVRow(
                    writer,
                    "record_type",
                    "kelas_uuid",
                    "kelas_id",
                    "course_id",
                    "course_name",
                    "semester_year",
                    "semester_type",
                    "attendee_uuid",
                    "nim",
                    "student_name",
                    "cohort",
                    "assessment_uuid",
                    "assessment_name",
                    "assessment_percentage",
                    "session_uuid",
                    "session_created_at",
                    "session_topic",
                    "session_notes",
                    "session_start",
                    "session_end",
                    "attendance_status",
                    "attendance_recorded_at",
                    "attendance_updated_at",
                    "grade"
            );

            writeCSVRow(
                    writer,
                    "KELAS",
                    kelas.getUUID(),
                    kelas.getKelasId(),
                    kelas.getCourseInfo().getCourseId(),
                    kelas.getCourseInfo().getCourseName(),
                    kelas.getCohortSemester().getYear(),
                    kelas.getCohortSemester().getJenis_Semester().name()
            );

            for (Attendee attendee : sortedAttendees(kelas)) {
                Student student = attendee.getStudent();

                writeCSVRow(
                        writer,
                        "ATTENDEE",
                        kelas.getUUID(),
                        kelas.getKelasId(),
                        null, null, null, null,
                        attendee.getUUID(),
                        student.getNIM(),
                        student.getNama(),
                        student.getCohort()
                );

                for (Attendance attendance :
                        sortedAttendances(attendee)) {

                    Session session = attendance.getSession();

                    writeCSVRow(
                            writer,
                            "ATTENDANCE",
                            kelas.getUUID(),
                            kelas.getKelasId(),
                            null, null, null, null,
                            attendee.getUUID(),
                            student.getNIM(),
                            student.getNama(),
                            student.getCohort(),
                            null, null, null,
                            session.getUUID(),
                            session.getCreatedAt(),
                            session.getTopic(),
                            session.getNotes(),
                            session.getStartTime(),
                            session.getEndTime(),
                            attendance.getStatus(),
                            attendance.getRecordedAt(),
                            attendance.getUpdatedAt()
                    );
                }
            }

            for (Assessment assessment : sortedAssessments(kelas)) {
                writeCSVRow(
                        writer,
                        "ASSESSMENT",
                        kelas.getUUID(),
                        kelas.getKelasId(),
                        null, null, null, null,
                        null, null, null, null,
                        assessment.getUUID(),
                        assessment.getName(),
                        assessment.getPercentage()
                );
            }

            for (Session session : sortedSessions(kelas)) {
                writeCSVRow(
                        writer,
                        "SESSION",
                        kelas.getUUID(),
                        kelas.getKelasId(),
                        null, null, null, null,
                        null, null, null, null,
                        null, null, null,
                        session.getUUID(),
                        session.getCreatedAt(),
                        session.getTopic(),
                        session.getNotes(),
                        session.getStartTime(),
                        session.getEndTime()
                );
            }

            for (Assessment assessment : sortedAssessments(kelas)) {
                for (Grade grade : kelas.getGrades(assessment)) {
                    Attendee attendee = grade.getAttendee();
                    Student student = attendee.getStudent();

                    writeCSVRow(
                            writer,
                            "GRADE",
                            kelas.getUUID(),
                            kelas.getKelasId(),
                            null, null, null, null,
                            attendee.getUUID(),
                            student.getNIM(),
                            student.getNama(),
                            student.getCohort(),
                            assessment.getUUID(),
                            assessment.getName(),
                            assessment.getPercentage(),
                            null, null, null, null, null, null, null, null,
                            grade.getGrade()
                    );
                }
            }

        } catch (IOException e) {
            throw new ExportException(
                    "Failed to export Kelas to CSV.",
                    e
            );
        }
    }

    public void exportXLSX(Kelas kelas, Path path) throws ExportException {
        Objects.requireNonNull(kelas, "kelas");
        Objects.requireNonNull(path, "path");

        try (OutputStream output = Files.newOutputStream(path);
             ZipOutputStream zip = new ZipOutputStream(output)) {

            writeEntry(zip, "[Content_Types].xml", contentTypes(6));
            writeEntry(zip, "_rels/.rels", rootRelationships());
            writeEntry(zip, "xl/workbook.xml", workbook(
                    "Kelas", "Attendees", "Assessments",
                    "Sessions", "Attendance", "Grades"
            ));
            writeEntry(zip, "xl/_rels/workbook.xml.rels",
                    workbookRelationships(6));
            writeEntry(zip, "xl/styles.xml", styles());

            writeEntry(zip, "xl/worksheets/sheet1.xml",
                    sheet(kelasRows(kelas)));
            writeEntry(zip, "xl/worksheets/sheet2.xml",
                    sheet(attendeeRows(kelas)));
            writeEntry(zip, "xl/worksheets/sheet3.xml",
                    sheet(assessmentRows(kelas)));
            writeEntry(zip, "xl/worksheets/sheet4.xml",
                    sheet(sessionRows(kelas)));
            writeEntry(zip, "xl/worksheets/sheet5.xml",
                    sheet(attendanceRows(kelas)));
            writeEntry(zip, "xl/worksheets/sheet6.xml",
                    sheet(gradeRows(kelas)));

        } catch (IOException e) {
            throw new ExportException(
                    "Failed to export Kelas to XLSX.",
                    e
            );
        }
    }

    public void exportReportXLSX(Kelas kelas, Path path)
            throws ExportException {

        Objects.requireNonNull(kelas, "kelas");
        Objects.requireNonNull(path, "path");

        try (OutputStream output = Files.newOutputStream(path);
             ZipOutputStream zip = new ZipOutputStream(output)) {

            writeEntry(zip, "[Content_Types].xml", contentTypes(2));
            writeEntry(zip, "_rels/.rels", rootRelationships());

            writeEntry(
                    zip,
                    "xl/workbook.xml",
                    workbook("Student Report", "Session Report")
            );

            writeEntry(
                    zip,
                    "xl/_rels/workbook.xml.rels",
                    workbookRelationships(2)
            );

            writeEntry(zip, "xl/styles.xml", styles());

            writeEntry(
                    zip,
                    "xl/worksheets/sheet1.xml",
                    sheet(buildStudentReport(kelas))
            );

            writeEntry(
                    zip,
                    "xl/worksheets/sheet2.xml",
                    sheet(buildSessionReport(kelas))
            );

        } catch (IOException e) {
            throw new ExportException(
                    "Failed to export Kelas report.",
                    e
            );
        }
    }

    private List<List<Object>> buildStudentReport(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        List<Assessment> assessments = sortedAssessments(kelas);
        List<Session> sessions = sortedSessions(kelas);

        List<Object> header = new ArrayList<>();

        header.add("NIM");
        header.add("Name");

        for (Assessment assessment : assessments) {
            header.add(assessment.getName());
        }

        header.add("Final Grade");

        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);

            String topic = session.getTopic();

            header.add(
                    topic == null || topic.isBlank()
                            ? "Session " + (i + 1)
                            : "Session " + (i + 1) + " - " + topic
            );
        }

        rows.add(header);

        for (Attendee attendee : sortedAttendees(kelas)) {

            Student student = attendee.getStudent();

            List<Object> row = new ArrayList<>();

            row.add(student.getNIM());
            row.add(student.getNama());

            for (Assessment assessment : assessments) {

                Grade grade =
                        kelas.getGradeByAssessment(
                                assessment,
                                attendee
                        );

                row.add(
                        grade == null
                                ? null
                                : grade.getGrade()
                );
            }

            row.add(kelas.getAttendeeFinalGrade(attendee));

            for (Session session : sessions) {

                Attendance attendance =
                        attendee.getAttendanceBySession(session);

                if (attendance == null) {
                    row.add("Absent");
                } else {
                    row.add(
                            attendanceStatusLabel(
                                    attendance
                            )
                    );
                }
            }

            rows.add(row);
        }

        return rows;
    }

    private List<List<Object>> buildSessionReport(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
                "Session",
                "Topic",
                "Start",
                "End",
                "Duration",
                "Present"
        ));

        List<Attendee> attendees = sortedAttendees(kelas);

        List<Session> sessions = sortedSessions(kelas);

        for (int i = 0; i < sessions.size(); i++) {

            Session session = sessions.get(i);

            long presentCount = attendees.stream()
                    .filter(attendee -> {
                        Attendance attendance =
                                attendee.getAttendanceBySession(session);

                        return attendance != null
                                && attendance.getStatus()
                                == AttendanceRecord.PRESENT;
                    })
                    .count();

            rows.add(Arrays.asList(
                    "Session " + (i + 1),
                    session.getTopic(),
                    session.getStartTime(),
                    session.getEndTime(),
                    calculateDuration(session),
                    presentCount
            ));
        }

        return rows;
    }

    private String attendanceStatusLabel(
            Attendance attendance
    ) {

        AttendanceRecord status =
                attendance.getStatus();

        if (status == AttendanceRecord.PRESENT) {
            return "Present";
        }

        if (status == AttendanceRecord.SICK) {
            return "Sick Leave";
        }

        if (status == AttendanceRecord.LEAVE) {
            return "Formal Leave";
        }

        return "Absent";
    }

    private String calculateDuration(Session session) {

        if (session.getStartTime() == null
                || session.getEndTime() == null) {

            return "";
        }

        Duration duration =
                Duration.between(
                        session.getStartTime(),
                        session.getEndTime()
                );

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return "%dh %dm".formatted(hours, minutes);
        }

        if (minutes > 0) {
            return "%dm %ds".formatted(minutes, seconds);
        }

        return "%ds".formatted(seconds);
    }

    /*
     * ============================================================
     * BASIC XLSX DATA
     * ============================================================
     */

    private List<List<Object>> kelasRows(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList("Field", "Value"));
        rows.add(Arrays.asList("UUID", kelas.getUUID()));
        rows.add(Arrays.asList("Kelas ID", kelas.getKelasId()));
        rows.add(Arrays.asList(
                "Course ID",
                kelas.getCourseInfo().getCourseId()
        ));
        rows.add(Arrays.asList(
                "Course Name",
                kelas.getCourseInfo().getCourseName()
        ));
        rows.add(Arrays.asList(
                "Semester Year",
                kelas.getCohortSemester().getYear()
        ));
        rows.add(Arrays.asList(
                "Semester Type",
                kelas.getCohortSemester().getJenis_Semester().name()
        ));

        return rows;
    }

    private List<List<Object>> attendeeRows(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
                "UUID", "NIM", "Name", "Cohort"
        ));

        for (Attendee attendee : sortedAttendees(kelas)) {

            Student student = attendee.getStudent();

            rows.add(Arrays.asList(
                    attendee.getUUID(),
                    student.getNIM(),
                    student.getNama(),
                    student.getCohort()
            ));
        }

        return rows;
    }

    private List<List<Object>> assessmentRows(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
                "UUID", "Name", "Percentage"
        ));

        for (Assessment assessment : sortedAssessments(kelas)) {

            rows.add(Arrays.asList(
                    assessment.getUUID(),
                    assessment.getName(),
                    assessment.getPercentage()
            ));
        }

        return rows;
    }

    private List<List<Object>> sessionRows(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
                "UUID",
                "Created At",
                "Topic",
                "Notes",
                "Start",
                "End"
        ));

        for (Session session : sortedSessions(kelas)) {

            rows.add(Arrays.asList(
                    session.getUUID(),
                    session.getCreatedAt(),
                    session.getTopic(),
                    session.getNotes(),
                    session.getStartTime(),
                    session.getEndTime()
            ));
        }

        return rows;
    }

    private List<List<Object>> attendanceRows(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
                "Attendee UUID",
                "NIM",
                "Student Name",
                "Session UUID",
                "Session Topic",
                "Status",
                "Timestamp",
                "Updated At"
        ));

        for (Attendee attendee : sortedAttendees(kelas)) {

            Student student = attendee.getStudent();

            for (Attendance attendance :
                    sortedAttendances(attendee)) {

                Session session = attendance.getSession();

                rows.add(Arrays.asList(
                        attendee.getUUID(),
                        student.getNIM(),
                        student.getNama(),
                        session.getUUID(),
                        session.getTopic(),
                        attendance.getStatus(),
                        attendance.getRecordedAt(),
                        attendance.getUpdatedAt()
                ));
            }
        }

        return rows;
    }

    private List<List<Object>> gradeRows(Kelas kelas) {

        List<List<Object>> rows = new ArrayList<>();

        rows.add(Arrays.asList(
                "Attendee UUID",
                "NIM",
                "Student Name",
                "Assessment UUID",
                "Assessment",
                "Percentage",
                "Grade",
                "Weighted Grade"
        ));

        for (Assessment assessment : sortedAssessments(kelas)) {

            for (Grade grade : kelas.getGrades(assessment)) {

                Student student =
                        grade.getAttendee()
                                .getStudent();

                rows.add(Arrays.asList(
                        grade.getAttendee().getUUID(),
                        student.getNIM(),
                        student.getNama(),
                        assessment.getUUID(),
                        assessment.getName(),
                        assessment.getPercentage(),
                        grade.getGrade(),
                        grade.calculateGrade()
                ));
            }
        }

        return rows;
    }

    /*
     * ============================================================
     * SORTING / MODEL HELPERS
     * ============================================================
     */

    private List<Attendee> sortedAttendees(Kelas kelas) {

        return kelas.getAttendees()
                .stream()
                .sorted(
                        Comparator.comparing(
                                a -> a.getStudent().getNIM()
                        )
                )
                .toList();
    }

    private List<Assessment> sortedAssessments(Kelas kelas) {

        return kelas.getAssessments()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Assessment::getName
                        )
                )
                .toList();
    }

    private List<Session> sortedSessions(Kelas kelas) {

        return kelas.getSessions()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Session::getCreatedAt,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .toList();
    }

    private List<Attendance> sortedAttendances(
            Attendee attendee
    ) {

        return attendee.getAttendances()
                .stream()
                .sorted(
                        Comparator.comparing(
                                a -> a.getSession().getCreatedAt(),
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .toList();
    }

    /*
     * ============================================================
     * XLSX WRITER
     * ============================================================
     */

    private void writeEntry(
            ZipOutputStream zip,
            String name,
            String content
    ) throws IOException {

        zip.putNextEntry(new ZipEntry(name));

        zip.write(
                content.getBytes(StandardCharsets.UTF_8)
        );

        zip.closeEntry();
    }

    private String contentTypes(int sheetCount) {

        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels"
                    ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml"
                    ContentType="application/xml"/>
                  <Override PartName="/xl/workbook.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                  <Override PartName="/xl/styles.xml"
                    ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                """);

        for (int i = 1; i <= sheetCount; i++) {

            xml.append("""
                      <Override PartName="/xl/worksheets/sheet%s.xml"
                        ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    """.formatted(i));
        }

        xml.append("</Types>");

        return xml.toString();
    }

    private String rootRelationships() {

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Relationships
                  xmlns="http://schemas.openxmlformats.org/package/2006/relationships">

                  <Relationship
                    Id="rId1"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
                    Target="xl/workbook.xml"/>

                </Relationships>
                """;
    }

    private String workbook(String... sheetNames) {

        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <workbook
                  xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                  <sheets>
                """);

        for (int i = 0; i < sheetNames.length; i++) {

            xml.append("""
                      <sheet
                        name="%s"
                        sheetId="%d"
                        r:id="rId%d"/>
                    """.formatted(
                    xmlEscape(sheetNames[i]),
                    i + 1,
                    i + 1
            ));
        }

        xml.append("""
                  </sheets>
                </workbook>
                """);

        return xml.toString();
    }

    private String workbookRelationships(int sheetCount) {

        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Relationships
                  xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                """);

        for (int i = 1; i <= sheetCount; i++) {

            xml.append("""
                      <Relationship
                        Id="rId%d"
                        Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
                        Target="worksheets/sheet%d.xml"/>
                    """.formatted(i, i));
        }

        xml.append("""
                  <Relationship
                    Id="rId%d"
                    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
                    Target="styles.xml"/>

                </Relationships>
                """.formatted(sheetCount + 1));

        return xml.toString();
    }

    private String styles() {

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <styleSheet
                  xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">

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
                    <xf numFmtId="0"
                        fontId="0"
                        fillId="0"
                        borderId="0"/>
                  </cellStyleXfs>

                  <cellXfs count="1">
                    <xf numFmtId="0"
                        fontId="0"
                        fillId="0"
                        borderId="0"/>
                  </cellXfs>

                </styleSheet>
                """;
    }

    private String sheet(List<List<Object>> rows) {

        StringBuilder xml = new StringBuilder("""
                <?xml version="1.0" encoding="UTF-8"?>
                <worksheet
                  xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                  <sheetData>
                """);

        for (int rowIndex = 0;
             rowIndex < rows.size();
             rowIndex++) {

            List<Object> row = rows.get(rowIndex);

            int excelRow = rowIndex + 1;

            xml.append("<row r=\"")
                    .append(excelRow)
                    .append("\">");

            for (int colIndex = 0;
                 colIndex < row.size();
                 colIndex++) {

                Object value = row.get(colIndex);

                if (value == null) {
                    continue;
                }

                String reference =
                        columnName(colIndex + 1)
                                + excelRow;

                xml.append(
                                "<c r=\"")
                        .append(reference)
                        .append("\" t=\"inlineStr\">")
                        .append("<is><t>")
                        .append(xmlEscape(value.toString()))
                        .append("</t></is>")
                        .append("</c>");
            }

            xml.append("</row>");
        }

        xml.append("""
                  </sheetData>
                </worksheet>
                """);

        return xml.toString();
    }

    private String columnName(int column) {

        StringBuilder result = new StringBuilder();

        while (column > 0) {

            int remainder = (column - 1) % 26;

            result.insert(
                    0,
                    (char) ('A' + remainder)
            );

            column = (column - 1) / 26;
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

    /*
     * ============================================================
     * CSV HELPERS
     * ============================================================
     */

    private void writeCSVRow(
            BufferedWriter writer,
            Object... values
    ) throws IOException {

        for (int i = 0; i < values.length; i++) {

            if (i > 0) {
                writer.write(",");
            }

            writer.write(
                    csvEscape(
                            values[i] == null
                                    ? ""
                                    : values[i].toString()
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

        String escaped =
                value.replace("\"", "\"\"");

        return needsQuotes
                ? "\"" + escaped + "\""
                : escaped;
    }
}
