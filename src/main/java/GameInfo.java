import org.apache.commons.lang3.StringUtils;
import org.json.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class GameInfo {
    public GameInfo(String msg) throws IOException {
        String rawName = StringUtils.substringBetween(msg, "[", "]");
        String slgName = rawName.toLowerCase().replace(" ", "-").replace(":", "").replace("'", "").replace("$","").replace("#","").replace("?","");
        if (slgName.equalsIgnoreCase("кал")) {
            slgName = "Dark Souls 2";
        }

        try {
            JSONObject gameInfo = URLTools.getInfo(slgName);
            if (gameInfo == null) {
                return;
            }
            //иногда присылают редирект, по которому 100% можно найти нужную нам запись
            if (gameInfo.has("redirect")){
                if (gameInfo.getBoolean("redirect")) {
                    gameInfo = URLTools.getInfo(gameInfo.getString("slug"));
                }
            }

            //TODO: пробуем через поиск
            if (gameInfo.has("status")) {
                JSONObject gameSearchedInfo = URLTools.getSearchInfo(slgName);
                if (gameSearchedInfo.has("results") && gameSearchedInfo.getJSONArray("results").length() >0 ) {
                    gameInfo = URLTools.getInfo(gameSearchedInfo.getJSONArray("results").getJSONObject(0).getString("slug"));
                } else {
                    gameInfo = new JSONObject("{\"status\":\"404\"}");
                }
            }
            //совсем ничего не нашли
            if (gameInfo.has("status")) {
                if (Integer.parseInt(gameInfo.getString("status")) == HttpURLConnection.HTTP_NOT_FOUND) {
                    errMsg = Main.notFound;
                    return;
                } else if (Integer.parseInt(gameInfo.getString("status")) == HttpURLConnection.HTTP_BAD_GATEWAY) {
                    errMsg = Main.shitGateway;
                    return;
                }
            }

            gameName = gameInfo.getString("name");
            try {
                metascore = gameInfo.getDouble("metacritic");
            } catch (JSONException e) {
                metascore = -1;
            }
            description = gameInfo.getString("description");
            try {
                releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(gameInfo.getString("released"));
            } catch (Exception e) {

            }

            if (!gameInfo.isNull("background_image")) {
                URL url = new URL(gameInfo.getString("background_image"));
                backgroundImage = ImageIO.read(url);
            }
            platforms = new ArrayList<>();
            JSONArray platformsArr = gameInfo.getJSONArray("platforms");
            for (int i = 0; i < platformsArr.length(); i++) {
                JSONObject pl = platformsArr.getJSONObject(i);
                String plName = pl.getJSONObject("platform").getString("name");
                platforms.add(plName);
            }

            developers = new ArrayList<>();
            if (gameInfo.getJSONArray("developers") != null) {
                JSONArray devsArr = gameInfo.getJSONArray("developers");
                for (int i = 0; i < devsArr.length(); i++) {
                    JSONObject dev = devsArr.getJSONObject(i);
                    String devName = dev.getString("name");
                    developers.add(devName);
                }
            }

            publishers = new ArrayList<>();
            if (gameInfo.getJSONArray("publishers") != null) {
                JSONArray devsArr = gameInfo.getJSONArray("publishers");
                for (int i = 0; i < devsArr.length(); i++) {
                    JSONObject dev = devsArr.getJSONObject(i);
                    String devName = dev.getString("name");
                    publishers.add(devName);
                }
            }

            genres = new ArrayList<>();
            if (gameInfo.getJSONArray("genres") != null) {
                JSONArray devsArr = gameInfo.getJSONArray("genres");
                for (int i = 0; i < devsArr.length(); i++) {
                    JSONObject dev = devsArr.getJSONObject(i);
                    String devName = dev.getString("name");
                    genres.add(devName);
                }
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String gameName;
    private String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    private Date releaseDate;

    private BufferedImage backgroundImage;

    public ArrayList<String> getPlatforms() {
        return platforms;
    }

    private ArrayList<String> platforms;

    private ArrayList<String> developers;

    private ArrayList<String> genres;

    public ArrayList<String> getGenres() {
        return genres;
    }

    public ArrayList<String> getDevelopers() {
        return developers;
    }

    public ArrayList<String> getPublishers() {
        return publishers;
    }

    private ArrayList<String> publishers;

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    private double metascore;

    private String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setMetascore(double metascore) {
        this.metascore = metascore;
    }

    public String getGameName() {
        return gameName;
    }

    public double getMetascore() {
        return metascore;
    }

}
