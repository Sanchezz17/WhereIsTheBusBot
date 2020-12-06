package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.FavoriteRequest;
import com.github.telegram.bot.db.RequestHistoryItem;
import com.github.telegram.bot.db.ServerLink;
import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.Command;
import com.github.telegram.bot.repos.FavoriteRequestRepository;
import com.github.telegram.bot.repos.RequestHistoryRepository;
import com.github.telegram.bot.repos.ServerLinksRepository;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.HtmlParser;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;

@BotController
public class DirectionController {
    private static final Logger log = LogManager.getLogger(DirectionController.class);

    private final TransportStopRepository transportStopRepository;

    private final ServerLinksRepository serverLinksRepository;

    private final RequestHistoryRepository requestHistoryRepository;

    private final FavoriteRequestRepository favoriteRequestRepository;

    public DirectionController(
            TransportStopRepository transportStopRepository,
            ServerLinksRepository serverLinksRepository,
            RequestHistoryRepository requestHistoryRepository,
            FavoriteRequestRepository favoriteRequestRepository) {
        this.transportStopRepository = transportStopRepository;
        this.serverLinksRepository = serverLinksRepository;
        this.requestHistoryRepository = requestHistoryRepository;
        this.favoriteRequestRepository = favoriteRequestRepository;
    }

    @BotRequest(value = "/direction *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage getScheduleByTransportStopId(String text, Long chatId, User user) {
        int transportStopId = Integer.parseInt(text.split(" ")[1]);
        ServerLink serverLink = serverLinksRepository.findFirstByTransportStop_Id(transportStopId);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(
                "%s Остановка %s (%s)\n",
                transportStop.transport.getEmoji(),
                transportStop.name,
                transportStop.direction));
        String content = HtmlParser.parse(serverLink.link);
        builder.append(content);
        saveRequest(transportStop, user.id());
        return new SendMessage(chatId, builder.toString()).parseMode(ParseMode.HTML)
                .replyMarkup(getEndKeyboard(transportStop, user.id()));
    }

    private void saveRequest(TransportStop transportStop, int userId) {
        RequestHistoryItem request = new RequestHistoryItem();
        request.datetime = new Date();
        request.transportStop = transportStop;
        request.userId = userId;
        log.info(String.format(
                "Пользователь сделал запрос по остановке. userId: %s, transportStopId: %s",
                userId,
                transportStop.id));
        requestHistoryRepository.save(request);
    }

    private Keyboard getEndKeyboard(TransportStop transportStop, int userId) {
        ArrayList<Command> endCommands = new ArrayList<>();
        endCommands.add(Command.START_OVER);
        FavoriteRequest request = favoriteRequestRepository.findByTransportStopAndUserId(transportStop, userId);
        if (request == null) {
            endCommands.add(Command.ADD_TO_FAVORITE);
        } else {
            endCommands.add(Command.REMOVE_FROM_FAVORITE);
        }
        return KeyboardHelper.getInlineKeyboardFromItems(
                endCommands.toArray(new Command[0]),
                Command::getName,
                command -> String.format("%s %s", command.toString(), transportStop.id),
                "command",
                1);
    }
}
