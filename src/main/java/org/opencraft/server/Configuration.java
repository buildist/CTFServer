/*
 * Jacob_'s Capture the Flag for Minecraft Classic and ClassiCube
 * Copyright (c) 2010-2014 Jacob Morgan
 * Based on OpenCraft v0.2
 *
 * OpenCraft License
 *
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Distribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Distributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Distributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opencraft.server;

import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.EntityID;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages server configuration.
 *
 * @author Graham Edgecombe
 */
public class Configuration {

  /** The configuration instance. */
  private static Configuration configuration;
  /** The server name. */
  private String name;

  /** The server MOTD. */
  private String message;

  /** The radius of a sponge's effectiveness. */
  private int spongeRadius;
  /** Public server flag. */
  private boolean publicServer;
  /** Verify names flag. */
  private boolean verifyNames;
  /** The game mode. */
  private String gameMode;

  private String statsPostURL;
  private String discordURL;
  private String discordToken;
  private String welcomeMessage;
  private boolean test = false;

  /**
   * Creates the configuration from the specified properties object.
   *
   * @param props The properties object.
   */
  public Configuration(Properties props) {
    test = Boolean.valueOf(props.getProperty("test", "false"));
    name = props.getProperty("name", "OpenCraft Server");
    message = props.getProperty("message", "http://opencraft.sf.net/");
    publicServer = Boolean.valueOf(props.getProperty("public", "false"));
    verifyNames = Boolean.valueOf(props.getProperty("verify_names", "false"));
    spongeRadius = Integer.valueOf(props.getProperty("sponge_radius", "2"));
    gameMode = props.getProperty("game_mode", CTFGameMode.class.getName());
    statsPostURL = props.getProperty("statsPostURL");
    discordURL = props.getProperty("discordURL");
    discordToken = props.getProperty("discordToken");
    welcomeMessage = props.getProperty("welcomeMessage");
    Constants.PORT = Integer.valueOf(props.getProperty("port"));
    Constants.WEB_PORT = Integer.valueOf(props.getProperty("webPort"));
    GameSettings.add("MaxPlayers", GameSettings.TYPE_INT, Integer.valueOf(props.getProperty("max_players", "16")));
  }

  /**
   * Reads and parses the configuration.
   *
   * @throws FileNotFoundException if the configuration file is not present.
   * @throws IOException if an I/O error occurs.
   */
  public static void readConfiguration() throws IOException {
    synchronized (Configuration.class) {
      Properties props = new Properties();
      InputStream is = new FileInputStream(Constants.ROOT_PATH + "/opencraft.properties");
      try {
        props.load(is);
        configuration = new Configuration(props);
      } finally {
        is.close();
      }
    }
  }

  /**
   * Gets the configuration instance.
   *
   * @return The configuration instance.
   */
  public static Configuration getConfiguration() {
    synchronized (Configuration.class) {
      return configuration;
    }
  }

  /**
   * Gets the server name.
   *
   * @return The server name.
   */
  public String getName() {
    return name;
  }

  public String getStatsPostURL() {
    return statsPostURL;
  }

  public String getDiscordURL() {
    return discordURL;
  }

  public String getDiscordToken() {
    return discordToken;
  }

  public String getWelcomeMessage() {
    return welcomeMessage;
  }

  /**
   * Gets the server MOTD.
   *
   * @return The server MOTD.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Gets the public server flag.
   *
   * @return The public server flag.
   */
  public boolean isPublicServer() {
    return publicServer;
  }

  /**
   * Gets the verify names flag.
   *
   * @return The verify names flag.
   */
  public boolean isVerifyingNames() {
    return verifyNames;
  }

  /**
   * Gets the range at which a sponge is effective.
   *
   * @return The sponge radius.
   */
  public int getSpongeRadius() {
    return spongeRadius;
  }

  /**
   * Gets the game mode class.
   *
   * @return The game mode class.
   */
  public String getGameMode() {
    return gameMode;
  }

  public boolean isTest() {
    return test;
  }
}
