const mineflayer = require('mineflayer');
const bot = mineflayer.createBot({
    username: "reloader",
    host: "mc.pegasis.site"
});
bot.on("login", () => {
    bot.settings.chat = "commandsOnly"
    bot.chat("/reload confirm");
    setTimeout(() => {
        bot.quit()
    }, 7000)
})
bot.on("chat", (name, msg) => {
    console.log(msg)
})
