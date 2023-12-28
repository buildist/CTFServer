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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.TexturePackHandler;
import org.opencraft.server.model.World;
import org.opencraft.server.net.ConsoleActionSender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.opencraft.server.model.PlayerUI.prettyTime;


public class WebServer {
  public static ArrayList<String> blockedWords = new ArrayList<String>();
  private static Player consolePlayer = new Player(null, "WebConsole");
  private static ExecutorService executor;

  public static void init() {
    consolePlayer.setActionSender(new ConsoleActionSender());
    consolePlayer.setAttribute("IsOperator", "true");
    consolePlayer.setAttribute("IsOwner", "true");

    try {
      InetSocketAddress addr = new InetSocketAddress(Constants.WEB_PORT);
      HttpServer server = HttpServer.create(addr, 0);

      CTFHandler ch = new CTFHandler();
      GameHandler gh = new GameHandler();
      PlayerHandler ph = new PlayerHandler();
      KillsHandler kh = new KillsHandler();

      HttpContext c = server.createContext("/", ch);
      c.getFilters().add(new ParameterFilter());

      HttpContext g = server.createContext("/api/game", gh);
      g.getFilters().add(new ParameterFilter());

      HttpContext p = server.createContext("/api/player", ph);
      p.getFilters().add(new ParameterFilter());

      HttpContext k = server.createContext("/api/kills", kh);
      k.getFilters().add(new ParameterFilter());

      executor = Executors.newCachedThreadPool();
      server.setExecutor(executor);
      server.start();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void run(Runnable r) {
    if (!Configuration.getConfiguration().isTest()) executor.submit(r);
  }

  public static void sendDiscordMessage(String message, String username) {
    if(executor == null) return;
    executor.submit(new Runnable() {
      @Override
      public void run() {
        String urlString = Configuration.getConfiguration().getDiscordURL();
        if (urlString.equals("null")) {
          return;
        }

        try {
          URL url = new URL(Configuration.getConfiguration().getDiscordURL());
          HttpURLConnection con = (HttpURLConnection) url.openConnection();
          con.setRequestMethod("POST");
          con.setDoInput(true);
          con.setDoOutput(true);
          StringBuilder data = new StringBuilder();

          data.append("content=").append(URLEncoder.encode(message, "UTF-8"));
          if (username != null) {
            data.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
            data.append("&avatar_url=").append(URLEncoder.encode("https://www.classicube.net/face/"
                + URLEncoder.encode(username, "UTF-8") + ".png", "UTF-8"));
          }

          byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
          con.setFixedLengthStreamingMode(bytes.length);
          con.setRequestProperty(
              "Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
          con.setRequestProperty(
              "User-Agent",
              "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
          con.connect();

          try (OutputStream out = con.getOutputStream()) {
            out.write(bytes);
          }
        } catch (IOException ex) {
          Server.log(ex);
        }
      }
    });
  }

  static class CTFHandler implements HttpHandler {

    public void handle(HttpExchange exchange) {
      try {
        Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
        if (params.containsKey("map")) {
          String map = params.get("map").toString();
          TexturePackHandler.createPatchedTexturePack(map);
          String filename = "terrain_" + map + ".zip";
          File texturePackFile = new File("texturepacks_cache/" + filename);
          Path texturePackPath = texturePackFile.toPath();

          Headers responseHeaders = exchange.getResponseHeaders();
          responseHeaders.set("Access-Control-Allow-Origin", "*");
          responseHeaders.set("Content-Type", "application/zip");
          responseHeaders.set("Content-Disposition", "attachment; filename=" + filename);
          responseHeaders.set("Last-Modified", formatDate(texturePackFile.lastModified()));
          String md5 = md5(texturePackPath);
          responseHeaders.set("ETag", md5);

          long fileSize = texturePackFile.length();
          if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(200, -1);
            responseHeaders.set("Content-Length", Long.toString(fileSize));
          } else if (exchange.getRequestHeaders().containsKey("If-None-Match")
                && exchange.getRequestHeaders().get("If-None-Match").get(0).equals(md5)) {
            exchange.sendResponseHeaders(304, -1);
          } else {
            exchange.sendResponseHeaders(200, fileSize);
            Files.copy(texturePackPath, exchange.getResponseBody());
          }
          exchange.close();
        }
        if (params.containsKey("x")) {
          if (!params.containsKey("k")
              || !params.get("k").equals(Integer.toString(Constants.SECRET))) {
            exchange.close();
            return;
          }
          String message = Server.cleanColorCodes(params.get("x").toString());
          String messageLower = message.toLowerCase();
          for (String word : blockedWords) {
            if (messageLower.contains(word)) {
              exchange.close();
              return;
            }
          }
          if (message.equals("stop")) {
            Server.stop();
          } else if (message.startsWith("/")) {
            // interpret as command
            String tokens = message.substring(1);
            String[] parts = tokens.split(" ");
            final Map<String, Command> commands = World.getWorld().getGameMode().getCommands();
            Command c = commands.get(parts[0]);
            if (c != null) {
              parts[0] = null;
              List<String> partsList = new ArrayList<String>();
              for (String s : parts) {
                if (s != null) {
                  partsList.add(s);
                }
              }
              parts = partsList.toArray(new String[0]);
              c.execute(consolePlayer, new CommandParameters(parts));
            } else {
              System.out.println("Invalid command.");
            }
          } else {
            World.getWorld().broadcast("(Console) &e" + message);
            System.out.println(message);
          }
          exchange.sendResponseHeaders(200, 0);
          exchange.close();
        } else if (params.containsKey("t")) {
          long time = Long.parseLong(params.get("t").toString());
          Headers responseHeaders = exchange.getResponseHeaders();
          responseHeaders.set("Content-Type", "text/plain");
          responseHeaders.set("Access-Control-Allow-Origin", "*");
          exchange.sendResponseHeaders(200, 0);
          exchange.getResponseBody().write(Server.getConsoleMessages(time).getBytes());
          exchange.close();
        } else {
          exchange.close();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        exchange.close();
      }
    }

    private String formatDate(long date) {
      SimpleDateFormat dateFormat = new SimpleDateFormat(
          "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
      dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
      return dateFormat.format(date);
    }

    private String md5(Path path) {
      try {
        byte[] b = Files.readAllBytes(path);
        return bytesToHex(MessageDigest.getInstance("MD5").digest(b));
      } catch (Exception ex) {
        return "";
      }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];
      for (int j = 0; j < bytes.length; j++) {
        int v = bytes[j] & 0xFF;
        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
      }
      return new String(hexChars);
    }
  }

  static class GameHandler implements HttpHandler {
    public void handle(HttpExchange exchange) {
      try {
        if ("GET".equals(exchange.getRequestMethod())) {
          String map = World.getWorld().getLevel().id;

          long elapsedTime = System.currentTimeMillis() - World.getWorld().getGameMode().gameStartTime;

          String timerSetting = null;
          if (CTFGameMode.getMode() == Level.TDM) {
            timerSetting = "TDMTimeLimit";
          } else {
            timerSetting = "TimeLimit";
          }

          long remaining = Math.max((GameSettings.getInt(timerSetting) * 60 * 1000 - elapsedTime) / 1000, 0);
          if (World.getWorld().getGameMode().voting) {
            remaining = 0;
          } else if (!World.getWorld().getGameMode().tournamentGameStarted) {
            remaining = GameSettings.getInt(timerSetting) * 60;
          }

          String timeRemaining = prettyTime((int) remaining);

          int redCaptures = CTFGameMode.redCaptures;
          int blueCaptures = CTFGameMode.blueCaptures;

          // Construct JSON using a StringBuilder
          StringBuilder respTextBuilder = new StringBuilder();
          respTextBuilder.append("{\n");
          respTextBuilder.append("  \"map\": ").append("\"" + map + "\"").append(",\n");
          respTextBuilder.append("  \"timeRemaining\": ").append("\"" + timeRemaining + "\"").append(",\n");
          respTextBuilder.append("  \"redCaptures\": ").append(redCaptures).append(",\n");
          respTextBuilder.append("  \"blueCaptures\": ").append(blueCaptures).append(",\n");

          // Add red/blue players

          Player[] names = World.getWorld().getPlayerList().getPlayers().toArray(new Player[0]);

          List<Player> redTeam = new ArrayList<>();
          List<Player> blueTeam = new ArrayList<>();

          for (Player other : names) {
            if (other.team == 0) redTeam.add(other);
            if (other.team == 1) blueTeam.add(other);
          }

          String redPlayersJSON = getPlayersJSON(redTeam);
          String bluePlayersJSON = getPlayersJSON(blueTeam);

          respTextBuilder.append("  \"redPlayers\": [").append(redPlayersJSON).append("],\n");
          respTextBuilder.append("  \"bluePlayers\": [").append(bluePlayersJSON).append("]\n");

          respTextBuilder.append("}");

          String respText = respTextBuilder.toString();
          Headers responseHeaders = exchange.getResponseHeaders();
          responseHeaders.set("Access-Control-Allow-Origin", "*");

          exchange.sendResponseHeaders(200, respText.getBytes().length);
          OutputStream output = exchange.getResponseBody();
          output.write(respText.getBytes());
          output.flush();
        } else {
          exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
        exchange.close();
      } catch (Exception ex) {
        ex.printStackTrace();
        exchange.close();
      }
    }

    private String getPlayersJSON(List<Player> players) {
      List<String> usernames = new ArrayList<>();
      for (Player player : players) {
        usernames.add("\"" + player.getName() + "\"");
      }
      return String.join(", ", usernames);
    }

  }

  public static ArrayList<GameMode.KillFeedItem> killFeed; // A copy of CTFGameMode.killFeed that does not purge after 10 seconds

  static class PlayerHandler implements HttpHandler {
    public void handle(HttpExchange exchange) {
      try {
        if ("GET".equals(exchange.getRequestMethod())) {
          Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
          StringBuilder respTextBuilder = new StringBuilder();
          respTextBuilder.append("{\n");

          String username;
          if (params.containsKey("p")) {
            username = params.get("p").toString();

            Player target = Player.getPlayer(username, null);
            if (target == null) {
              respTextBuilder.append("  \"error\": ").append("\"Player not found.\"").append("\n");
              respTextBuilder.append("}");
            } else {
              int kills = target.kills;
              int deaths = target.deaths;
              int captures = target.captures;
              int points = target.getPoints();

              respTextBuilder.append("  \"username\": ").append("\"" + username + "\"").append(",\n");
              respTextBuilder.append("  \"kills\": ").append(kills).append(",\n");
              respTextBuilder.append("  \"deaths\": ").append(deaths).append(",\n");
              respTextBuilder.append("  \"captures\": ").append(captures).append(",\n");
              respTextBuilder.append("  \"points\": ").append(points).append("\n");
              respTextBuilder.append("}");
            }
          } else {
            respTextBuilder.append("  \"error\": ").append("\"Player not found.\"").append("\n");
            respTextBuilder.append("}");
          }

          String respText = respTextBuilder.toString();
          Headers responseHeaders = exchange.getResponseHeaders();
          responseHeaders.set("Access-Control-Allow-Origin", "*");

          exchange.sendResponseHeaders(200, respText.getBytes().length);
          OutputStream output = exchange.getResponseBody();
          output.write(respText.getBytes());
          output.flush();
        } else {
          exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
        exchange.close();
      } catch (Exception ex) {
        ex.printStackTrace();
        exchange.close();
      }
    }
  }

  static class KillsHandler implements HttpHandler {
    public void handle(HttpExchange exchange) {
      try {
        if ("GET".equals(exchange.getRequestMethod())) {
          StringBuilder respTextBuilder = new StringBuilder();

          respTextBuilder.append("{\n");

          if (killFeed == null) {
            respTextBuilder.append("  \"1\": null").append(",\n");
            respTextBuilder.append("  \"2\": null").append(",\n");
            respTextBuilder.append("  \"3\": null").append("\n");
          } else {
            for (int i = 0; i < 3; i++) {
              System.out.println("kf size " + killFeed.size());
              System.out.println("i " + i);
              if (i >= killFeed.size()) {
                respTextBuilder.append("  \"" + i + "\": null");
              } else {
                respTextBuilder.append("  \"" + i + "\": ").append(killFeed.get(i).getMessage());
              }

              if (i < 2) {
                respTextBuilder.append(",\n");
              } else {
                respTextBuilder.append("\n");
              }
            }
          }

          respTextBuilder.append("}");

          String respText = respTextBuilder.toString();
          Headers responseHeaders = exchange.getResponseHeaders();
          responseHeaders.set("Access-Control-Allow-Origin", "*");

          exchange.sendResponseHeaders(200, respText.getBytes().length);
          OutputStream output = exchange.getResponseBody();
          output.write(respText.getBytes());
          output.flush();
        } else {
          exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
        }
        exchange.close();
      } catch (Exception ex) {
        ex.printStackTrace();
        exchange.close();
      }
    }
  }
}
