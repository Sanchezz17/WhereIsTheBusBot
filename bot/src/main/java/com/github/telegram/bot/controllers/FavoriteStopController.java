package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.FavoriteRequest;
import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.Command;
import com.github.telegram.bot.repos.FavoriteRequestRepository;
import com.github.telegram.bot.repos.TransportStopRepository;
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

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class FavoriteStopController {
    private final TransportStopRepository transportStopRepository;

    private final FavoriteRequestRepository favoriteRequestRepository;

    public FavoriteStopController(
            TransportStopRepository transportStopRepository,
            FavoriteRequestRepository favoriteRequestRepository) {
        this.transportStopRepository = transportStopRepository;
        this.favoriteRequestRepository = favoriteRequestRepository;
    }

    @BotRequest(value = "/command ADD_TO_FAVORITE *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage addTransportStopToFavorite(String text, Long chatId, User user) {
        int transportStopId = Integer.parseInt(text.split(" ")[2]);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        FavoriteRequest request = favoriteRequestRepository.findFirstByTransportStop(transportStop);
        String message;
        if (request == null) {
            FavoriteRequest favoriteRequest = new FavoriteRequest();
            favoriteRequest.transportStop = transportStop;
            favoriteRequest.userId = user.id();
            favoriteRequestRepository.save(favoriteRequest);
            message = String.format(
                    "Остановка <b>%s %s</b> добавленна в избранное",
                    transportStop.name,
                    transportStop.direction);
        }
        else {
            message = String.format(
                    "Остановка <b>%s %s</b> уже есть в избранном",
                    transportStop.name,
                    transportStop.direction);
        }

        return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
    }

    @BotRequest(value = "/command SHOW_FAVORITE*", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage showFavoriteStops(String text, Long chatId, User user) {
        FavoriteRequest[] favoriteRequests = favoriteRequestRepository.findByUserId(user.id())
                .toArray(new FavoriteRequest[0]);
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                favoriteRequests,
                request -> String.format("%s %s", request.transportStop.name, request.transportStop.direction),
                request -> Integer.toString(request.transportStop.id),
                "direction",
                1);
        return new SendMessage(chatId, "Избранное").replyMarkup(inlineKeyboardMarkup);
    }
}
