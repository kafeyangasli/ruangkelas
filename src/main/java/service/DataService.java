package service;

import java.nio.file.Path;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import events.DataUpdateListener;

import exceptions.PersistenceException;
import repository.JsonPersistence;
import repository.Repository;

public abstract class DataService<T> {
    protected final Repository<T> repository;
    protected final JsonPersistence jsonPersistence;
    private final List<DataUpdateListener> listeners = new ArrayList<>();

    protected DataService(JsonPersistence jsonPersistence) {
        this.repository = new Repository<>();
        this.jsonPersistence = jsonPersistence;
    }

    public abstract void saveData() throws PersistenceException;

    public Set<T> getAll() {
        return repository.getAll();
    }

    public void addChangeListener(DataUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }

    public void notifyChange() {
        for (DataUpdateListener listener : List.copyOf(listeners)) {
            listener.onUpdate();
        }
    }


}
