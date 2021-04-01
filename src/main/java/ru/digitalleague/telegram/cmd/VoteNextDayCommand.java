package ru.digitalleague.telegram.cmd;

import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.digitalleague.telegram.Configurable;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class VoteNextDayCommand extends BotCommand {
    private final Configurable configurableChat;

    public VoteNextDayCommand(Configurable configurableChat) {
        super("next_day", "Vote about next day\n");
        this.configurableChat = configurableChat;
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        configurableChat.sendRemind();
    }
}
