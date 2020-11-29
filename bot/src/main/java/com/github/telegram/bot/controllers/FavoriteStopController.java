package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.FavoriteRequest;
import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.repos.FavoriteRequestRepository;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.User;
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
        FavoriteRequest favoriteRequest = new FavoriteRequest();
        int transportStopId = Integer.parseInt(text.split(" ")[2]);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        favoriteRequest.transportStop = transportStop;
        favoriteRequest.userId = user.id();
        favoriteRequestRepository.save(favoriteRequest);
        String message = String.format(
                "Остановка %s %s добавленна в избранное",
                transportStop.name,
                transportStop.direction);
        return new SendMessage(chatId, message);
    }
}
