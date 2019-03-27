package io.github.lbarnkow.jarb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.lbarnkow.jarb.api.Bot;
import io.github.lbarnkow.jarb.api.BotConfiguration;
import io.github.lbarnkow.jarb.misc.GuiceModule;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class Main {
  public static void main(String[] args) {
    log.info("# # # W E L C O M E # # #");

    BotManagerConfiguration config = loadConfig("jarb-config.yaml");

    Injector guice = Guice.createInjector(new GuiceModule());

    Bot[] bots = Main.createBots(config, guice);
    val runtime = guice.getInstance(Runtime.class);
    val botManager = guice.getInstance(BotManager.class);

    runtime.addShutdownHook(new Thread("ShutdownHook") {
      @Override
      public void run() {
        log.info("JVM is shutting down, stopping all thread/services...");
        botManager.stop();
        log.info("All threads/service stopped.");
      }
    });

    botManager.start(config, bots);
  }

  private static BotManagerConfiguration loadConfig(String path) {
    BotManagerConfiguration result = null;
    val file = new File(path);

    if (file.exists()) {
      val mapper = new ObjectMapper(new YAMLFactory());
      try {
        result = mapper.readValue(file, BotManagerConfiguration.class);
      } catch (IOException e) {
        log.error("Failed to read configuration from file '{}'!", path, e);
        System.exit(1);
      }
    } else {
      // just go with the defaults for local development
      result = new BotManagerConfiguration();
    }

    return result;
  }

  private static Bot[] createBots(BotManagerConfiguration config, Injector guice) {
    val bots = new ArrayList<Bot>();

    config.getBots().forEach(botConfig -> {
      try {
        val clazz = loadClass(botConfig);
        val bot = guice.getInstance(clazz);
        bot.initialize(botConfig.getName(), botConfig.getCredentials());
        bots.add(bot);
      } catch (Exception e) {
        log.error("Failed to load, instantiate and initialize the bot '{}'!", botConfig.getName(),
            e);
        System.exit(1);
      }
    });

    return bots.toArray(new Bot[0]);
  }

  @SuppressWarnings("unchecked")
  private static Class<Bot> loadClass(BotConfiguration botConfig) {
    try {
      val clazz = Bot.class.getClassLoader().loadClass(botConfig.getQualifiedClassName());
      if (Bot.class.isAssignableFrom(clazz)) {
        return (Class<Bot>) clazz;
      }

      log.error("Selected bot class '{}' is not a in the type hierachy of '{}'!",
          botConfig.getQualifiedClassName(), Bot.class.getName());
    } catch (ClassNotFoundException e) {
      log.error("Failed to load bot class '{}'!", botConfig.getQualifiedClassName(), e);
    }
    System.exit(1);

    // will never be reached...
    return null;
  }
}
