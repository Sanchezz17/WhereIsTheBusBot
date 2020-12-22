package com.github.telegram.bot.utils;

import java.util.ArrayList;
import java.util.List;

public class Extensions {

    public static List<List<String>> SplitArrayListBySizeOfInnerStrings(ArrayList<String> source, int size) {
        List<List<String>> result = new ArrayList<>();
        int currentSize = 0;
        List<String> currentPack = new ArrayList<>();
        for (String current : source) {
            if (currentSize + current.length() >= size) {
                result.add(currentPack);
                currentPack = new ArrayList<>();
                currentSize = 0;
            } else {
                currentPack.add(current);
                currentSize += current.length();
            }
        }

        if (currentPack.size() > 0)
            result.add(currentPack);

        return result;
    }
}
