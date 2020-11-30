package com.github.telegram.bot;

import com.github.telegram.mvc.api.BotController;
import com.github.telegram.mvc.api.EnableTelegram;
import com.github.telegram.mvc.config.TelegramBotBuilder;
import com.github.telegram.mvc.config.TelegramMvcConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableTelegram
@BotController
@Configuration
@EnableJpaRepositories
public class WhereIsTheTrolleybusOrTramBot implements TelegramMvcConfiguration {
    private final Environment environment;

    public WhereIsTheTrolleybusOrTramBot(Environment environment) {
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
}
