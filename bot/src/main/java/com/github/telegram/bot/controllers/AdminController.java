package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.RequestHistoryItem;
import com.github.telegram.bot.db.UserRight;
import com.github.telegram.bot.models.Right;
import com.github.telegram.bot.repos.RequestHistoryRepository;
import com.github.telegram.bot.repos.UserRightRepository;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class AdminController {
    private static final int messageLengthLimit = 3500;
    private static final Logger log = LogManager.getLogger(AdminController.class);

    private final UserRightRepository userRightRepository;

    private final RequestHistoryRepository requestHistoryRepository;

    public AdminController(
            UserRightRepository userRightRepository,
            RequestHistoryRepository requestHistoryRepository) {
        this.userRightRepository = userRightRepository;
        this.requestHistoryRepository = requestHistoryRepository;
    }

    @BotRequest("/addRight*")
    private SendMessage addRightToUser(String text, Long chatId, User user) {
        if (!userHaveRight(user.username(), Right.Admin)) {
            return new SendMessage(chatId, "У вас недостаточно прав на выполнение данной операции");
        }

        String[] textTokens = text.split(" ");
        if (textTokens.length == 1) {
            return new SendMessage(chatId,
                    "Комманда введена некорректно. Использование: /addRight {username} {right} = admin");
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
        log.info(String.format("Админ %s выдал права администратора пользователю %s", user.username(), username));
        return new SendMessage(chatId, String.format("Пользователю <b>%s</b> выдано право %s", username, right.getName()))
                .parseMode(ParseMode.HTML);
    }

    @BotRequest("/history*")
    private SendMessage showHistory(String text, Long chatId, User user, TelegramBot telegramBot) {
        if (!userHaveRight(user.username(), Right.Admin)) {
            return new SendMessage(chatId, "У вас недостаточно прав на выполнение данной операции");
        }
        int itemsCount = 10;
        String[] textTokens = text.split(" ");
        if (textTokens.length > 1) {
            try {
                itemsCount = Integer.parseInt(textTokens[1]);
            } catch (Exception ex) {
                return new SendMessage(chatId,
                        "Комманда введена некорректно. Использование: /history {itemsCount} = 10");
            }
        }
        List<RequestHistoryItem> history = requestHistoryRepository
                .getHistorySortedByDateDesc(new PageRequest(0, itemsCount));
        StringBuilder builder = new StringBuilder("История запросов\n");
        for (RequestHistoryItem historyItem : history) {
            if (builder.length() > messageLengthLimit) {
                telegramBot.execute(new SendMessage(chatId, builder.toString()).parseMode(ParseMode.HTML));
                builder = new StringBuilder();
            }
            builder.append(String.format(
                    "<i>%s</i> Пользователь <b>%s</b> запросил информацию по остановке <b>%s (%s)</b>\n\n",
                    historyItem.datetime,
                    historyItem.userId,
                    historyItem.transportStop.name,
                    historyItem.transportStop.direction));
        }
        log.info(String.format("Админ %s просмотрел историю запросов", user.username()));
        return new SendMessage(chatId, builder.toString()).parseMode(ParseMode.HTML);
    }

    @BotRequest(value = "/command ADMIN_COMMANDS*", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage getAdminCommandsList(Long chatId, User user) {
        if (!userHaveRight(user.username(), Right.Admin)) {
            return new SendMessage(chatId, "У вас недостаточно прав на выполнение данной операции");
        }
        final String adminCommandsList = "Команды администратора\n\n" +
                "/addRight {<b>username</b>} {<b>right</b>} = admin - " +
                "выдать право <b>right</b> пользователю <b>username</b>\n\n" +
                "/history {<b>itemsCount</b>} = 10 - " +
                "просмотреть последние <b>itemsCount</b> запросов к боту\n\n";
        return new SendMessage(chatId, adminCommandsList).parseMode(ParseMode.HTML);
    }

    private boolean userHaveRight(String username, Right right) {
        return userRightRepository.existsByUsernameAndRight(username, right);
    }
}
