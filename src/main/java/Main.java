import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static Connection con;
    public static String rawgToken = System.getenv("RAWG_API_TOKEN");

    public static String notFound = "К сожалению, ничего не найдено";
    public static String shitGateway = "Проблемы с сетью, попробуйте отправить сообщение заново";

    public static void main(String[] args) throws TelegramApiException, SQLException, ClassNotFoundException {
        try {
            String botToken = System.getenv("IGROLION_TALK_BOT_TOKEN");

            try {
                con = DBTools.connect();
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
            }
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, new IgrolionTalkBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}
