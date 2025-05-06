package service;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    boolean add(T t) throws SQLException;
    boolean update(T t) throws SQLException;
    void delete(int id) throws SQLException;
    List<T> getAll() throws SQLException;
    T getById(int id) throws SQLException;
    boolean validate(T t) throws IllegalArgumentException;

    List<T> display() throws SQLException;
}