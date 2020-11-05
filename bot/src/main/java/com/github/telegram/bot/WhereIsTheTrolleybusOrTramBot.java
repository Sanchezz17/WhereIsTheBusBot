package com.github.telegram.bot;

import com.github.telegram.bot.db.ServerLink;
import com.github.telegram.bot.db.TransportStop;
import com.github.telegram.bot.models.FirstLetter;
import com.github.telegram.bot.models.Transport;
import com.github.telegram.bot.repos.ServerLinksRepository;
import com.github.telegram.bot.repos.TransportStopRepository;
import com.github.telegram.bot.utils.KeyboardHelper;
import com.github.telegram.mvc.api.*;
import com.github.telegram.mvc.config.TelegramBotBuilder;
import com.github.telegram.mvc.config.TelegramMvcConfiguration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableTelegram
@BotController
@Configuration
@EnableJpaRepositories
public class WhereIsTheTrolleybusOrTramBot implements TelegramMvcConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(WhereIsTheTrolleybusOrTramBot.class);
    private final HashMap<Integer, Transport> usersTransport = new HashMap<>();
    private final HashMap<Integer, Map<String, List<TransportStop>>> usersTransportStops = new HashMap<>();

    private final TransportStopRepository transportStopRepository;

    private final ServerLinksRepository serverLinksRepository;

    private final Environment environment;

    public WhereIsTheTrolleybusOrTramBot(
            TransportStopRepository transportStopRepository,
            ServerLinksRepository serverLinksRepository,
            Environment environment) {
        this.transportStopRepository = transportStopRepository;
        this.serverLinksRepository = serverLinksRepository;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(WhereIsTheTrolleybusOrTramBot.class);
    }

    @Override
    public void configuration(TelegramBotBuilder telegramBotBuilder) {
        telegramBotBuilder
                .token(environment.getProperty("telegram.bot.token")).alias("myFirsBean");
    }

    @BotRequest("/start")
    SendMessage start(String text,
                      Long chatId,
                      TelegramRequest telegramRequest,
                      TelegramBot telegramBot,
                      Update update,
                      Message message,
                      Chat chat,
                      User user
    ) {
        logger.info("Text = {}", text);
        logger.info("ChatId or UserId = {}", chatId);
        logger.info("Telegram Request = {}", telegramRequest);
        logger.info("TelegramBot = {}", telegramBot);
        logger.info("Update = {}", update);
        logger.info("Message = {}", message);
        logger.info("Chat = {}", chat);
        logger.info("User = {}", user);

        SendMessage hello = new SendMessage(chatId, "Привет! Я - бот, который может подсказать" +
                " через сколько минут приедет общественный транспорт на определенную остановку");
        telegramBot.execute(hello);
        return sendTransportPrompt(chatId);
    }

    @BotRequest(value = "/transport *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage setTransportAndSendFirstLetterPrompt(String text, Long chatId, User user, TelegramBot telegramBot) {
        String transportStr = text.split(" ")[1];
        Transport transport = Transport.fromString(transportStr);
        if (transport == null) {
            telegramBot.execute(new SendMessage(chatId, "Неизвестный вид общественного транспорта"));
            return sendTransportPrompt(chatId);
        }
        usersTransport.put(user.id(), transport);
        return sendFirstLetterPrompt(chatId);
    }

    @BotRequest(value = "/letter *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage getTransportStopsByFirstLetter(String text, Long chatId, User user, TelegramBot telegramBot) {
        String letterStr = text.split(" ")[1];
        if (letterStr.length() > 1) {
            telegramBot.execute(new SendMessage(chatId, "Введите одну букву"));
            return sendFirstLetterPrompt(chatId);
        }
        FirstLetter letter = FirstLetter.fromStr(letterStr);
        if (letter == null) {
            telegramBot.execute(new SendMessage(chatId, "На эту букву нет остановок"));
            return sendFirstLetterPrompt(chatId);
        }
        return sendTransportStopNamePrompt(chatId, user, letter);
    }

    @BotRequest(value = "/stop *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage getDirectionsByTransportStopName(String text, Long chatId, User user, TelegramBot telegramBot) {
        if (!usersTransportStops.containsKey(user.id())) {
            // можно запоминать выбранную первую букву
            // и здесь уже вызывать sendTransportStopNamePrompt(chatId, user, userFirstLetters(user.id()))
            return sendFirstLetterPrompt(chatId);
        }
        String transportStopName = text.split(" ", 2)[1];
        Map<String, List<TransportStop>> transportStopsGroupsByDirection = usersTransportStops.get(user.id());
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                transportStopsGroupsByDirection.get(transportStopName).toArray(new TransportStop[0]),
                transportStop -> transportStop.direction,
                transportStop -> String.valueOf(transportStop.id),
                "direction",
                1);
        return new SendMessage(chatId, "Выберите направление движения").replyMarkup(inlineKeyboardMarkup);
    }

    @BotRequest(value = "/direction *", messageType = MessageType.INLINE_CALLBACK)
    SendMessage getScheduleByTransportStopId(String text, Long chatId, User user, TelegramBot telegramBot) {
        if (!usersTransportStops.containsKey(user.id())) {
            // можно запоминать выбранную первую букву
            // и здесь уже вызывать sendTransportStopNamePrompt(chatId, user, userFirstLetters(user.id()))
            return sendFirstLetterPrompt(chatId);
        }
        int transportStopId = Integer.parseInt(text.split(" ")[1]);
        ServerLink serverLink = serverLinksRepository.findFirstByTransportStop_Id(transportStopId);
        TransportStop transportStop = transportStopRepository.findOne(transportStopId);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Остановка %s (%s)\n", transportStop.name, transportStop.direction));
        // toDo вместо этого Parser.parse(serverLink.link)
        builder.append(serverLink.link);
        return new SendMessage(chatId, builder.toString());
    }

    private SendMessage sendTransportPrompt(Long chatId) {
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                Transport.values(),
                Transport::getName,
                Transport::getName,
                "transport",
                2);
        return new SendMessage(chatId, "Выберите вид общественного транспорта")
                .replyMarkup(inlineKeyboardMarkup);
    }

    private SendMessage sendFirstLetterPrompt(Long chatId) {
        Keyboard inlineKeyboardMarkup = KeyboardHelper.getInlineKeyboardFromItems(
                FirstLetter.values(),
                FirstLetter::getValue,
                FirstLetter::getValue,
                "letter",
                8);
        return new SendMessage(chatId, "Выберите первую букву из названия остановки")
                .replyMarkup(inlineKeyboardMarkup);
    }

    private SendMessage sendTransportStopNamePrompt(Long chatId, User user, FirstLetter letter) {
        if (!usersTransport.containsKey(user.id())) {
            return sendTransportPrompt(chatId);
        }
        TransportStop[] transportStops = transportStopRepository.findByNameStartsWithAndTransportEquals(
                letter.getValue(), usersTransport.get(user.id())).toArray(new TransportStop[0]);
        Map<String, List<TransportStop>> transportStopsGroupsByDirection = Arrays.stream(transportStops)
                .collect(Collectors.groupingBy(transportStop -> transportStop.name));
        usersTransportStops.put(user.id(), transportStopsGroupsByDirection);
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
