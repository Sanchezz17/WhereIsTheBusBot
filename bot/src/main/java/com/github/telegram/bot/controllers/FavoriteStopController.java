package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.FavoriteRequest;
import com.github.telegram.bot.db.TransportStop;
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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.transaction.Transactional;

@Transactional
@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class FavoriteStopController {
    private static final Logger log = LogManager.getLogger(FavoriteStopController.class);

    private final TransportStopRepository transportStopRepository;

    private final FavoriteRequestRepository favoriteRequestRepository;

    public FavoriteStopController(
            TransportStopRepository transportStopRepository,
            FavoriteRequestRepository favoriteRequestRepository) {
        this.transportStopRepository = transportStopRepository;
        this.favoriteRequestRepository = favoriteRequestRepository;
    }

    @BotRequest(value = "/command ADD_TO_FAVORITE *", messageType = MessageType.INLINE_CALLBACK)
    public SendMessage addTransportStopToFavorite(String text, Long chatId, User user) {
        int transportStopId = Integer.parseInt(text.split(" ")[2]);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        FavoriteRequest favoriteRequest = favoriteRequestRepository.findByTransportStopAndUserId(transportStop, user.id());
        if (favoriteRequest != null) {
            String message = String.format(
                    "Остановка <b>%s (%s)</b> уже была добавлена в избранное",
                    transportStop.name,
                    transportStop.direction);
            return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
        }
        FavoriteRequest newFavoriteRequest = new FavoriteRequest();
        newFavoriteRequest.transportStop = transportStop;
        newFavoriteRequest.userId = user.id();
        favoriteRequestRepository.save(newFavoriteRequest);
        String message = String.format(
                "Остановка <b>%s (%s)</b> добавлена в избранное",
                transportStop.name,
                transportStop.direction);
        log.info(String.format(
                "Пользователь добавил остановку в избранное. userId: %s, transportStopId: %s",
                user.id(),
                transportStop.id));
        return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
    }

    @BotRequest(value = "/command REMOVE_FROM_FAVORITE *", messageType = MessageType.INLINE_CALLBACK)
    public SendMessage removeTransportStopFromFavorite(String text, Long chatId, User user) {
        int transportStopId = Integer.parseInt(text.split(" ")[2]);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        int removedCount = favoriteRequestRepository.removeByTransportStopAndUserId(transportStop, user.id());
        if (removedCount == 0) {
            String message = String.format(
                    "Остановка <b>%s (%s)</b> уже была удалена из избранного",
                    transportStop.name,
                    transportStop.direction);
            return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
        }
        String message = String.format(
                "Остановка <b>%s (%s)</b> удалена из избранного",
                transportStop.name,
                transportStop.direction);
        log.info(String.format(
                "Пользователь удалил остановку из избранного. userId: %s, transportStopId: %s",
                user.id(),
                transportStop.id));
        return new SendMessage(chatId, message).parseMode(ParseMode.HTML);
    }

    @BotRequest(value = "/command SHOW_FAVORITE*", messageType = MessageType.INLINE_CALLBACK)
    public SendMessage showFavoriteStops(Long chatId, User user) {
        FavoriteRequest[] favoriteRequests = favoriteRequestRepository.findByUserId(user.id())
                .toArray(new FavoriteRequest[0]);
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                favoriteRequests,
                request -> String.format(
                        "%s %s (%s)",
                        request.transportStop.transport.getEmoji(),
                        request.transportStop.name,
                        request.transportStop.direction),
                request -> Integer.toString(request.transportStop.id),
                "direction",
                1);
        return new SendMessage(chatId, "Избранное").parseMode(ParseMode.HTML).replyMarkup(inlineKeyboardMarkup);
    }
}
