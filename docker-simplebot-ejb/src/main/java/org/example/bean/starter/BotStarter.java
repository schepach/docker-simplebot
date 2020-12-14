package org.example.bean.starter;

import org.example.IDBOperations;
import org.example.bot.SimpleBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

@Startup
@Singleton
public class BotStarter {

    private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    private BotSession botSession;

    @Inject
    IDBOperations idbOperations;

    @PostConstruct
    public void init() {
        logger.log(Level.SEVERE, "ApiContextInitializer...");
        ApiContextInitializer.init();
        logger.log(Level.SEVERE, "Initialization BotsApi....");
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

        try {
            logger.log(Level.SEVERE, "OK!");
            logger.log(Level.SEVERE, "Register SimpleBot....");
            botSession = telegramBotsApi.registerBot(new SimpleBot(idbOperations));
            logger.log(Level.SEVERE, "Register done!");
            logger.log(Level.SEVERE, "SimpleBot was started...");
            idbOperations.createTableToDB();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Exception: ", ex);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            logger.log(Level.SEVERE, "Stop botSession...");
            botSession.stop();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Destroyed exception: ", ex);
        }
    }

}