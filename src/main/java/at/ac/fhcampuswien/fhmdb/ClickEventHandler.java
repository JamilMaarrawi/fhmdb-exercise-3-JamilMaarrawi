package at.ac.fhcampuswien.fhmdb;

import java.sql.SQLException;

@FunctionalInterface
public interface ClickEventHandler<T> {
    void onClick(T t) throws SQLException;
}
