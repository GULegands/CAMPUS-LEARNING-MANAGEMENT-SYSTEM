package lms.dao;

import lms.exception.LMSException;
import java.util.List;

/** Generic CRUD interface implemented by the DAO classes (Interfaces + Generics). */
public interface Dao<T> {
    void add(T item) throws LMSException;
    void update(T item) throws LMSException;
    void delete(int id) throws LMSException;
    T findById(int id) throws LMSException;
    List<T> findAll() throws LMSException;
}
