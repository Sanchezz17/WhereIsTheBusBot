package com.github.telegram.bot.controllers;

import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.EnableTelegram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableTelegram
@Configuration
@EnableJpaRepositories
@BotController
public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    // toDo реализовать базовый контроллер-обертку, в которой будет логика логгирования
    // затем на каждое сообщение свой контроллер
}
