package com.example.daxing.qualitytest;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


public class SearchYoutube extends AsyncTask<String, ArrayList<ListItem>, ArrayList<ListItem>> {
    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */

    private static YouTube youtube;

    @Override
    protected ArrayList<ListItem> doInBackground(String... strings) {

        if (Looper.myLooper()==null)
            Looper.prepare();
//        Looper.prepare();
        if (strings[0] != "") System.out.println("keyword is " + strings[0]);
        ArrayList<ListItem> searchResultList = search(strings[0]);
        Log.i("After loop()", "return Search Research");
        return searchResultList;

    }

    protected void onPostExecute(ArrayList<ListItem> str) {
        Looper.loop();
        return;
    }

    public ArrayList<ListItem> search(String keyword){
        // Read the developer key from the properties file.
        Properties properties = new Properties();
//        Log.e("search", "after props");
//        Context context = ;
//        try {
////            InputStream in= getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
////            InputStream in = SearchYoutube.class.getResourceAsStream("app/src/main/assets/" + PROPERTIES_FILENAME);
//            Log.e("search", "after context");
//            AssetManager assetManager =
//            Log.e("assetmanager",assetManager.list("").toString());
//            InputStream in = assetManager.open("youtube.properties");
//            properties.load(in);
//
//
//        } catch (IOException e) {
//            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
//                    + " : " + e.getMessage());
//            System.exit(1);
//        }

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("qualityTest").build();

            // Prompt the user to enter a query term.
            String queryTerm = keyword;

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the Google Developers Console for
            // non-authenticated requests. See:
            // https://console.developers.google.com/
            //String token = properties.getProperty("AIzaSyAzmrXIdc2sU6zqUUhCBLsxCtoB1EtoicM");
            //String apiKey = properties.getProperty("276529071213-t6knj02f02u8rrv1cbvgmfg4rkj69e09.apps.googleusercontent.com");
//            if(apiKey == null) {
//               System.out.println("search" + "apikey == null");
//            }
            search.setKey("AIzaSyAzmrXIdc2sU6zqUUhCBLsxCtoB1EtoicM");
            //search.setOauthToken(token);
            search.setQ(queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            if (searchResponse == null) System.out.println("search result is null");

            List<SearchResult> searchResultList = searchResponse.getItems();
            Iterator<SearchResult> iteratorSearchResults = searchResultList.iterator();

            ArrayList<ListItem> myArr = new ArrayList<ListItem>();

            if (searchResultList != null) {
//                Log.i("searchResponse", "search result is not null");

                final List<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
                if (!iteratorSearchResults.hasNext()) {
//                    Log.i("SearchYoutube"," There aren't any results for your query.");
                }

                while (iteratorSearchResults.hasNext()) {
                    SearchResult singleVideo = iteratorSearchResults.next();
                    ResourceId rId = singleVideo.getId();
//                    prettyPrint(searchResultList.iterator(), queryTerm);
                    if (rId.getKind().equals("youtube#video")) {
                        Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                        String url = thumbnail.getUrl();

                        ListItem newItem = new ListItem();

                        System.out.println("URL is " + url);
                        newItem.setUrl(url.toString());
                        newItem.setVideoTitle(singleVideo.getSnippet().getTitle().toString());
                        newItem.setVideoID(rId.getVideoId().toString());
                        myArr.add(newItem);
                    }
                }
                Log.i("Before Return", "return ArrayList");
                return myArr;
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }

}