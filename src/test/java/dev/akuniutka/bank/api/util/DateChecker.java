package dev.akuniutka.bank.api.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class DateChecker {
    public static boolean isDateBetween(Object o, OffsetDateTime start, OffsetDateTime finish) {
        OffsetDateTime date;
        if (o instanceof String) {
            try {
                date = OffsetDateTime.parse((String) o);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("wrong date format");
            }
        } else if (o instanceof OffsetDateTime) {
            date = (OffsetDateTime) o;
        } else {
            throw new IllegalArgumentException("wrong argument type");
        }
        if (start == null) {
            throw new IllegalArgumentException("start date is null");
        }
        if (finish == null) {
            throw new IllegalArgumentException("end date is null");
        }
        if (!date.isEqual(start) && !date.isEqual(finish) && !(date.isAfter(start) && date.isBefore(finish))) {
            throw new IllegalArgumentException("not between");
        }
        return true;
    }
}
