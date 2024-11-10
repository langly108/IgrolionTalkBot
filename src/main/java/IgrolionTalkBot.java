import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.imageio.ImageIO;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IgrolionTalkBot implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient telegramClient = new OkHttpTelegramClient(System.getenv("IGROLION_TALK_BOT_TOKEN"));

    private long ignoreMessagesCount = 15;
    private long messagesInSession = 0;

    private static String nolan(String str) {
        str = str.replaceAll("/nolan", "").replaceAll("[+.^:,?!]", "");
        System.out.println("Длина строки: " + str.length());
        if (str.length() == 0) {
            return "";
        }
        String[] words = str.split(" ");
        ArrayList<String> filteredWords = new ArrayList<>();
        for (String word : words) {
            if (word.length() >= 3) {
                filteredWords.add(word);
            }
        }
        if (filteredWords.size() > 0) {
            Random random = new Random();
            int index = random.nextInt(filteredWords.size());
            return filteredWords.get(index) + "?";
        } else {
            return "Тупое предложение без нормальных слов, переделывай.";
        }
    }

    private static String composeMessage(List<String> ls) {
        int pos = 0;
        StringBuilder result = new StringBuilder();

        for (String s : ls) {
            String s1 = s;
            if (pos > 0) {
                s1 = s.substring(0, 1).toLowerCase() + s.substring(1);
            }
            if ((s.charAt(s.length() - 1)) == '?' || (s.charAt(s.length() - 1)) == '.' || (s.charAt(s.length() - 1)) == ',' || (s.charAt(s.length() - 1)) == '-' || (s.charAt(s.length() - 1)) == '!') {
                result.append(s1).append(" ");
            } else
                result.append(s1).append(", ");
            pos++;
        }

        return result.toString().replaceAll(", $", "").replaceAll("@", "");
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getMessageThreadId() != null && update.getMessage().getMessageThreadId() == 7) {
            if (!update.getMessage().getText().startsWith("/nolan")) {
                messagesInSession++;
                if (update.getMessage().getText().length() > 4 && !update.getMessage().getText().equals("/phrase")) {
                    try {
                        PreparedStatement ps = Main.con.prepareStatement("insert into floodchat(msg_text) values (?)");
                        ps.setString(1, update.getMessage().getText());
                        ps.execute();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (messagesInSession % ignoreMessagesCount == 0 || update.getMessage().getText().equals("/phrase")) {
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

                }
            } else {
                String msg = nolan(update.getMessage().getText());
                if (msg.length() > 0) {
                    SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), msg);
                    sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
                    sendMessage.setMessageThreadId(update.getMessage().getMessageThreadId());
                    try {
                        telegramClient.execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {

            if (update.getMessage().getText().indexOf('[') >= 0 && update.getMessage().getText().indexOf(']') > 0 &&
                    update.getMessage().getText().indexOf('[') < update.getMessage().getText().indexOf(']') - 1) {
                String gameinfo = null;
                try {
                    GameInfo gi = new GameInfo(update.getMessage().getText());
                    if (gi.getErrMsg() == null) {
                        File of;
                        if (gi.getBackgroundImage() != null) {
                            of = new File("img.jpeg");
                            ImageIO.write(gi.getBackgroundImage(), "JPEG", of);
                        } else {
                            of = new File("404.png");
                        }

                        StringBuilder ms = new StringBuilder();
                        ms.append("Название игры: ");
                        ms.append("*").append(gi.getGameName()).append("*\n");
                        ms.append("Жанр: ");
                        if (gi.getGenres().size() > 0) {
                            ms.append("*").append(gi.getGenres()).append("*\n");
                        } else {
                            ms.append("*Неизвестен*\n");
                        }
                        ms.append("Дата выхода: ");
                        if (gi.getReleaseDate() != null) {
                            DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                            ms.append("*").append(df.format(gi.getReleaseDate())).append("*\n");

                        } else {
                            ms.append("*Неизвестна* \n");
                        }
                        ms.append("Платформы: ");
                        ms.append("*").append(gi.getPlatforms()).append("*\n");

                        ms.append("Средняя оценка: ");
                        ms.append("*").append(gi.getMetascore() > 0 ? (int) gi.getMetascore() + "/100" : "Нет данных").append("*\n");
                        ms.append("*-----------------------------------------*\n");

                        ms.append("Разработчики: ");
                        if (gi.getDevelopers().size() > 0) {
                            ms.append("*").append(gi.getDevelopers()).append("*\n");
                        } else {
                            ms.append("*Неизвестны*\n");
                        }

                        ms.append("Издатели: ");
                        if (gi.getPublishers().size() > 0) {
                            ms.append("*").append(gi.getPublishers()).append("*\n");
                        } else {
                            ms.append("*Неизвестны*\n");
                        }

                        SendPhoto sendPhoto = new SendPhoto(update.getMessage().getChatId().toString(), new InputFile(of));


                        if (update.getMessage().getMessageThreadId() != null) {
                            sendPhoto.setMessageThreadId(update.getMessage().getMessageThreadId());
                        }
                        sendPhoto.setCaption(ms.toString().replace(".", "\\.").replace("<", "\\<").replace(">", "\\>")
                                .replace("-", "\\-").replace("#", "\\#").replace("!", "\\!").replace("(", "\\(").replace(")", "\\)").replace("|", "\\|")
                        );
                        sendPhoto.setParseMode(ParseMode.MARKDOWNV2);
                        sendPhoto.setReplyToMessageId(update.getMessage().getMessageId());
                        telegramClient.execute(sendPhoto);
                    } else {
                        SendMessage sm = new SendMessage(update.getMessage().getChatId().toString(), gi.getErrMsg());
                        sm.setReplyToMessageId(update.getMessage().getMessageId());
                        telegramClient.execute(sm);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}
