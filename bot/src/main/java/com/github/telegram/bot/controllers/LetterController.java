package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.FirstLetter;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@BotController
public class LetterController {
    private static final Logger log = LogManager.getLogger(LetterController.class);

    private final TransportStopRepository transportStopRepository;

    public LetterController(TransportStopRepository transportStopRepository) {
        this.transportStopRepository = transportStopRepository;
    }

    @BotRequest(value = "/letter *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage getTransportStopsByFirstLetter(String text, Long chatId) {
        String[] parameters = text.split("\\s+");
        String letterStr = parameters[1];
        FirstLetter letter = FirstLetter.fromString(letterStr);
        if (letter == null) {
            log.info("Пользователь ввел символ, а мы его не нашли. Символ: " + letterStr);
            return new SendMessage(chatId, "На эту букву нет остановок");
        }
        String transportStr = parameters[2];
        Transport transport = Transport.fromString(transportStr);
        if (transport == null) {
            return new SendMessage(chatId, "Неизвестный вид транспорта");
        }
        return sendTransportStopNamePrompt(chatId, letter, transport);
    }

    private SendMessage sendTransportStopNamePrompt(Long chatId, FirstLetter letter, Transport transport) {
        TransportStop[] transportStops = transportStopRepository
                                            .findByNameStartsWithAndTransportEquals(letter.getValue(), transport)
                                            .toArray(new TransportStop[0]);
        Map<String, TransportStop> transportStopsGroupsByDirection = Arrays
                .stream(transportStops)
                .collect(Collectors.groupingBy(
                        transportStop -> transportStop.name,
                        Collectors.collectingAndThen(Collectors.toList(), values -> values.get(0))
                ));
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                transportStopsGroupsByDirection.values().toArray(new TransportStop[0]),
                transportStop -> transportStop.name,
                transportStop -> String.format("%s %s", transportStop.id, transportStop.transport.getName()),
                "stop",
                1);
        return new SendMessage(chatId, "Выберите остановку")
                .replyMarkup(inlineKeyboardMarkup);
    }
}
