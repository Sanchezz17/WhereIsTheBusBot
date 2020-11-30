package com.github.telegram.bot.controllers;

import com.github.telegram.bot.db.ServerLink;
import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.repos.ServerLinksRepository;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.HtmlParser;
import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.BotRequest;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.api.MessageType;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class DirectionController {
    private final TransportStopRepository transportStopRepository;

    private final ServerLinksRepository serverLinksRepository;

    public DirectionController(
            TransportStopRepository transportStopRepository,
            ServerLinksRepository serverLinksRepository) {
        this.transportStopRepository = transportStopRepository;
        this.serverLinksRepository = serverLinksRepository;
    }

    @BotRequest(value = "/direction *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage getScheduleByTransportStopId(String text, Long chatId) {
        int transportStopId = Integer.parseInt(text.split(" ")[1]);
        ServerLink serverLink = serverLinksRepository.findFirstByTransportStop_Id(transportStopId);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Остановка %s (%s)\n", transportStop.name, transportStop.direction));
        String content = HtmlParser.parse(serverLink.link);
        builder.append(content);
        return new SendMessage(chatId, builder.toString()).parseMode(ParseMode.HTML);
    }
}
