# Notes

* Framework should be able to handle multiple
  different bots, each with its own identity
  and credentials!
* Getting channel history MUST be limited by date!
  (avoid fetching years worth of data)
* TaskManager
  * to manage background tasks like:
    * leader election task
    * login task(s) to prevent token(s) from expiring per bot
    * join public channel task(s) per bot
      * Ask bot if it wants to join channel?
* MongoDb to give bots persistent storage
  * remember channels a bot was banned from?
  * store reminders (remindme bot)
  * store votes (vote bot)
  * Phrasenschwein-Bot? :)
