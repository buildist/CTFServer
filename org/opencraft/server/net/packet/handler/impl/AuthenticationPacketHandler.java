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
package org.opencraft.server.net.packet.handler.impl;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencraft.server.Constants;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;
import org.opencraft.server.persistence.LoadPersistenceRequest;

/**
 * Handles the incoming authentication packet.
 * @author Graham Edgecombe
 */
public final class AuthenticationPacketHandler implements PacketHandler<MinecraftSession> {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = Logger.getLogger(AuthenticationPacketHandler.class.getName());
	
	@Override
	public void handlePacket(MinecraftSession session, Packet packet) {
		if (session.isAuthenticated()) {
			return;
		}
		
		String username = packet.getStringField("username");
                int idx = username.indexOf("@");
                if(idx > -1) {
                    session.isEmailUser = true;
                    String[] parts = username.split("@");
                    int n = (parts[0].charAt(0)*parts[0].charAt(1)*parts[0].charAt(2))%99;
                    username = parts[0]+"_"+n;
                }
                int padding = packet.getNumericField("unused").intValue();
                if(padding == 0x42) {
                    session.ccUser = true;
                    boolean authenticated = true;
                    File test = new File("./savedGames/"+username.toLowerCase()+".xml");
                    if(test.exists()) {
                        Player p = new Player(null, username);
                        try {
                            new LoadPersistenceRequest(p).perform();
                            authenticated = p.getAttribute("ccAuthenticated") != null;
                        } catch (IOException ex) {
                            authenticated = false;
                        }
                    }
                    session.ccAuthenticated = authenticated;
                    if(!authenticated) {
                        username += "*";
                    }
                    session.getActionSender().sendCPEHandshake();
                }
                session.username = username;
		String verificationKey = session.verificationKey = packet.getStringField("verification_key");
		int protocolVersion = packet.getNumericField("protocol_version").intValue();
		
		if (protocolVersion != Constants.PROTOCOL_VERSION) {
			session.getActionSender().sendLoginFailure("Incorrect protocol version.");
		} else {
                    if(!session.ccUser)
			World.getWorld().register(session, username, verificationKey);
		}
	}

	
}
