package com.github.telegram.mvc;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelegramService  {
    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
    private final TelegramBot telegramBot;
    private final RequestDispatcher botRequestDispatcher;

    public TelegramService(TelegramBot telegramBot, RequestDispatcher botRequestDispatcher) {
        this.telegramBot = telegramBot;
        this.botRequestDispatcher = botRequestDispatcher;
    }

    public void start() {
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    botRequestDispatcher.execute(update, telegramBot);
                } catch (Exception e) {
                    logger.error("{0}", e);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, new GetUpdates());
    }

}
