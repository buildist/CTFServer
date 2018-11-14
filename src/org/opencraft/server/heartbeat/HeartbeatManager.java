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
package org.opencraft.server.heartbeat;

import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.model.World;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * A class which manages heartbeats.
 *
 * @author Graham Edgecombe
 */
public class HeartbeatManager {
  /**
   * The singleton instance.
   */
  private static final HeartbeatManager INSTANCE = new HeartbeatManager();

  /**
   * Default private constructor.
   */
  private HeartbeatManager() {
        /* empty */
    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }
        }
    };
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the heartbeat manager instance.
   *
   * @return The heartbeat manager instance.
   */
  public static HeartbeatManager getHeartbeatManager() {
    return INSTANCE;
  }

  /**
   * Sends a heartbeat with the specified parameters. This method does not block.
   *
   * @param parameters The parameters.
   */
  public String sendHeartbeat(String url, final Map<String, String> parameters, String name) {
    parameters.put("name", name);
    final StringBuilder bldr = new StringBuilder();
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      bldr.append(entry.getKey());
      bldr.append('=');
      try {
        bldr.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
      bldr.append('&');
    }
    if (bldr.length() > 0) {
      bldr.deleteCharAt(bldr.length() - 1);
    }
    // send it off
    boolean success = false;
    while (!success) {
      try {
                /*URL url2 = new URL(url + "?" + bldr.toString());
                HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("GET");
                //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                //conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                // this emulates the minecraft server exactly.. idk why
                // notch added this personally
                //conn.setRequestProperty("Content-Language", "en-US");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.connect();
                try {
                    String link;
                    BufferedReader rdr = new BufferedReader(new InputStreamReader(conn
                    .getInputStream()));
                    try {
                        link = rdr.readLine();
                        System.out.println("To connect to this server, use : " + link + ".");
                        sending = false;
                    } finally {
                        rdr.close();
                    }
                } finally {
                    conn.disconnect();
                }*/
        String paramString = bldr.toString();
        String response = Server.httpGet(url + "?" + paramString);
        if (Configuration.getConfiguration().isTest()) {
          Server.log("URL: " + response.trim());
          Server.log("Players: " + World.getWorld().getPlayerList().size());
        }
        success = true;
      } catch (Exception ex) {
        if (!success) {
          System.out.println("Error sending hearbeat: " + ex.toString());
        }
      }
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ex1) {
      }
      if (!success) {

      }
    }
    return "";
  }

  /**
   * Gets the salt.
   *
   * @return The salt.
   */
  public long getSalt() {
    return Constants.SECRET;
  }
}
