Bot:

http://t.me/phosagro_liga_bot

https://habr.com/ru/post/432548/

Чтобы обратиться к боту в чате с командой config, надо:
1) Добавить ему прав
Sequence within a @BotFather chat:
You: /setprivacy
BotFather: Choose a bot to change group messages settings.
You: @your_name_bot
BotFather: 'Enable' - your bot will only receive messages that either start with the '/' symbol or mention the bot by username.
'Disable' - your bot will receive all messages that people send to groups.
Current status is: ENABLED
You: Disable
BotFather: Success! The new status is: DISABLED. /help
2) Обратиться: /config@phosagro_liga_bot

Запуск на Debian:
nohup java -Dbot.token=<token> -jar bot.jar 2>&1 &
