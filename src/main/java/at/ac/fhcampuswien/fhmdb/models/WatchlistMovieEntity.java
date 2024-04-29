package at.ac.fhcampuswien.fhmdb.models;

import at.ac.fhcampuswien.fhmdb.database.WatchlistRepository;

import java.util.ArrayList;
import java.util.List;

public class WatchlistMovieEntity extends MovieEntity{
    String id;
    long dbId;

    public WatchlistMovieEntity(){

    }
    public WatchlistMovieEntity(Movie m){
        super(m);
    }

    public static List<Movie> watchlistToMovies(List<WatchlistMovieEntity> watchlistMovieEntities){
        List<Movie> movies = new ArrayList<>();
        for(WatchlistMovieEntity m : watchlistMovieEntities) movies.add(new Movie(m));
        return movies;
    }

}
