package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.UserRight;
import com.github.telegram.bot.models.Right;
import com.github.telegram.bot.repos.UserRightRepository;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
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

    @BotRequest("/addAdmin*")
    private SendMessage start(String text, Long chatId, User user) {
        if (!userHaveRight(user.username(), Right.Admin)) {
            return new SendMessage(chatId, "У вас недостаточно прав на выполнение данной операции");
        }

        String[] textTokens = text.split(" ");
        if (textTokens.length == 1) {
            return new SendMessage(chatId,
                    "Комманда введена некорректно. Использование: /addAdmin {username} {right} = admin");
        }
        String username = textTokens[1];

        Right right = Right.Admin;
        if (textTokens.length > 2) {
            String rightStr = textTokens[2];
            right = Right.fromString(rightStr);
            if (right == null) {
                return new SendMessage(chatId, String.format("Права <b>%s</b> не существует", rightStr))
                        .parseMode(ParseMode.HTML);
            }
        }

        if (userHaveRight(username, right)) {
            return new SendMessage(chatId, String.format(
                    "Пользователь <b>%s</b> уже имеет право <b>%s</b>", username, right.getName()))
                    .parseMode(ParseMode.HTML);
        }

        UserRight newAdminRight = new UserRight();
        newAdminRight.username = username;
        newAdminRight.right = right;
        userRightRepository.save(newAdminRight);
        log.info(String.format("Пользователь %s выдал права администратора пользователю %s", user.username(), username));
        return new SendMessage(chatId, String.format("Пользователю <b>%s</b> выданы права администратора", username))
                .parseMode(ParseMode.HTML);
    }

    private boolean userHaveRight(String username, Right right) {
        return userRightRepository.existsByUsernameAndRight(username, right);
    }
}
