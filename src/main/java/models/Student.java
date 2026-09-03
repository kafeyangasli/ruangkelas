package models;

import java.util.Objects;

public class Student {
    private final String nim;
    private final String nama;
    private final int cohort;

    public Student(String nim, String nama, int cohort) {
        this.nim = nim;
        this.nama = nama;
        this.cohort = cohort;
    };

    public String getNIM() {
        return this.nim;
    }

    public String getNama() {
        return this.nama;
    }

    public int getCohort() {
        return this.cohort;
    }
    
    @Override
    public String toString() {
        return "%s (%s)".formatted(this.nama, this.nim);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student other)) return false;

        return this.nim.equals(other.nim);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.nim);
        return hash;
    }
}