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
 *       notice, this list of conditions and the following disclaimer in thedebug
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

import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.MapController;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Store;
import org.opencraft.server.model.World;
import org.opencraft.server.net.SessionHandler;
import org.opencraft.server.task.TaskQueue;
import org.opencraft.server.task.impl.CTFProcessTask;
import org.opencraft.server.task.impl.ConsoleTask;
import org.opencraft.server.task.impl.HeartbeatTask;
import org.opencraft.server.task.impl.ItemDropTask;
import org.opencraft.server.task.impl.MessageTask;
import org.opencraft.server.task.impl.PingTask;
import org.opencraft.server.task.impl.UpdateTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

/**
 * The core class of the OpenCraft server.
 *
 * @author Graham Edgecombe
 */
public final class Server {

  public static Random random = new Random();
  public static String log = "";
  public static ArrayList<String> rulesText = new ArrayList<String>(20);
  private static Store store;
  private static ArrayList<String> ipBans = new ArrayList<String>(128);
  private static ArrayList<String> mutes = new ArrayList<String>(128);
  private static ArrayList<String> whitelist = new ArrayList<String>(128);
  private static Server instance;

  private static LinkedList<ConsoleMessage> messages = new LinkedList<ConsoleMessage>();
  /** The socket acceptor. */
  private final NioSocketAcceptor acceptor = new NioSocketAcceptor();

  /**
   * Creates the server.
   *
   * @throws IOException if an I/O error occurs.
   * @throws FileNotFoundException if the configuration file is not found.
   */
  public Server() throws FileNotFoundException, IOException {
    log("Starting OpenCraft server...");
    Configuration.readConfiguration();

    FileInputStream ipFile = new FileInputStream("ipbans.txt");
    BufferedReader r = new BufferedReader(new InputStreamReader(ipFile));
    String l;
    while ((l = r.readLine()) != null) {
      ipBans.add(l);
    }

    FileInputStream muteFile = new FileInputStream("mutes.txt");
    r = new BufferedReader(new InputStreamReader(muteFile));
    while ((l = r.readLine()) != null) {
      mutes.add(l);
    }

    FileInputStream rulesFile = new FileInputStream("rules.txt");
    r = new BufferedReader(new InputStreamReader(rulesFile));
    l = null;
    while ((l = r.readLine()) != null) {
      rulesText.add(l);
    }

    FileInputStream whitelistFile = new FileInputStream("whitelist.txt");
    r = new BufferedReader(new InputStreamReader(rulesFile));
    l = null;
    while ((l = r.readLine()) != null) {
      whitelist.add(l);
    }

    GameSettings.load();

    MapController.create();
    log("Creating world...");
    World.getWorld();
    acceptor.setHandler(new SessionHandler());
    acceptor.getSessionConfig().setTcpNoDelay(true);
    TaskQueue.getTaskQueue().schedule(new UpdateTask());
    TaskQueue.getTaskQueue().schedule(new CTFProcessTask());
    TaskQueue.getTaskQueue().schedule(new HeartbeatTask());
    TaskQueue.getTaskQueue().schedule(new MessageTask());
    TaskQueue.getTaskQueue().schedule(new PingTask());
    new Thread(new ConsoleTask()).start();
    new Thread(new ItemDropTask()).start();
    //if (!Configuration.getConfiguration().isTest()) {
      new Thread(new DiscordBot()).start();
    //}
    log("Initializing game...");
  }

  /** The entry point of the server application. */
  public static void log(Throwable e) {
    Server.log("[E] Exception occured: " + e.toString());
    if (e.getStackTrace() != null) {
      for (StackTraceElement s : e.getStackTrace()) {
        Server.log("[E]" + s.toString());
      }
    }
    if (e.getCause() != null) {
      Server.log("[E] Caused by: ");
      log(e.getCause());
    }
  }

  public static void main(String[] args) {
    Thread.setDefaultUncaughtExceptionHandler(
        new UncaughtExceptionHandler() {

          @Override
          public void uncaughtException(Thread t, Throwable e) {
            try {
              if (!(e instanceof IndexOutOfBoundsException
                  || e instanceof ThreadDeath
                  || e instanceof InterruptedException)) {
                log(e);
              }
            } catch (Exception ex) {

            }
          }
        });
    try {
      instance = new Server();
      instance.start();
    } catch (Throwable t) {
      log("[E] An error occurred whilst loading the server.");
      log(t);
    }
  }

  public static void stop() {
    instance.acceptor.unbind();
    System.exit(0);
  }

  public static Server getServer() {
    return instance;
  }

  public static String date() {
    DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dfm.format(new Date());
  }

  public static void log(String line) {
    String text = "[" + date() + "] " + line;
    System.out.println(text);
    log += text + "\n";
    messages.addFirst(new ConsoleMessage(new Date(), text));
    if (messages.size() == 50) {
      messages.removeLast();
    }
  }

