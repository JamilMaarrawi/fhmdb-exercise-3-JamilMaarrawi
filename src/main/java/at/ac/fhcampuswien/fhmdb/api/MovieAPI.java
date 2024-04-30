package at.ac.fhcampuswien.fhmdb.api;

import at.ac.fhcampuswien.fhmdb.exceptions.MovieAPIException;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import okhttp3.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MovieAPI {
    public static final String DELIMITER = "&";
    private static final String URL = "http://prog2.fh-campuswien.ac.at/movies"; // https if certificates work
    private static final OkHttpClient client = new OkHttpClient();

    private static String buildUrl(UUID id) {
        StringBuilder url = new StringBuilder(URL);
        if (id != null) {
            url.append("/").append(id);
        }
        return url.toString();
    }

    private static String buildUrl(String query, Genre genre, String releaseYear, String ratingFrom) {
        StringBuilder url = new StringBuilder(URL);

        if ( (query != null && !query.isEmpty()) ||
                genre != null || releaseYear != null || ratingFrom != null) {

            url.append("?");

            // check all parameters and add them to the url
            if (query != null && !query.isEmpty()) {
                url.append("query=").append(query).append(DELIMITER);
            }
            if (genre != null) {
                url.append("genre=").append(genre).append(DELIMITER);
            }
            if (releaseYear != null) {
                url.append("releaseYear=").append(releaseYear).append(DELIMITER);
            }
            if (ratingFrom != null) {
                url.append("ratingFrom=").append(ratingFrom).append(DELIMITER);
            }
        }

        return url.toString();
    }

    public static List<Movie> getAllMovies() throws MovieAPIException {
        List<Movie> movies = getAllMovies(null,null,null,null);
        if (movies.isEmpty()) throw new MovieAPIException(new RuntimeException());
        return movies;
    }

    public static List<Movie> getAllMovies(String query, Genre genre, String releaseYear, String ratingFrom){
        String url = buildUrl(query, genre, releaseYear, ratingFrom);
        Request request = new Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "http.agent")  // needed for the server to accept the request
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            Gson gson = new Gson();
            Movie[] movies = gson.fromJson(responseBody, Movie[].class);

            return Arrays.asList(movies);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new ArrayList<>();
    }

    public static Movie requestMovieById(UUID id){
        String url = buildUrl(id);
        Request request = new Request.Builder()
                .url(url)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "http.agent")  // needed for the server to accept the request
                .build();

        try (Response response = client.newCall(request).execute()) {
            Gson gson = new Gson();
            return gson.fromJson(response.body().string(), Movie.class);
        } catch (IOException e) {
            System.err.println(MovieAPI.class + ": http status not ok");
        }

        return null;
    }

    public static boolean isRequestSuccessful() {
        Request request = new Request.Builder()
                .url(URL)
                .removeHeader("User-Agent")
                .addHeader("User-Agent", "http.agent")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (IOException e) {
            System.err.println(MovieAPI.class + ": http status not ok");
        }
        //throw new MovieAPIException();
        return false;
    }
}
