package ru.digitalleague.telegram;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public interface Configurable {
    void configChat(Long chatId);
    void editMessage(EditMessageText editMessageTextCmd);
    void sendRemind();

    void sendRepeatRemind();
}
