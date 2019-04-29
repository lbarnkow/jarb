/*
 *    jarb is a framework and collection of Rocket.Chat bots written in Java.
 *
 *    Copyright 2019 Lorenz Barnkow
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Main class for jarb (contains main method).
 *
 * @author lbarnkow
 */
@Slf4j
public final class Main {
  private Main() {
  }

  /**
   * Main code entry point for jarb.
   *
   * @param args CLI args (currently not used)
   */
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

    return validateConfiguration(result);
  }

  private static BotManagerConfiguration validateConfiguration(BotManagerConfiguration config) {
    // Bot names must be unique!
    HashSet<String> names = new HashSet<>();
    config.getBots().forEach(botConfig -> names.add(botConfig.getName()));
    if (names.size() != config.getBots().size()) {
      throw new IllegalArgumentException(
          "Configuration error: Every bot must have a unique 'name'!");
    }

    return config;
  }

  private static Bot[] createBots(BotManagerConfiguration config, Injector guice) {
    val bots = new ArrayList<Bot>();

    config.getBots().forEach(botConfig -> {
      try {
        val clazz = loadClass(botConfig);
        val bot = guice.getInstance(clazz);
        bot.initialize(botConfig.getName(), botConfig.getCredentials().getUsername());
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
