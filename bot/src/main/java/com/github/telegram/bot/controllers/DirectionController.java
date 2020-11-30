package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.RequestHistoryItem;
import com.github.telegram.bot.db.ServerLink;
import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.Command;
import com.github.telegram.bot.repos.RequestHistoryRepository;
import com.github.telegram.bot.repos.ServerLinksRepository;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.HtmlParser;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Date;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class DirectionController {
    private final TransportStopRepository transportStopRepository;

    private final ServerLinksRepository serverLinksRepository;

    private final RequestHistoryRepository requestHistoryRepository;

    public DirectionController(
            TransportStopRepository transportStopRepository,
            ServerLinksRepository serverLinksRepository,
            RequestHistoryRepository requestHistoryRepository) {
        this.transportStopRepository = transportStopRepository;
        this.serverLinksRepository = serverLinksRepository;
        this.requestHistoryRepository = requestHistoryRepository;
    }

    @BotRequest(value = "/direction *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage getScheduleByTransportStopId(String text, Long chatId, User user) {
        int transportStopId = Integer.parseInt(text.split(" ")[1]);
        ServerLink serverLink = serverLinksRepository.findFirstByTransportStop_Id(transportStopId);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Остановка %s (%s)\n", transportStop.name, transportStop.direction));
        String content = HtmlParser.parse(serverLink.link);
        builder.append(content);
        saveRequest(transportStop, user.id());
        return new SendMessage(chatId, builder.toString()).parseMode(ParseMode.HTML)
                .replyMarkup(getKeyboard(transportStopId));
    }

    private void saveRequest(TransportStop transportStop, int userId) {
        RequestHistoryItem request = new RequestHistoryItem();
        request.datetime = new Date();
        request.transportStop = transportStop;
        request.userId = userId;
        requestHistoryRepository.save(request);
    }

    private Keyboard getKeyboard(int transportStopId) {
        return KeyboardHelper.getInlineKeyboardFromItems(
                Command.endCommands,
                Command::getName,
                command -> String.format("%s %s", command.toString(), transportStopId),
                "command",
                1);
    }
}
