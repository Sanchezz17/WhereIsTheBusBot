package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;

@BotController
public class TransportStopController {
    private final TransportStopRepository transportStopRepository;

    public TransportStopController(TransportStopRepository transportStopRepository) {
        this.transportStopRepository = transportStopRepository;
    }

    @BotRequest(value = "/stop *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage getDirectionsByTransportStopName(String text, Long chatId) {
        String[] textTokens = text.split(" ");
        int transportStopId = Integer.parseInt(textTokens[1]);
        Transport transport = Transport.fromString(textTokens[2]);
        String transportStopName = transportStopRepository.findOne(transportStopId).name;
        TransportStop[] transportStops = transportStopRepository.findByNameAndTransport(transportStopName, transport)
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
