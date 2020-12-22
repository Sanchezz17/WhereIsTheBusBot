package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.RequestHistoryItem;
import com.github.telegram.bot.db.UserRight;
import com.github.telegram.bot.models.Right;
import com.github.telegram.bot.repos.RequestHistoryRepository;
import com.github.telegram.bot.repos.UserRightRepository;
import com.github.telegram.bot.utils.Extensions;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;


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

        if (!text.matches("^\\s*/addRight\\s+\\w+\\s*\\w*\\s*$"))
            return new SendMessage(chatId, "Комманда введена некорректно. Использование: /addRight {username} {right} = admin");

        String[] textTokens = text
                                .trim()
                                .split("\\s+");

        String usernameToken = textTokens[1];
        String rightToken = textTokens.length == 3
                ? textTokens[2]
                : "admin";

        Right right = Right.fromString(rightToken);
        if (right == null) {
            return new SendMessage(chatId, String.format("Права <b>%s</b> не существует", rightToken))
                        .parseMode(ParseMode.HTML);
        }


        if (userHaveRight(usernameToken, right)) {
            return new SendMessage(chatId, String.format(
                    "Пользователь <b>%s</b> уже имеет право <b>%s</b>", usernameToken, right.getName()))
                    .parseMode(ParseMode.HTML);
        }

        UserRight newAdminRight = new UserRight();
        newAdminRight.username = usernameToken;
        newAdminRight.right = right;
        userRightRepository.save(newAdminRight);
        log.info(String.format("Админ %s выдал права администратора пользователю %s", user.username(), usernameToken));
        return new SendMessage(chatId, String.format("Пользователю <b>%s</b> выдано право %s", usernameToken, right.getName()))
                .parseMode(ParseMode.HTML);
    }

    @BotRequest("/history*")
    private SendMessage showHistory(String text, Long chatId, User user, TelegramBot telegramBot) {
        if (!userHaveRight(user.username(), Right.Admin)) {
            return new SendMessage(chatId, "У вас недостаточно прав на выполнение данной операции");
        }

        if (!text.matches("^\\s*/history\\s*\\d*\\s*$"))
            return new SendMessage(chatId, "Комманда введена некорректно. Использование: /history {itemsCount} = 10");

        String[] textTokens = text
                                .trim()
                                .split("\\s+");

        int itemsCount = textTokens.length == 2
                            ? Integer.parseInt(textTokens[1])
                            : 10;

        List<RequestHistoryItem> history = requestHistoryRepository.getHistorySortedByDateDesc(new PageRequest(0, itemsCount));
        ArrayList<String> historyMessageLines = FormatHistoryMessageLines(history);
        List<List<String>> messages = Extensions.SplitArrayListBySizeOfInnerStrings(historyMessageLines, messageLengthLimit);

        for(int i = 0; i < messages.size() - 1; i++) {
            telegramBot.execute(new SendMessage(chatId, String.join("\n", messages.get(i)))
                                        .parseMode(ParseMode.HTML)
            );
        }
        log.info(String.format("Админ %s просмотрел историю запросов", user.username()));
        return new SendMessage(chatId, String.join("\n", String.join("\n", messages.get(messages.size() - 1))))
                .parseMode(ParseMode.HTML);
    }

    private ArrayList<String> FormatHistoryMessageLines(List<RequestHistoryItem> historyItems){
        ArrayList<String> builder = new ArrayList<>();
        builder.add("История запросов");
        for (RequestHistoryItem historyItem : historyItems) {
            builder.add(String.format(
                    "<i>%s</i> Пользователь <b>%s</b> запросил информацию по остановке <b>%s (%s)</b>\n",
                    historyItem.datetime,
                    historyItem.userId,
                    historyItem.transportStop.name,
                    historyItem.transportStop.direction));
        }
        return builder;
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
