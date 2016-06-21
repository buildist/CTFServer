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

  /**
   * The configuration instance.
   */
  private static Configuration configuration;
  /**
   * The filename of the map file.
   */
  private static String mapFilename;
  /**
   * The server name.
   */
  private String name;
  private String longName;
  /**
   * The server MOTD.
   */
  private String message;
  /**
   * The maximum allowed player count.
   */
  private int maximumPlayers;
  /**
   * The radius of a sponge's effectiveness.
   */
  private int spongeRadius;
  /**
   * Public server flag.
   */
  private boolean publicServer;
  /**
   * Verify names flag.
   */
  private boolean verifyNames;
  /**
   * The game mode.
   */
  private String gameMode;
  /**
   * The script name.
   */
  private String scriptName;
  private String statsPostURL;
  private String welcomeMessage;
  private int rankLimit;
  private String ircServer;
  private String ircChannel;
  private String ircName;
  private boolean premium;
  private String envTexturePack;
  private boolean test = false;

  /**
   * Creates the configuration from the specified properties object.
   *
   * @param props The properties object.
   */
  public Configuration(Properties props) {
    test = Boolean.valueOf(props.getProperty("test", "false"));
    name = props.getProperty("name", "OpenCraft Server");
    longName = props.getProperty("longName", "OpenCraft Server");
    message = props.getProperty("message", "http://opencraft.sf.net/");
    maximumPlayers = Integer.valueOf(props.getProperty("max_players", "16"));
    publicServer = Boolean.valueOf(props.getProperty("public", "false"));
    verifyNames = Boolean.valueOf(props.getProperty("verify_names", "false")) /*&& !test*/;
    mapFilename = props.getProperty("filename", "server_level.dat");
    spongeRadius = Integer.valueOf(props.getProperty("sponge_radius", "2"));
    gameMode = props.getProperty("game_mode", CTFGameMode.class.getName());
    scriptName = props.getProperty("script_name", null);
    statsPostURL = props.getProperty("statsPostURL");
    welcomeMessage = props.getProperty("welcomeMessage");
    rankLimit = Integer.valueOf(props.getProperty("minRank"));
    ircServer = props.getProperty("ircServer");
    ircChannel = props.getProperty("ircChannel");
    ircName = props.getProperty("ircName");
    Constants.PORT = Integer.valueOf(props.getProperty("port"));
    premium = Boolean.valueOf(props.getProperty("premium", "false"));
    envTexturePack = props.getProperty("envTexturePack", Constants.URL_TEXTURE_PACK);
  }

  /**
   * Reads and parses the configuration.
   *
   * @throws FileNotFoundException if the configuration file is not present.
   * @throws IOException           if an I/O error occurs.
   */
  public static void readConfiguration() throws FileNotFoundException, IOException {
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

  public String getLongName() {
    return longName;
  }

  public String getStatsPostURL() {
    return statsPostURL;
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
   * Gets the maximum player count.
   *
   * @return The maximum player count.
   */
  public int getMaximumPlayers() {
    return maximumPlayers;
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
   * Gets the map filename.
   *
   * @return The map's filename.
   */
  public String getMapFilename() {
    return mapFilename;
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

  /**
   * Gets the script name.
   *
   * @return The script name.
   */
  public String getScriptName() {
    return scriptName;
  }

  public int getMinRank() {
    return rankLimit;
  }

  public String getIRCServer() {
    return ircServer;
  }

  public String getIRCChannel() {
    return ircChannel;
  }

  public String getIRCName() {
    return ircName;
  }

  public boolean isPremium() {
    return premium;
  }

  public String getEnvTexturePack() {
    return envTexturePack;
  }

  public boolean isTest() {
    return test;
  }
}
