package ru.digitalleague.telegram.cmd;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.ICommandRegistry;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.digitalleague.telegram.Configurable;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class ConfigCommand extends BotCommand {
    private final Configurable configurableChat;

    public ConfigCommand(Configurable configurableChat) {
        super("config", "update config\n");
        this.configurableChat = configurableChat;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        configurableChat.configChat(chat.getId());
    }
}
