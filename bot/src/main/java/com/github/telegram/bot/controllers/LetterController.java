package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.FirstLetter;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class LetterController {
    private final TransportStopRepository transportStopRepository;
    private final Logger log = LogManager.getLogger(LetterController.class);

    public LetterController(TransportStopRepository transportStopRepository) {
        this.transportStopRepository = transportStopRepository;
    }

    @BotRequest(value = "/letter *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage getTransportStopsByFirstLetter(String text, Long chatId) {
        String[] parameters = text.split(" ");
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
        TransportStop[] transportStops = transportStopRepository.findByNameStartsWithAndTransportEquals(
                letter.getValue(), transport).toArray(new TransportStop[0]);
        Map<String, List<TransportStop>> transportStopsGroupsByDirection = Arrays.stream(transportStops)
                .collect(Collectors.groupingBy(transportStop -> transportStop.name));
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                transportStopsGroupsByDirection.keySet().toArray(new String[0]),
                transportStopName -> transportStopName,
                transportStopName -> transportStopName,
                "stop",
                1);
        return new SendMessage(chatId, "Выберите остановку")
                .replyMarkup(inlineKeyboardMarkup);
    }
}
