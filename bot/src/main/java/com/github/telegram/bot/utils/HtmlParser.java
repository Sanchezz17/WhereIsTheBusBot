package com.github.telegram.bot.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HtmlParser {
    private static final String rowFormat = "<b>%-12s</b>%-12s%-15s\n";
    private static final String header = String.format(rowFormat, "Маршрут", "Время", "Расстояние");

    public static String parse(String url) {
        Document document;
        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            return "Ошибка при загрузке расписания";
        }
        StringBuilder builder = new StringBuilder("\n");
        builder.append("<pre>");
        builder.append(header);
        Elements rows = document.select("div > p~div:not(:last-of-type)");
        for (Element row : rows) {
            String[] rowText = row.text().split(" ");
            String transportNumber = rowText[0];
            String minutesToArrival = String.format("%s %s", rowText[1], rowText[2]);
            String distanceToArrival = String.format("%s %s", rowText[3], rowText[4]);
            String formattedRow = String.format(rowFormat, transportNumber, minutesToArrival, distanceToArrival);
            System.out.println(formattedRow);
            builder.append(formattedRow);
        }
        builder.append("\n");
        builder.append("</pre>");
        System.out.println(builder.toString());
        return builder.toString();
    }
}
