package ru.digitalleague.telegram;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.TimeZone;

/**
 * @author Repkin Andrey {@literal <arepkin@at-consulting.ru>}
 */
public class BotMain {
    public static final String REMIND_CRON = "0 59 8 ? * MON-FRI";
    public static final String REPEAT_CRON = "0 27 10 ? * MON-FRI";
    public static final String DEBUG_CRON = "0 02 12 ? * MON-FRI"; //"0 0/1 0 ? * * *"; //каждую 1 минуту
    public static final String JOB_GROUP = "group";

    public static void main(String[] args) throws Exception {
        try {
            System.out.println("Initializing API context...");
            ApiContextInitializer.init();

            TelegramBotsApi botsApi = new TelegramBotsApi();

            System.out.println("Configuring bot options...");
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            System.out.println("Registering Anonymizer...");
            DinnerBot bot = new DinnerBot(botOptions);
            startReminder(bot);
            botsApi.registerBot(bot);

            System.out.println("Anonymizer bot is ready for work!");

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
            System.out.println("Error while initializing bot!");
        }
    }

    private static void startReminder(Configurable bot) throws SchedulerException {

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        remindJob(bot, scheduler);
        repeatJob(bot, scheduler);
    }

    private static void remindJob(Configurable bot, Scheduler scheduler) throws SchedulerException {
        JobDataMap newJobDataMap = new JobDataMap();
        newJobDataMap.put("bot", bot);
        JobDetail job = JobBuilder.newJob(RemindJob.class).withIdentity("dinnerJob", JOB_GROUP)
                .usingJobData(newJobDataMap).build();
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("cronTrigger", JOB_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(REMIND_CRON).inTimeZone(timeZone)).build();
        scheduler.scheduleJob(job, trigger);
    }

    private static void repeatJob(Configurable bot, Scheduler scheduler) throws SchedulerException {
        JobDataMap newJobDataMap = new JobDataMap();
        newJobDataMap.put("bot", bot);
        JobDetail job = JobBuilder.newJob(RepeatJob.class).withIdentity("repeatDinnerJob", JOB_GROUP)
                .usingJobData(newJobDataMap).build();
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("repeatCronTrigger", JOB_GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(REPEAT_CRON).inTimeZone(timeZone)).build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class RemindJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            DinnerBot bot = (DinnerBot) context.getMergedJobDataMap().get("bot");
            bot.sendRemind();
        }
    }

    public static class RepeatJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
            DinnerBot bot = (DinnerBot) context.getMergedJobDataMap().get("bot");
            bot.sendRepeatRemind();
        }
    }
}