  public static void d(String line) {
    if (GameSettings.getBoolean("Debug")) {
      String text = "[" + date() + " DEBUG] " + line;
      System.out.println(text);
      log += text + "\n";
    }
  }

  public static void saveLog() {
    try {
      PrintWriter w = new PrintWriter(new FileWriter(new File("./server.log"), true));
      w.append(log);
      w.flush();
      w.close();
      log = "";
    } catch (IOException ex) {
      System.err.println("Error saving logs:");
      ex.printStackTrace();
    }
  }

  public static Store getStore() {
    return store;
  }

  public static String httpGet(String address) {
    try {
      URL url = new URL(address);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String result = "";
      String line;
      while ((line = in.readLine()) != null) {
        result += line + "\n";
      }
      in.close();
      return result;
    } catch (Exception ex) {
      log("[E] httpGet failed: " + ex.toString());
      ex.printStackTrace();
      return null;
    }
  }

  public static int getUnsigned(int b) {
    if (b < 0) {
      return b + 256;
    } else {
      return b;
    }
  }

  public static int[] getUnsigned(byte[] b) {
    int[] result = new int[b.length];
    for(int i = 0; i < b.length; i++) {
      result[i] = Server.getUnsigned(b[i]);
    }
    return result;
  }

  public static int getUnsignedShort(int s) {
    if (s < 0) {
      return s + 65536;
    } else {
      return s;
    }
  }

  public static boolean isIPBanned(String ip) {
    return ipBans.contains(ip);
  }

  public static void banIP(String ip) {
    ipBans.add(ip);
    saveIPBans();
  }

  public static void unbanIP(String ip) {
    ipBans.remove(ip);
    saveIPBans();
  }

  private static void saveIPBans() {
    try {
      new File("ipbans.txt").delete();
      FileOutputStream out = new FileOutputStream("ipbans.txt");
      for (String ip : ipBans) {
        out.write((ip + "\n").getBytes());
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static boolean isMuted(String name) {
    return mutes.contains(name);
  }

  public static void mutePlayer(String name) {
    mutes.add(name);
    saveMutes();
  }

  public static void unMutePlayer(String name) {
    mutes.remove(name);
    saveMutes();
  }

  private static void saveMutes() {
    try {
      new File("mutes.txt").delete();
      FileOutputStream out = new FileOutputStream("mutes.txt");
      for (String name : mutes) {
        out.write((name + "\n").getBytes());
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static boolean isWhitelisted(String name) {
    return whitelist.contains(name);
  }

  public static void whitelistPlayer(String name) {
    whitelist.add(name);
    saveWhitelist();
  }

  public static void unWhitelistPlayer(String name) {
    whitelist.remove(name);
    saveWhitelist();
  }

  private static void saveWhitelist() {
    try {
      new File("whitelist.txt").delete();
      FileOutputStream out = new FileOutputStream("whitelist.txt");
      for (String name : mutes) {
        out.write((name + "\n").getBytes());
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static String readFileAsString(String filePath) throws java.io.IOException {
    StringBuilder fileData = new StringBuilder(1000);
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      fileData.append(buf, 0, numRead);
    }
    reader.close();
    return fileData.toString();
  }

  public static String cleanColorCodes(String msg) {
    String msg2 = "";
    for (int i = 0; i < msg.length(); i++) {
      char c = msg.charAt(i);
      if (c == '%') {
        if (i == msg.length() - 1) {
          msg2 += c;
        } else if (i == msg.length() - 2) {
          msg2 += c;
        } else if (!((msg.charAt(i + 1) >= '0' && msg.charAt(i + 1) <= '9')
            || (msg.charAt(i + 1) >= 'a' && msg.charAt(i + 1) <= 'f'))) {
          msg2 += c;
        } else {
          msg2 += '&';
        }
      } else {
        msg2 += c;
      }
    }
    return msg2;
  }

  public static void restartServer(String why) {
    String message = why == null ? "Server is restarting!" : "Server is restarting: " + why;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      p.getActionSender().sendLoginFailure(message);
    }
    try {
      Thread.sleep(2000);
    } catch (Exception ex) {
    }

    // This just exits but the server runs with a wrapper script that automatically restarts it.
    System.exit(1);
  }

  public static String getConsoleMessages(long minTime) {
    StringBuilder text = new StringBuilder();
    for (ConsoleMessage message : messages) {
      if (message.date.getTime() < minTime) break;
      text.append(message.message).append("\n");
    }
    return text.toString();
  }

  /**
   * Starts the server.
   *
   * @throws IOException if an I/O error occurs.
   */
  public void start() throws IOException {
    log("Binding to port " + Constants.PORT + "...");
    acceptor.setReuseAddress(true);
    acceptor.bind(new InetSocketAddress(Constants.PORT));
    log("Ready for connections.");
    createStore();
    WebServer.init();
  }

  public void createStore() {
    store = new Store();
  }

  public static class ConsoleMessage {
    public Date date;
    public String message;

    public ConsoleMessage(Date d, String m) {
      date = d;
      message = m;
    }
  }
}
