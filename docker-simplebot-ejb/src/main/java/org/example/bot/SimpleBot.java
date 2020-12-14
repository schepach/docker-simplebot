package org.example.bot;


import org.example.IDBOperations;
import org.example.redis.RedisManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleBot extends TelegramLongPollingBot {

    private static final Logger logger = Logger.getLogger(SimpleBot.class.getSimpleName());

    private IDBOperations idbOperations;

    public SimpleBot() {
    }

    @Inject
    public SimpleBot(IDBOperations idbOperations) {
        this.idbOperations = idbOperations;
    }

    @Override
    public void onUpdateReceived(Update update) {

        Calendar cal = Calendar.getInstance();
        logger.log(Level.INFO, "Date - {0}", cal.getTime());

        Message message = update.getMessage();

        if (message != null && message.hasText()) {
            RedisManager.checkRedisStore(String.valueOf(message.getChatId()));

            logger.log(Level.INFO, "FirstName: {0}, LastName: {1}, UserName: {2} \n" +
                            "UserId: {3}, ChatId: {4}, CommandInput: {5}",
                    new Object[]{message.getFrom().getFirstName(),
                            message.getFrom().getLastName(),
                            message.getFrom().getUserName(),
                            message.getFrom().getId(),
                            message.getChatId(),
                            message.getText()});

            idbOperations.insertDataToDB(message.getFrom().getId(), message.getFrom().getFirstName());
            logger.log(Level.INFO, "Elements from SQLite DB - {0}", idbOperations.selectDataFromDB());
            sendMessage(message, "OK, your data was added into Database...");
        }
    }

    private void sendMessage(Message message, String textMessage) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(textMessage);
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            logger.log(Level.SEVERE, "TelegramApiException: ", ex);
        }
    }

    @Override
    public String getBotUsername() {
        return "botName";
    }

    @Override
    public String getBotToken() {
        return "botToken";
    }
}