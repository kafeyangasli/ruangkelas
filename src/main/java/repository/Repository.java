package repository;

import exceptions.PersistenceException;
import models.Course;
import models.Kelas;
import models.Student;

import java.util.Set;
import java.util.HashSet;
import java.nio.file.Path;

public class Repository<T> {
    private final Set<T> data = new HashSet<>();

    public Repository() {

    }

    public void add(T newData) {
        data.add(newData);
    }

    public void remove(T oldData) {
        data.remove(oldData);
    }

    public boolean contains(T check) {
        return data.contains(check);
    }

    public void setAll(Set<T> data) {
        this.data.clear();
        this.data.addAll(data);
    }

    public Set<T> getAll() {
        return Set.copyOf(data);
    }
}
