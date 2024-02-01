package cs1302.gallery;

import java.net.http.HttpClient;
import javafx.scene.image.Image;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.TilePane;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.IOException;
import java.lang.InterruptedException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.animation.Timeline;
import javax.imageio.ImageIO;
import java.io.File;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ProgressBar;
import javafx.geometry.Insets;
import javafx.scene.control.OverrunStyle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private Stage stage;
    private Scene scene;
    private VBox root;
    private HBox searchBar;
    private ImageDisplayer images;
    private Label urlBar;
    private Button playButton;
    private Label searchLabel;
    private TextField searchField;
    private Button getImages;
    private ComboBox<String> typeMenu;
    private Timeline animation;
    private int numImages;
    private boolean animating = false;
    private Alert error;
    private boolean successfulOnce = false;
    private ProgressBar progressBar;
    private Label apiNotice;
    private HBox loadingBar;

    /** HTTP Client. */
    public static final HttpClient CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(Redirect.NORMAL)
        .build();

    /**
     * Constructs a {@code GalleryApp} object}.
     */
    public GalleryApp() {
        // Instantiating various components
        this.stage = null;
        this.scene = null;
        this.root = new VBox();
        this.images = new ImageDisplayer();
        this.searchBar = new HBox(8);
        searchBar.setAlignment(Pos.CENTER);
        this.playButton = new Button("Play");
        playButton.setDisable(true);
        this.searchLabel = new Label("Search:");
        this.searchField = new TextField();
        this.searchField.setPrefWidth(2);
        this.getImages = new Button("Get Images");
        getImages.setOnAction(e -> runNow(() -> getImages()));
        this.urlBar = new Label("");
        this.typeMenu = new ComboBox<String>();
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(235);
        apiNotice = new Label("Images provided by iTunes Search API.");
        searchField.setText("david lynch");
        loadingBar = new HBox(4);
        loadingBar.setAlignment(Pos.CENTER);
    } // GalleryApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Initializing various components
        typeMenu.getItems().addAll("music", "movie", "podcast", "musicVideo", "audiobook",
            "shortFilm", "tvShow", "software", "ebook", "all");
        typeMenu.setValue("music");
        urlBar.setText("Type in a term, select a media type, then hit the button.");
        urlBar.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        searchBar.getChildren().addAll(playButton, searchLabel, searchField, typeMenu, getImages);
        searchBar.setHgrow(searchField, Priority.ALWAYS);
        searchBar.setFillHeight(true);
        searchBar.setPadding(new Insets(4));
        loadingBar.getChildren().addAll(progressBar, apiNotice);
        loadingBar.setHgrow(progressBar, Priority.ALWAYS);
        loadingBar.setFillHeight(true);
        root.getChildren().addAll(searchBar, urlBar, images, loadingBar);
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this.root);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        stage.setResizable(false);
        stage.setMaxHeight(720);
        stage.setMaxWidth(1280);
        this.stage.show();
    } // start

    /**
     * Runs the specified task on a new thread.
     *
     * @param task the task to be run
     */
    private static void runNow(Runnable task) {
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    } // runNow

    /**
     * Method called whenever the "Get Images" button is pressed.
     */
    private void getImages() {
        if (animating) {
            play();
        }

        // Updates the UI appropriately for this task
        Platform.runLater(() -> getImages.setDisable(true));
        Platform.runLater(() -> playButton.setDisable(true));
        Platform.runLater(() -> urlBar.setText("Getting images..."));
        Platform.runLater(() -> progressBar.setProgress(0));

        // Constructs the URl
        String searchTerm = URLEncoder.encode(searchField.getText(), StandardCharsets.UTF_8);
        String limit = URLEncoder.encode("200", StandardCharsets.UTF_8);
        String media = URLEncoder.encode(typeMenu.getValue(), StandardCharsets.UTF_8);
        String query = String.format("?term=%s&media=%s&limit=%s", searchTerm, media, limit);
        String url = "https://itunes.apple.com/search" + query;
        Platform.runLater(() -> urlBar.setText(url));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
            .build();
        String json;
        try {
            HttpResponse<String> response = CLIENT.send(request, BodyHandlers.ofString());
            GalleryApp.<String>ensureGoodResponse(response);
            json = response.body();
            animation = parseJson(json);
            playButton.setOnAction(e -> play());
            Platform.runLater(() -> getImages.setDisable(false));
            if (!successfulOnce) {
                successfulOnce = true;
            }
        } catch (Exception e) {
            Platform.runLater(() -> createError(url, e));
            Platform.runLater(() -> getImages.setDisable(false));
            Platform.runLater(() -> urlBar.setText("Last attempt to get images failed..."));
            Platform.runLater(() -> progressBar.setProgress(1));
        } // try

        if (successfulOnce) {
            Platform.runLater(() -> playButton.setDisable(false));
        }
    } // getImages

    /**
     * Creates a new error dialog containing the specified URL and exception.
     *
     * @param url the url the error ocurred on
     * @param e the exception thrown
     */
    private void createError(String url, Exception e) {
        error = new Alert(AlertType.ERROR, "URI: " + url + "\n\n" + e.getMessage());
        error.showAndWait();
    } // createError

    /**
     * Parses the given JSON into a Timeline capable of random replacement.
     *
     * @param json the json to be parsed
     * @return returns a timeline which will properly execute random replacement
     */
    private Timeline parseJson(String json) throws IOException {
        ItunesResponse response = GSON.fromJson(json, cs1302.gallery.ItunesResponse.class);
        if (response == null) {
            throw new IllegalArgumentException("No results found");
        } // if
        ItunesResult[] responses = response.getResults();

        if (responses.length < 21) {
            throw new IllegalArgumentException(responses.length +
            " distinct results found, but 21 or more are needed");
        }

        // download all images, store local URLs in string array
        for (int i = 0; i < responses.length; i++) {
            Image image = new Image(responses[i].getUrl());
            File file = new File("resources/downloads/thumb" + i + ".jpg");
            file.getParentFile().mkdirs();
            file.createNewFile();
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", file);
            Platform.runLater(updateProgress(i, responses.length));
        } // for

        Platform.runLater(() -> progressBar.setProgress(1));

        // update imagedisplayer with first 20 URLs
        for (int i = 0; i < 20; i++) {
            images.replaceImage(i, "file:resources/downloads/thumb" + i + ".jpg");
        } // for

        // create and return a timeline facilitating the random replacement
        numImages = responses.length;
        EventHandler<ActionEvent> handler = event -> images.replaceRandomImage(numImages);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        return timeline;
    } // parseJson

    /**
     * Updates the progress bar.
     *
     * @param numDownloaded the number of images currently downloaded
     * @param numTotal the number of total images
     * @return a Runnable that will set the progress bar to the appropriate length
     */
    private Runnable updateProgress(int numDownloaded, int numTotal) {
        Runnable r = () -> progressBar.setProgress((1.0 * numDownloaded) / numTotal);
        return r;
    } // updateProgress

    /**
     * The method called when the "play" or "pause" button is pressed.
     */
    private void play() {
        if (animating) {
            animating = false;
            animation.stop();
            Platform.runLater(() -> playButton.setText("Play"));
        } else {
            animating = true;
            animation.play();
            Platform.runLater(() -> playButton.setText("Pause"));
        } // if
    } // play

    /**
     * Throw an {@link java.io.IOException} if the HTTP status code of the
     * {@link java.net.http.HttpResponse} supplied by {@code response} is
     * not {@code 200 OK}.
     * @param <T> response body type
     * @param response response to check
     * @see <a href="https://httpwg.org/specs/rfc7231.html#status.200">[RFC7232] 200 OK</a>
     */
    private static <T> void ensureGoodResponse(HttpResponse<T> response) throws IOException {
        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        } // if
    } // ensureGoodResponse

} // GalleryApp
