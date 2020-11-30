package com.github.telegram.bot.controllers;

import com.github.telegram.bot.models.FirstLetter;
import com.github.telegram.bot.models.Transport;
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
public class TransportController {
    @BotRequest(value = "/transport *", messageType = MessageType.INLINE_CALLBACK)
    private SendMessage setTransportAndSendFirstLetterPrompt(String text, Long chatId) {
        String transportStr = text.split(" ")[1];
        Transport transport = Transport.fromString(transportStr);
        return sendFirstLetterPrompt(chatId, transport);
    }

    private SendMessage sendFirstLetterPrompt(Long chatId, Transport transport) {
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                FirstLetter.values(),
                FirstLetter::getValue,
                letter -> String.format("%s %s", letter.getValue(), transport.getName()),
                "letter",
                8);
        return new SendMessage(chatId, "Выберите первую букву из названия остановки")
                .replyMarkup(inlineKeyboardMarkup);
    }
}
