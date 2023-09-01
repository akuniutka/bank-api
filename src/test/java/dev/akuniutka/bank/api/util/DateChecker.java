package dev.akuniutka.bank.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateChecker {
    private static final SimpleDateFormat DATE_FROM_JSON = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static boolean isDateBetween(Object o, Date start, Date finish) {
        Date date;
        if (o instanceof String) {
            try {
                date = DATE_FROM_JSON.parse((String) o);
            } catch (ParseException e) {
                throw new IllegalArgumentException("wrong date format");
            }
        } else if (o instanceof Date) {
            date = (Date) o;
        } else {
            throw new IllegalArgumentException("wrong argument type");
        }
        if (date == null || start == null || finish == null || start.compareTo(date) * date.compareTo(finish) < 0) {
            throw new IllegalArgumentException("not between");
        }
        return true;
    }
}
