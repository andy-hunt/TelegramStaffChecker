package ru.digitalleague.telegram;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.digitalleague.telegram.cmd.ConfigCommand;
import ru.digitalleague.telegram.cmd.HelpCommand;
import ru.digitalleague.telegram.cmd.RemindCommand;
import ru.digitalleague.telegram.cmd.RepeatCommand;
import ru.digitalleague.telegram.work.Location;
import ru.digitalleague.telegram.work.WorkInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class DinnerBot extends TelegramLongPollingCommandBot implements Configurable {
    private static final String BOT_NAME = "phosagro_liga_bot";

    private Set<Long> chatIds = new HashSet<>();
    private Map<Long, WorkInfo> workInfoMap = Maps.newHashMap();
    private Set<WorkInfo.PersonTodayInfo> allPersons = Sets.newHashSet();

    public DinnerBot(DefaultBotOptions options) {
        super(options);

        HelpCommand helpCommand = new HelpCommand( this);
        register(helpCommand);
        register(new ConfigCommand( this));
        register(new RemindCommand( this));
        register(new RepeatCommand( this));

        registerDefaultAction(((absSender, message) -> {
            SendMessage text = new SendMessage();
            text.setChatId(message.getChatId());
            text.setText(message.getText() + " command not found!");

            try {
                absSender.execute(text);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }


            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[] {});
        }));
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        Message message = update.getMessage();
        Long chatId = message.getChatId();
        if (message.getChat().isGroupChat()) {
            if (BOT_NAME.equals(Optional.ofNullable(message.getLeftChatMember()).map(User::getUserName).orElse(null))) {
                removeFromChat(chatId);
            } else if (Optional.ofNullable(message.getNewChatMembers()).map(List::stream).orElse(Stream.<User>empty())
                    .filter(n -> BOT_NAME.equals(n.getUserName())).findAny().isPresent()) {
                configChat(chatId);
            } else if (message.getText() != null && chatIds.contains(message.getChatId())) {
                WorkInfo workInfo = workInfoMap.get(message.getChatId());
                workInfo.onTextMessage(message, this);
                this.allPersons.addAll(workInfo.getPersonsMap().values());
            }
        }
//        sendTextMsg(chatId, "I will remind you about work! And about dinner)");
    }

    public void removeFromChat(Long chatId) {
        chatIds.remove(chatId);
        workInfoMap.remove(chatId);
    }

    @Override
    public void configChat(Long chatId) {
        chatIds.add(chatId);
        workInfoMap.put(chatId, new WorkInfo(chatId, null));
    }

    @Override
    public void editMessage(EditMessageText editMessageTextCmd) {
        try {
            execute(editMessageTextCmd);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendRemind() {
        for (Long chatId : chatIds) {
            Message message = sendTextMsg(chatId, "What happend today?\n " + StringUtils.join(Location.values(), ", " + "\n"));
            workInfoMap.put(chatId, new WorkInfo(chatId, message));
        }
    }

    @Override
    public void sendRepeatRemind() {
        for (WorkInfo workInfo : workInfoMap.values()) {
            workInfo.editMessage(this, allPersons);
        }
        for (Long chatId : chatIds) {
            sendTextMsg(chatId, "A minute later, the daily begins. Do not forget to connect via Skype!");
        }
    }

    @Override
    public String getBotToken() {
        return System.getProperty("bot.token");
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    private Message sendTextMsg(Long chatId, String msg) {
        try {
            return execute(new SendMessage(chatId, msg));
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return null;
        }
    }
}
