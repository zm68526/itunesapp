package cs1302.gallery;

import javafx.scene.layout.TilePane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Orientation;

/**
 * An ImageDisplayer is a type of TilePane consisting of 4 rows of 5
 * ImageView objects each.
 */
public class ImageDisplayer extends TilePane {
    private ImageView[] images = new ImageView[20];
    private int[] activeIndices = new int[20];

    /**
     * Constructs a new ImageDisplayer object.
     */
    public ImageDisplayer() {
        super(Orientation.VERTICAL);
        this.setPrefColumns(5);
        this.setPrefRows(4);
        for (int i = 0; i < 20; i++) {
            images[i] = new ImageView();
            images[i].setImage(new Image("file:resources/default.png"));
            images[i].setFitHeight(100.0);
            images[i].setFitWidth(100.0);
            this.getChildren().addAll(images[i]);
        } // for

        for (int i = 0; i < 20; i++) {
            activeIndices[i] = i;
        } // for
    } // ImageDisplayer

    /**
     * Used to replace the image contained in the ith ImageView.
     *
     * @param i the ImageView whose image will be replaced
     * @param url the url of the new image
     */
    public void replaceImage(int i, String url) {
        images[i].setImage(new Image(url));
    } // replaceImage

    /**
     * Replaces the image of a random ImageView object.
     *
     * @param url the url of the new image
     * @return int the index of the ImageView which had its image replaced
     */
    public int replaceRandomImage(String url) {
        int random = (int)(Math.random() * images.length);
        replaceImage(random, url);
        return random;
    } // replaceRandomImage

    /**
     * Replaces a randomly selected ImageDisplayer with a randomly selected downloaded image.
     * @param numDownloads the number of downloaded images
     */
    public void replaceRandomImage(int numDownloads) {
        boolean executing = true;
        while (executing) {
            int random = (int)(Math.random() * numDownloads);
            if (!isActive(random)) {
                int i = replaceRandomImage("file:resources/downloads/thumb" + random + ".jpg");
                activeIndices[i] = random;
                executing = false;
            } // if
        } // while
    } // replaceRandomImage

    /**
     * Checks if a particular download is currently being displayed or not.
     *
     * @param i the number of the downloaded image to be checked
     * @return true if it is being displayed, false if not
     */
    public boolean isActive(int i) {
        for (int index : activeIndices) {
            if (index == i) {
                return true;
            } // is
        } // for
        return false;
    } // isActive

} // ImageDisplayer
