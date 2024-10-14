import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IgrolionTalkBot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(System.getenv("IGROLION_TALK_BOT_TOKEN"));

    private long ignoreMessagesCount = 20;
    private long messagesInSession = 0;

    private static String composeMessage(List<String> ls) {
        int pos = 0;
        StringBuilder result = new StringBuilder();

        for (String s: ls) {
            String s1 = s;
            if (pos > 0) {
                s1 = s.substring(0, 1).toLowerCase() + s.substring(1);
            }
            if ((s.charAt(s.length()-1)) == '?' || (s.charAt(s.length()-1)) == '.' || (s.charAt(s.length()-1)) == ','|| (s.charAt(s.length()-1)) == '-') {
                result.append(s1).append(" ");
            } else
                result.append(s1).append(", ");
            pos++;
        }

        return result.toString().replaceAll(", $", "");
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getMessageThreadId() != null && update.getMessage().getMessageThreadId() == 7) {
            messagesInSession++;
            if (update.getMessage().getText().length() > 4) {
                try {
                    PreparedStatement ps = Main.con.prepareStatement("insert into floodchat(msg_text) values (?)");
                    ps.setString(1, update.getMessage().getText());
                    ps.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            if (messagesInSession % ignoreMessagesCount == 0) {
                Random rnd = new Random();
                int limit = rnd.nextInt(3) + 2;
                try {
                    PreparedStatement ps = Main.con.prepareStatement("select case\n" +
                            "           when length(msg_text) > 50 then substr(substr(msg_text, 1, 50), 1, length(substr(msg_text, 1, 50)) -\n" +
                            "                                                                              position(' ' in reverse(substr(msg_text, 1, 50))) +\n" +
                            "                                                                              1)\n" +
                            "           else msg_text end\n" +
                            "from floodchat order by random() limit ?");
                    ps.setInt(1, limit);
                    ResultSet rs = ps.executeQuery();
                    List<String> textList = new ArrayList<>();
                    while (rs.next()) {
                        textList.add(rs.getString("msg_text"));
                    }

                    String messageToSend = composeMessage(textList);

                    SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), messageToSend);
            sendMessage.setMessageThreadId(update.getMessage().getMessageThreadId());
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(update.getMessage().getText());
                System.out.println(update.getMessage().getMessageThreadId());

            }

        }
    }
}
