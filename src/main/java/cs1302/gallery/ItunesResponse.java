package cs1302.gallery;

/**
 * Represents a response from the iTunes Search API. This is used by Gson to
 * create an object from the JSON response body. This class is provided with
 * project's starter code, and the instance variables are intentionally set
 * to package private visibility.
 */
public class ItunesResponse {
    int resultCount;
    ItunesResult[] results;

    /**
     * Gets the result count.
     *
     * @return the result count
     */
    public int getResultCount() {
        return resultCount;
    }

    /**
     * Gets the results.
     *
     * @return the results
     */
    public ItunesResult[] getResults() {
        return results;
    }

    /**
     * Gets a specific result.
     *
     * @param i the specific result to get
     * @return the specific result
     */
    public ItunesResult getResult(int i) {
        return results[i];
    }
} // ItunesResponse
