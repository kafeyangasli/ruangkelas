package models;

import java.time.LocalDate;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class Semester {
    public enum Jenis_Semester {
        GENAP,
        GANJIL;
    }

    private int year;
    private Jenis_Semester jenis;

    public Semester(int year, Jenis_Semester jenis) {
        this.year = year;
        this.jenis = jenis;
    }
    
    @Override
    public String toString() {
        return ("%d/%d %s").formatted(
                this.jenis == Jenis_Semester.GENAP ? this.year - 1 : this.year,
                this.jenis == Jenis_Semester.GANJIL ? this.year + 1 : this.year,
                StringUtils.capitalize(this.jenis.toString().toLowerCase()));
    }

    public int getYear() {
        return this.year;
    }

    public Jenis_Semester getJenis_Semester() {
        return this.jenis;
    }

    public static Semester semesterNow() {
        LocalDate now = LocalDate.now();

        return new Semester(
            now.getYear(), now.getMonthValue() > 6 ? Jenis_Semester.GANJIL : Jenis_Semester.GENAP);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Semester other)) return false;

        return year == other.year
                && jenis == other.jenis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, jenis);
    }
}
