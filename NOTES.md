# Notes

* ~~Framework should be able to handle multiple different bots, each with its own identity and credentials!~~
* Getting channel history MUST be limited by date!
  (avoid fetching years worth of data)
* ~~TaskManager~~
  * ~~to manage background tasks like:~~
    * ~~leader election task~~
    * ~~login task(s) to prevent token(s) from expiring per bot~~
    * join public channel task(s) per bot
      * Ask bot if it wants to join channel?
* MongoDb to give bots persistent storage
  * remember channels a bot was banned from?
  * store reminders (remindme bot)
  * store votes (vote bot)
  * Phrasenschwein-Bot? :) (can be taught annoying phrases and talk down on people using them...)

## Misc

* Realtime subscriptions
  * Do they end automatically, when a bot is kicked from a room?
  * Room tracker task: offers all new public rooms to each bot.
    * Knows its bot and will allways do two queries: get all joined rooms for the bot, get all public channels.

* Subscriptions-Tracker-Task
  * Upon first run just notify about *all* subscriptions! This way the BotManager can start realtime-subs for each.
  * Get all subscriptions (via REST) and all public channels (via REST). Offer channels not yet subscribed to, to the bot and add REST subscription when necessary. Inform BotManager about all new Subscriptions

  * Repeatedly get all subscriptions for a bot via REST. Start realtime subs for each new REST subscription and stop removed ones.
