package service;

import exceptions.ExportException;

import java.nio.file.Path;

public interface ExportService<T> {
    void exportCSV(T data, Path path) throws ExportException;
    void exportXLSX(T data, Path path) throws ExportException;
}
