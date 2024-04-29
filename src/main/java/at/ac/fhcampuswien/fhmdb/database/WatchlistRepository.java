package at.ac.fhcampuswien.fhmdb.database;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.models.MovieEntity;
import at.ac.fhcampuswien.fhmdb.models.WatchlistMovieEntity;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

public class WatchlistRepository {
    Dao<WatchlistMovieEntity, Long> watchlistDao;

    public WatchlistRepository() {
        this.watchlistDao = DatabaseManager.getDatabaseManager().getWatchlistDao();
    }

    public List<WatchlistMovieEntity> getWatchlist() throws SQLException {
        return watchlistDao.queryForAll();
    }

    public int addToWatchlist(WatchlistMovieEntity movie) throws SQLException {
        if(this.isInWatchlist(movie.getId())) return 0;
        return watchlistDao.create(movie);
    }

    public int removeFromWatchlist(String id) throws SQLException {
        return watchlistDao.delete(getMovieById(id));
    }

    public WatchlistMovieEntity getMovieById(String id) throws SQLException {
        List<WatchlistMovieEntity> list = getWatchlist();
        for (WatchlistMovieEntity m : list) {
            if(m.getId().equals(id)) return m;
        }
        return null;
    }

    public boolean isInWatchlist (String id) throws SQLException {
        for(WatchlistMovieEntity m : getWatchlist()) if(m.getId() == id) return true;
        return false;
    }
}
