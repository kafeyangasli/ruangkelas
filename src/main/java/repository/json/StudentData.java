package repository.json;

import models.Student;

public class StudentData {
    private String nim;
    private String nama;
    private int cohort;

    public StudentData() { }

    public StudentData(Student student) {
        this.nim = student.getNIM();
        this.nama = student.getNama();
        this.cohort = student.getCohort();
    }

    public Student toDomain() {
        return new Student(nim, nama, cohort);
    }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public int getCohort() { return cohort; }
    public void setCohort(int cohort) { this.cohort = cohort; }
}
