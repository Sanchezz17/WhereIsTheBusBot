package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.UserRight;
import com.github.telegram.bot.models.Right;
import com.github.telegram.bot.repos.UserRightRepository;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.TelegramRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class AdminController {
    private static final Logger log = LogManager.getLogger(AdminController.class);

    private final UserRightRepository userRightRepository;

    public AdminController(UserRightRepository userRightRepository) {
        this.userRightRepository = userRightRepository;
    }

    @BotRequest("/addAdmin *")
    private SendMessage start(String text,
                              Long chatId,
                              TelegramRequest telegramRequest,
                              TelegramBot telegramBot,
                              Update update,
                              Message message,
                              Chat chat,
                              User user
    ) {
        if (!checkAdminRight(user.username())) {
            return new SendMessage(chatId, "У вас недостаточно прав на выполнение данной операции");
        }
        String username = text.split(" ")[1];
        if (username == null || username.isEmpty()) {
            return new SendMessage(chatId, "Комманда введена некорректно. Использование: /addAdmin {username}");
        }
        UserRight newAdminRight = new UserRight();
        newAdminRight.username = username;
        newAdminRight.right = Right.Admin;
        userRightRepository.save(newAdminRight);
        log.info(String.format("Пользователь %s выдал права администратора пользователю %s", user.username(), username));
        return new SendMessage(chatId, String.format("Пользователю <b>%s</b> выданы права администратора", username))
                .parseMode(ParseMode.HTML);
    }

    private boolean checkAdminRight(String username) {
        return userRightRepository.existsByUsernameAndRight(username, Right.Admin);
    }
}
