# Notes

* ~~Framework should be able to handle multiple different bots, each with its own identity and credentials!~~
* Getting channel history MUST be limited by date!
  (avoid fetching years worth of data)
* ~~TaskManager~~
  * ~~to manage background tasks like:~~
    * ~~leader election task~~
    * ~~login task(s) to prevent token(s) from expiring per bot~~
    * ~~join public channel task(s) per bot~~
      * ~~Ask bot if it wants to join channel?~~
* MongoDb to give bots persistent storage
  * remember channels a bot was banned from?
  * store reminders (remindme bot)
  * store votes (vote bot)
  * Phrasenschwein-Bot? :) (can be taught annoying phrases and talk down on people using them...)
* Bot ideas
  * Welcome bot:
    * Is initialized with a welcome message and channels the bot should auto-join, e.g. #general
    * Should introduce users to other bots and link to more docs.
    * Can be invited into other channels.
    * Can be taught welcome messages by the admin/moderator of that channel
  * JIRA issue bot:
    * Auto-joins all channels.
    * Checks every incoming message for a JIRA issue pattern: "[A-Z]+\-[0-9]+", e.g. OTA-4711
      * Tries to look up the issue via JIRA REST api. If found posts a message containing the link to the issue as well as some additional info (title, assignee, type, priority, state, description, ...)
  * JIRA webhook bot
    * NO auto-join (but #general?)
    * Watches a special/private JIRA-webhook-channel.
      * This channel is fed by JIRA itself about all issue updates.
      * The bot can be invited to additional channels.
      * Bot can be instructed to relay issue updates to channels or DMs.
        * Filtering must be available at least by project! Possibly more, like by assignee, etc.
      * Bot will clean up history every day or so.
  * BitBucket webhook bot
    * NO auto-join (but #general?)
    * Watches a special/private Bitbucket-webhook-channel.
      * This channel is fed by Bitbucket itself about all events (commits / branches / PRs)
      * The bot can be invited to additional channels.
      * Bot can be instructed to relay events to channels or DMs.
        * Filtering must be available at least by project or repository! Possibly more, like by commit author, etc.
      * Bot will clean up history every day or so.
  * Jenkins bot?
  * Web-Update-Bot (aka Info.net-Bot?) :)
    * Repeatedly scans a given Website (login?) possibly using REGEX to identify articles.
    * Keeps a database of past articles.
    * When it finds a new article, it posts a message into a configured channel (e.g. #general) about the update.
  * RSS-Bot
  * OpenShift-Ops-Bot?
  * Linux-Ops-Bot?

## Misc

* Realtime subscriptions
  * Do they end automatically, when a bot is kicked from a room?

* Subscriptions-Tracker-Task
  * Repeatedly get all subscriptions for a bot via REST. Start realtime subs for each new REST subscription and stop removed ones.
