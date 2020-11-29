package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class TransportStopController {
    private final TransportStopRepository transportStopRepository;

    public TransportStopController(TransportStopRepository transportStopRepository) {
        this.transportStopRepository = transportStopRepository;
    }

    @BotRequest(value = "/stop *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage getDirectionsByTransportStopName(String text, Long chatId) {
        String transportStopName = text.split(" ", 2)[1];
        TransportStop[] transportStops = transportStopRepository.findByName(transportStopName)
                .toArray(new TransportStop[0]);
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                transportStops,
                transportStop -> transportStop.direction,
                transportStop -> String.valueOf(transportStop.id),
                "direction",
                1);
        return new SendMessage(chatId, "Выберите направление движения").replyMarkup(inlineKeyboardMarkup);
    }
}
