import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class URLTools {

    private static String readFromUrl(InputStream is) throws IOException {
        BufferedReader br =
                new BufferedReader(
                        new InputStreamReader(is));

        String input;
        StringBuilder allData = new StringBuilder();

        while ((input = br.readLine()) != null) {
            allData.append(input);
        }
        br.close();

        return allData.toString();
    }

    private static HttpsURLConnection callUrl(String Url) throws IOException {
        URL url = new URL(Url);

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setAllowUserInteraction(true);
        con.setRequestMethod("GET");
        con.connect();

        return con;
    }

    private static JSONObject handleExcept (HttpURLConnection con) throws IOException {
        if (con.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            return new JSONObject("{\"status\":\"404\"}");
        } else if (con.getResponseCode() == HttpURLConnection.HTTP_BAD_GATEWAY) {
            return new JSONObject("{\"status\":\"502\"}");
        } else {
            return null;
        }
    }

    public static JSONObject getInfo(String slgName) throws IOException {

        HttpsURLConnection con = callUrl("https://api.rawg.io/api/games/" + slgName + "?key=" + Main.rawgToken);

        if (handleExcept(con) != null) {
            return handleExcept(con);
        }

        String allData = readFromUrl(con.getInputStream());

        try {
            return new JSONObject(allData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getSearchInfo(String searchString) throws IOException {
        String trimedStr = searchString.replace(" ", "+");
        HttpsURLConnection con = callUrl("https://api.rawg.io/api/games?key=" + Main.rawgToken + "&search="+trimedStr);

        if (handleExcept(con) != null) {
            return handleExcept(con);
        }
        String allData = readFromUrl(con.getInputStream());
        try {
            return new JSONObject(allData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
