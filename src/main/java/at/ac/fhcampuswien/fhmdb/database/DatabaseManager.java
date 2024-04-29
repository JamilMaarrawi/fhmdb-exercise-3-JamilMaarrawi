package at.ac.fhcampuswien.fhmdb.database;

import at.ac.fhcampuswien.fhmdb.models.MovieEntity;
import at.ac.fhcampuswien.fhmdb.models.WatchlistMovieEntity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseManager {
    public static final String DB_URL = "jdbc:h2:./db/moviesdb";
    public static final String user = "user";
    public static final String password = "password";
    private static ConnectionSource connectionSource;
    private Dao<MovieEntity, Long> movieDao;
    private Dao<WatchlistMovieEntity, Long> watchlistDao;

    private static DatabaseManager instance;
    private DatabaseManager(){
        try {
            createConnectSource();
            movieDao = DaoManager.createDao(connectionSource, MovieEntity.class);
            watchlistDao = DaoManager.createDao(connectionSource, WatchlistMovieEntity.class);
            createTables();
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public static DatabaseManager getDatabaseManager(){
        if(instance==null) instance = new DatabaseManager();
        return instance;
    }

    public static void createConnectSource() throws SQLException {
        connectionSource = new JdbcConnectionSource(DB_URL,user,password);
    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }

    private static void createTables() throws SQLException {
        TableUtils.createTableIfNotExists(connectionSource, MovieEntity.class);
        TableUtils.createTableIfNotExists(connectionSource, WatchlistMovieEntity.class);
    }

    public Dao<MovieEntity, Long> getMovieDao() {
        return this.movieDao;
    }

    public Dao<WatchlistMovieEntity, Long> getWatchlistDao() {
        return this.watchlistDao;
    }
}
