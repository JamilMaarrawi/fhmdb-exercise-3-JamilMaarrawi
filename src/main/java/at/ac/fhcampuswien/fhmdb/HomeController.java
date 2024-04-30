package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.database.DatabaseManager;
import at.ac.fhcampuswien.fhmdb.database.MovieRepository;
import at.ac.fhcampuswien.fhmdb.database.WatchlistRepository;
import at.ac.fhcampuswien.fhmdb.exceptions.DatabaseException;
import at.ac.fhcampuswien.fhmdb.exceptions.MovieAPIException;
import at.ac.fhcampuswien.fhmdb.models.*;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import com.jfoenix.controls.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HomeController implements Initializable {
    @FXML
    public JFXButton searchBtn;

    @FXML
    public TextField searchField;

    @FXML
    public JFXListView movieListView;

    @FXML
    public JFXComboBox genreComboBox;

    @FXML
    public JFXComboBox releaseYearComboBox;

    @FXML
    public JFXComboBox ratingFromComboBox;

    @FXML
    public JFXButton sortBtn;

    @FXML
    public JFXButton unFilterBtn;

    @FXML
    public JFXButton windowBtn;

    @FXML
    public VBox vBox;

    public List<Movie> allMovies;

    protected ObservableList<Movie> observableMovies = FXCollections.observableArrayList();

    protected SortedState sortedState;

    public WindowState windowState;

    private MovieRepository movieRepository;
    private WatchlistRepository watchlistRepository;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        movieRepository = new MovieRepository();
        watchlistRepository = new WatchlistRepository();
        try {
            DatabaseManager.createConnectSource();
            initializeState();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initializeLayout();
    }

    public void initializeState() throws SQLException {
        try {
            //throw new MovieAPIException(new RuntimeException());
            List<Movie> movies = MovieAPI.getAllMovies();
            movieRepository.removeAll();
            movieRepository.addAllMovies(movies);
        } catch (MovieAPIException e) {
            System.out.println(e.getMessage());
            showMovieAPIError(e);
        } finally {
            try {
                //throw new DatabaseException(new RuntimeException());
                List<Movie> result = MovieEntity.toMovies(movieRepository.getAllMovies());
                setMovies(result);
                setMovieList(result);
            } catch (DatabaseException e) {
                System.out.println(e.getMessage());
                showDatabaseError(e);
            }
        }
        /*if(MovieAPI.isRequestSuccessful()) {
            movieRepository.removeAll();
            movieRepository.addAllMovies(MovieAPI.getAllMovies());
        }
        List<Movie> result =MovieEntity.toMovies(movieRepository.getAllMovies());
        setMovies(result);
        setMovieList(result);*/

        sortedState = SortedState.NONE;
        windowState = WindowState.HOME;

        // test stream methods
        try {
            System.out.println("getMostPopularActor");
            System.out.println(getMostPopularActor(allMovies));

            System.out.println("getLongestMovieTitle");
            System.out.println(getLongestMovieTitle(allMovies));

            System.out.println("count movies from Zemeckis");
            System.out.println(countMoviesFrom(allMovies, "Robert Zemeckis"));

            System.out.println("count movies from Steven Spielberg");
            System.out.println(countMoviesFrom(allMovies, "Steven Spielberg"));

            System.out.println("getMoviewsBetweenYears");
            List<Movie> between = getMoviesBetweenYears(allMovies, 1994, 2000);
            System.out.println(between.size());
            System.out.println(between.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        } catch (DatabaseException e) {
            System.out.println(e.getMessage());
        }
    }

    public void initializeLayout() {
        movieListView.setItems(observableMovies);   // set the items of the listview to the observable list
        movieListView.setCellFactory(movieListView -> new MovieCell(onAddToWatchlistClicked,this)); // apply custom cells to the listview

        // genre combobox
        Object[] genres = Genre.values();   // get all genres
        genreComboBox.getItems().add("No filter");  // add "no filter" to the combobox
        genreComboBox.getItems().addAll(genres);    // add all genres to the combobox
        genreComboBox.setPromptText("Filter by Genre");

        // year combobox
        releaseYearComboBox.getItems().add("No filter");  // add "no filter" to the combobox
        // fill array with numbers from 1900 to 2023
        Integer[] years = new Integer[124];
        for (int i = 0; i < years.length; i++) {
            years[i] = 1900 + i;
        }
        releaseYearComboBox.getItems().addAll(years);    // add all years to the combobox
        releaseYearComboBox.setPromptText("Filter by Release Year");

        // rating combobox
        ratingFromComboBox.getItems().add("No filter");  // add "no filter" to the combobox
        // fill array with numbers from 0 to 10
        Integer[] ratings = new Integer[11];
        for (int i = 0; i < ratings.length; i++) {
            ratings[i] = i;
        }
        ratingFromComboBox.getItems().addAll(ratings);    // add all ratings to the combobox
        ratingFromComboBox.setPromptText("Filter by Rating");
    }

    private final ClickEventHandler onAddToWatchlistClicked = (clickedItem) -> {
        if(windowState == WindowState.HOME) watchlistRepository.addToWatchlist(new WatchlistMovieEntity((Movie) clickedItem));
        else {
            watchlistRepository.removeFromWatchlist(clickedItem.toString());
            List<Movie> result =WatchlistMovieEntity.watchlistToMovies(watchlistRepository.getWatchlist());
            setMovies(result);
            setMovieList(result);
        }
    };

    public void setMovies(List<Movie> movies) {
        allMovies = movies;
    }

    public void setMovieList(List<Movie> movies) {
        observableMovies.clear();
        observableMovies.addAll(movies);
    }

    public void sortMovies(){
        if (sortedState == SortedState.NONE || sortedState == SortedState.DESCENDING) {
            sortMovies(SortedState.ASCENDING);
        } else if (sortedState == SortedState.ASCENDING) {
            sortMovies(SortedState.DESCENDING);
        }
    }
    // sort movies based on sortedState
    // by default sorted state is NONE
    // afterward it switches between ascending and descending
    public void sortMovies(SortedState sortDirection) {
        if (sortDirection == SortedState.ASCENDING) {
            observableMovies.sort(Comparator.comparing(Movie::getTitle));
            sortedState = SortedState.ASCENDING;
        } else {
            observableMovies.sort(Comparator.comparing(Movie::getTitle).reversed());
            sortedState = SortedState.DESCENDING;
        }
    }

    public List<Movie> filterByQuery(List<Movie> movies, String query){
        if(query == null || query.isEmpty()) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream().filter(movie ->
                movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                movie.getDescription().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    public List<Movie> filterByGenre(List<Movie> movies, Genre genre){
        if(genre == null) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream().filter(movie -> movie.getGenres().contains(genre)).toList();
    }

    public List<Movie> filterByReleaseYear(List<Movie> movies, String releaseYear){
        if(releaseYear == null || Integer.valueOf(releaseYear) > 2023 || Integer.valueOf(releaseYear) < 1900) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream().filter(movie -> movie.getReleaseYear() == Integer.valueOf(releaseYear)).toList();
    }

    public List<Movie> filterByRating(List<Movie> movies, String ratingFrom){
        if(ratingFrom == null || Integer.valueOf(ratingFrom) > 10 || Integer.valueOf(ratingFrom) < 0) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream().filter(movie -> movie.getRating() >= Integer.valueOf(ratingFrom)).toList();
    }

    public List<Movie> applyAllFilters(String searchQuery, Object genre, Object releaseYear, Object ratingFrom) {
        List<Movie> filteredMovies = allMovies;

        if (!searchQuery.isEmpty()) {
            filteredMovies = filterByQuery(filteredMovies, searchQuery);
        }

        if (genre != null && !genre.toString().equals("No filter")) {
            filteredMovies = filterByGenre(filteredMovies, Genre.valueOf(genre.toString()));
        }

        if (releaseYear != null ) {
            filteredMovies = filterByReleaseYear(filteredMovies,releaseYear.toString());
        }

        if (ratingFrom != null) {
            filteredMovies = filterByRating(filteredMovies,ratingFrom.toString());
        }


        return filteredMovies;
    }

    public void searchBtnClicked(ActionEvent actionEvent) {
        String searchQuery = searchField.getText().trim().toLowerCase();
        String releaseYear = validateComboboxValue(releaseYearComboBox.getSelectionModel().getSelectedItem());
        String ratingFrom = validateComboboxValue(ratingFromComboBox.getSelectionModel().getSelectedItem());
        String genreValue = validateComboboxValue(genreComboBox.getSelectionModel().getSelectedItem());

        Genre genre = null;
        if(genreValue != null) {
            genre = Genre.valueOf(genreValue);
        }

        //List<Movie> movies = getMovies(searchQuery, genre, releaseYear, ratingFrom);
        List<Movie> movies = applyAllFilters(searchQuery, genre, releaseYear, ratingFrom);
        setMovies(movies);
        setMovieList(movies);

        sortMovies(sortedState);
    }

    public String validateComboboxValue(Object value) {
        if(value != null && !value.toString().equals("No filter")) {
            return value.toString();
        }
        return null;
    }

    public List<Movie> getMovies(String searchQuery, Genre genre, String releaseYear, String ratingFrom) {
        return MovieAPI.getAllMovies(searchQuery, genre, releaseYear, ratingFrom);
    }

    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    public void windowBtnClicked(ActionEvent actionEvent) throws SQLException {
        if(windowState == WindowState.HOME){
            windowState = WindowState.WATCHLIST;
            windowBtn.setText("Go back Home");
            List<Movie> result =WatchlistMovieEntity.watchlistToMovies(watchlistRepository.getWatchlist());
            setMovies(result);
            setMovieList(result);
            movieListView.setItems(observableMovies);
            movieListView.setCellFactory(movieListView -> new MovieCell(onAddToWatchlistClicked,this));
        } else {
            windowState = WindowState.HOME;
            windowBtn.setText("Go to Watchlist");
            try {
                List<Movie> result =MovieEntity.toMovies(movieRepository.getAllMovies());
                setMovies(result);
                setMovieList(result);
            } catch (DatabaseException e) {
                System.out.println(e.getMessage());
                showDatabaseError(e);
            }
            movieListView.setItems(observableMovies);
            movieListView.setCellFactory(movieListView -> new MovieCell(onAddToWatchlistClicked,this));
        }
    }

    public void unFilterBtnClicked(ActionEvent actionEvent) throws SQLException {
        searchField.setText(null);
        genreComboBox.setValue(null);
        releaseYearComboBox.setValue(null);
        ratingFromComboBox.setValue(null);
        List<Movie> result;
        if(windowState == WindowState.HOME) {
            try {
                result = MovieEntity.toMovies(movieRepository.getAllMovies());
                setMovies(result);
                setMovieList(result);
            } catch (DatabaseException e) {
                System.out.println(e.getMessage());
                showDatabaseError(e);
            }

        } else {
            result = WatchlistMovieEntity.watchlistToMovies(watchlistRepository.getWatchlist());
            setMovies(result);
            setMovieList(result);
        }
    }

    // count which actor is in the most movies
    public String getMostPopularActor(List<Movie> movies) throws DatabaseException {
        if (movies == null || movies.isEmpty()) throw new DatabaseException();
        String actor = movies.stream()
                .flatMap(movie -> movie.getMainCast().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        return actor;
    }

    public int getLongestMovieTitle(List<Movie> movies) throws DatabaseException {
        if (movies == null || movies.isEmpty()) throw new DatabaseException();
        return movies.stream()
                .mapToInt(movie -> movie.getTitle().length())
                .max()
                .orElse(0);
    }

    public long countMoviesFrom(List<Movie> movies, String director) throws DatabaseException {
        if (movies == null || movies.isEmpty()) throw new DatabaseException();
        return movies.stream()
                .filter(movie -> movie.getDirectors().contains(director))
                .count();
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endYear) throws DatabaseException {
        if (movies == null || movies.isEmpty()) throw new DatabaseException();
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() >= startYear && movie.getReleaseYear() <= endYear)
                .collect(Collectors.toList());
    }

    private void showMovieAPIError(MovieAPIException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        alert.setTitle("Connection Error");
        alert.setHeaderText(null);
        alert.setContentText("Ooops, could not load movies!!!" + System.lineSeparator()
                + System.lineSeparator() + "Loading Movies from last Time . . ." + System.lineSeparator()
                + System.lineSeparator() + "Error: " + e.getCause());
        alert.show();
    }

    private void showDatabaseError(DatabaseException e) {
        Text text = new Text("Could not reach Database!!!" + System.lineSeparator() +
                System.lineSeparator() + "Please try again later !!!" + System.lineSeparator() +
                System.lineSeparator() + "Error: " + e.getCause());
        text.getStyleClass().add("ErrorText");
        vBox.getChildren().add(vBox.getChildren().size()-1,text );
    }
}