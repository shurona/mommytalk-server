package com.shrona.mommytalk.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class RandomUtils {

    public final static String tempPrefix = "temp";

    public static String generateId(String prefix) {
        String ymd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid6 = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return String.format("%s-%s-%s", prefix, ymd, uuid6);
    }

}
