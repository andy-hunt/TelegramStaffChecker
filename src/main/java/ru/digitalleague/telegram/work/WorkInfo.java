package ru.digitalleague.telegram.work;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.digitalleague.telegram.Configurable;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class WorkInfo {
    private Message remindMessage;
    private Long channelId;
    private Map<Integer, PersonTodayInfo> personsMap = Maps.newHashMap();
    private Set<PersonTodayInfo> allPersons = null;

    public WorkInfo(Long channelId, Message remindMessage) {
        this.channelId = channelId;
        this.remindMessage = remindMessage;
    }

    public WorkInfo onTextMessage(Message message, Configurable bot) {
        User userFrom = message.getFrom();
        Integer userId = userFrom.getId();
        String msg = message.getText();
        Optional<Location> location = Location.findByCode(msg);
        if (location.isPresent()) {
            personsMap.put(userId, new PersonTodayInfo(userId, userFrom, location.get()));
            editMessage(bot);
        }
        return this;
    }

    public void editMessage(Configurable bot) {
        editMessage(bot, null);
    }

    public void editMessage(Configurable bot, Set<PersonTodayInfo> allPersonsNew) {
        if (remindMessage != null) {
            allPersons = allPersonsNew == null ? allPersons : allPersonsNew;
            String oldText = remindMessage.getText();
            String newText = oldText + "\n";
            Map<Location, List<PersonTodayInfo>> locationMap = Maps.newHashMap();
            for (PersonTodayInfo personTodayInfo : personsMap.values()) {
                List<PersonTodayInfo> personTodayInfos = locationMap.get(personTodayInfo.getCurrentLocation());
                if (personTodayInfos == null) {
                    personTodayInfos = Lists.newArrayList();
                    locationMap.put(personTodayInfo.getCurrentLocation(), personTodayInfos);
                }
                personTodayInfos.add(personTodayInfo);
            }
            for (Map.Entry<Location, List<PersonTodayInfo>> entry : locationMap.entrySet()) {
                Location location = entry.getKey();
                String personTodayInfos = entry.getValue().stream().map(v -> username(v)).collect(Collectors.joining(", "));
                newText += MessageFormat.format("In {0} working today: {1} \n", location.getCode(), personTodayInfos);
            }
            if (allPersons != null) {
                Sets.SetView<PersonTodayInfo> lostUsers = Sets.difference(allPersons, Sets.newHashSet(personsMap.values()));
                if (!lostUsers.isEmpty()) {
                    String lostUsersStr = lostUsers.stream().map(PersonTodayInfo::getUsername).collect(Collectors.joining(", "));
                    newText += MessageFormat.format("Lost people: {0} \n", lostUsersStr);
                } else {
                    newText += "No one is lost! \n";
                }
            }
            EditMessageText newMessageText = new EditMessageText();
            newMessageText.setChatId(remindMessage.getChatId());
            newMessageText.setMessageId(remindMessage.getMessageId());
            newMessageText.setText(newText);
            bot.editMessage(newMessageText);
        }
    }

    public static String username(PersonTodayInfo v) {
        return v.getUsername();
    }

    public Map<Integer, PersonTodayInfo> getPersonsMap() {
        return personsMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkInfo workInfo = (WorkInfo) o;
        return Objects.equals(channelId, workInfo.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId);
    }

    public static class PersonTodayInfo {
        private Integer memberId;
        private String username;
        private Location currentLocation;

        public PersonTodayInfo(Integer memberId, User user, Location currentLocation) {
            this.memberId = memberId;
            String name = user.getUserName() != null ? "@" + user.getUserName() : "";
            if (StringUtils.isEmpty(name)) {
                name = user.getFirstName();
                if (StringUtils.isNotEmpty(user.getLastName())) {
                    name += user.getLastName();
                }
            }
            this.username = name;
            this.currentLocation = currentLocation;
        }

        public Integer getMemberId() {
            return memberId;
        }

        public String getUsername() {
            return username;
        }

        public Location getCurrentLocation() {
            return currentLocation;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PersonTodayInfo that = (PersonTodayInfo) o;
            return Objects.equals(getMemberId(), that.getMemberId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getMemberId());
        }
    }

}
