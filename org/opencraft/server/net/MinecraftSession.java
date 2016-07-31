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
package org.opencraft.server.net;


import org.apache.mina.core.session.IoSession;
import org.opencraft.server.Constants;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.packet.Packet;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Manages a connected Minecraft session.
 *
 * @author Graham Edgecombe
 */
public final class MinecraftSession extends OCSession {


  private final ActionSender actionSender = new ActionSender(this);
  /**
   * The action sender associated with this session.
   */
  public boolean levelSent = false;
  public boolean isEmailUser = false;
  public boolean ccUser = false;
  public boolean ccAuthenticated = false;
  public String client = "Minecraft.net";
  public String username;
  public String verificationKey;
  public HashMap<String, Integer> supportedCPEExtensions =
      new HashMap<String, Integer>(Constants.NUM_CPE_EXTENSIONS);
  public int numExtEntries = 0;
  public int receivedExtEntries = 0;
  public boolean receivedAllExtEntries = false;
  public int customBlockLevel = 0;
  private String ip;


  /**
   * The player associated with this session.
   */
  private Player player;


  public MinecraftSession(IoSession sess) {
    super(sess);
    sess.getConfig().setBothIdleTime(10);
    sess.getConfig().setWriteTimeout(5);
    ip = ((InetSocketAddress) sess.getRemoteAddress()).getAddress().getHostAddress();
  }

  /**
   * Gets the action sender associated with this session.
   *
   * @return The action sender.
   */
  public ActionSender getActionSender() {
    return actionSender;
  }

  public String getIP() {
    return ip;
  }

  /**
   * Gets the player associated with this session.
   *
   * @return The player.
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Sets the player associated with this session.
   *
   * @param player The player.
   */
  public void setPlayer(Player player) {
    this.player = player;
  }

  /**
   * Handles a packet.
   *
   * @param packet The packet to handle.
   */
  @Override
  public void handle(Packet packet) {
    PersistingHandlerManager.getPacketHandlerManager().handlePacket(this, packet);
  }


  /**
   * Called when this session is to be destroyed, should release any resources.
   */
  @Override
  public void destroy() {
    World.getWorld().unregister(this);
  }


  public boolean isAuthenticated() {
    if (player == null) {
      return false;
    } else {
      return true;
    }
  }

  public boolean isExtensionSupported(String name) {
    return isExtensionSupported(name, 1);
  }
  
  public boolean isExtensionSupported(String name, int version) {
    if (!ccUser)
      return false;
    else
      return supportedCPEExtensions.containsKey(name)
          && supportedCPEExtensions.get(name) == version;
  }

  public void addExtension(String name) {
    addExtension(name, 1);
  }
  
  public void addExtension(String name, int version) {
    supportedCPEExtensions.put(name, version);
  }
}
