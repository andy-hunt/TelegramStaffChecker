package ru.digitalleague.telegram.work;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public enum Location {
    SAVELA("Savela"), VORONEZH("Voronezh"), HOME("Home"), SICK("sick"),
    I_DO_NOT_WANT_TO_WORK("I_do_not_want_to_work");
    private String code;

    Location(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static Optional<Location> findByCode(String msg) {
        return Stream.of(values()).filter(v -> StringUtils.containsIgnoreCase(msg, v.code)).findFirst();
    }
}
