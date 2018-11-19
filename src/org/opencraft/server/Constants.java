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

import org.opencraft.server.model.BlockConstants;

/**
 * Holds global constants for the OpenCraft server.
 *
 * @author Graham Edgecombe
 */
public final class Constants {

  // THis is set in the opencraft.properties file

  public static final int SECRET = 476286881;
  // Server name
  public static final String SERVER_NAME = "Jacob_'s Capture the Flag";
  // /help
  public static final String HELP_TEXT =
      "Try to capture the other team's flag and bring it back "
          + "to your own side. Click the other team's flag to take it; capture it by clicking your own "
          + "flag. You can stop the enemy by tagging them when they're on your side of the map, blowing"
          + " them up with TNT (place a TNT block, then place a purple block or say /t to explode it), "
          + "cooking them with the flamethrower (/f) or placing landmines (dark gray blocks). You gain "
          + "points for doing well; say /store to find out how many points you have and what you can "
          + "buy.";
  // /help in TDM mode
  public static final String TDM_HELP_TEXT =
      "This is Team Deathmatch mode. You can kill players "
          + "on the other team by blowing them up with TNT (place a TNT block, then place a purple "
          + "block or say /t to explode it), cooking them with the flamethrower (/f) or placing "
          + "landmines (dark gray blocks). You gain points for doing well; say /store to find out how "
          + "many points you have and what you can buy. The team with the most kills after 10 minutes "
          + "wins. (Use the ClassiCube client to see a timer!)";
  // Supported Classic Protocol Extension names
  public static final String[] CPE_EXT_NAMES = {
    "HeldBlock",
    "EnvMapAppearance",
    "MessageTypes",
    "EnvColors",
    "HackControl",
    "CustomBlocks",
    "ExtPlayerList",
    "TwoWayPing",
    "BlockDefinitions",
    "BlockDefinitionsExt",
    "BlockPermissions",
    "InventoryOrder"
  };
  // CPE versions
  public static final int[] CPE_EXT_VERSIONS = {1, 2, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1};
  public static final int NUM_CPE_EXTENSIONS = CPE_EXT_NAMES.length;
  public static final int COLOR_SKY = 0;
  public static final int COLOR_CLOUD = 1;
  public static final int COLOR_FOG = 2;
  public static final int COLOR_AMBIENT = 3;
  public static final int COLOR_DIFFUSE = 4;
  // Environment colors for CPE clients
  public static final short[][] DEFAULT_COLORS =
      new short[][] {
        {127, 169, 255},
        {214, 229, 255},
        {158, 193, 255},
        {-1, -1, -1},
        {-1, -1, -1}
      };
  public static final byte CUSTOM_BLOCK_LEVEL = 1;
  // URL to send chat messages to
  public static final String URL_SENDCHAT =
      "http://buildism.net/mc/server/sendChat.php?k=" + SECRET;
  // URL to send server status (map + number of players) to
  public static final String URL_SERVER_STATUS = "http://buildism.net/mc/server/serverStatus.php";

  public static final String URL_MAP_COMMENT =
      "http://buildism.net/mc/server/mapComment.php?k=" + SECRET;

  public static final int BLOCK_MINE = 230;
  public static final int BLOCK_MINE_RED = 231;
  public static final int BLOCK_MINE_BLUE = 232;
  public static final int BLOCK_TNT = BlockConstants.TNT;
  public static final int BLOCK_RED_FLAG = 233;
  public static final int BLOCK_BLUE_FLAG = 234;
  public static final int BLOCK_DETONATOR = 235;
  public static final int BLOCK_CRATE = 64;
  public static final int BLOCK_PAYLOAD = 47;

  public static final int BLOCK_SOLIDITY_WALK_THROUGH = 0;
  public static final int BLOCK_SOLIDITY_SWIM_THROUGH = 1;
  public static final int BLOCK_SOLIDITY_SOLID = 2;

  public static final int BLOCK_WALK_SOUND_NONE = 0;
  public static final int BLOCK_WALK_SOUND_WOOD = 1;
  public static final int BLOCK_WALK_SOUND_GRAVEL = 2;
  public static final int BLOCK_WALK_SOUND_GRASS = 3;
  public static final int BLOCK_WALK_SOUND_STONE = 4;
  public static final int BLOCK_WALK_SOUND_METAL = 5;
  public static final int BLOCK_WALK_SOUND_GLASS = 6;
  public static final int BLOCK_WALK_SOUND_WOOL = 7;
  public static final int BLOCK_WALK_SOUND_SAND = 8;
  public static final int BLOCK_WALK_SOUND_SNOW = 9;

  public static final int BLOCK_DRAW_OPAQUE = 0;
  public static final int BLOCK_DRAW_TRANSPARENT_GLASS = 1;
  public static final int BLOCK_DRAW_TRANSPARENT_LEAVES = 2;
  public static final int BLOCK_DRAW_TRANSLUCENT = 3;
  public static final int BLOCK_DRAW_GAS = 4;

  public static final String[] MAP_EXTENSIONS = new String[] {"dat", "lvl", "cw"};

  public static final int ICE_MELT_TIME = 60000;
  public static final int PAYLOAD_RADIUS = 2;

  /** The protocol version of Minecraft that this version of OpenCraft is compatible with. */
  public static final int PROTOCOL_VERSION = 7;

  public static final String ROOT_PATH = ".";
  public static int PORT = 25560;
  public static String VERSION = "Capture the Flag";
  public static int SAFE_TIME = 3000;

  public static final int FLAME_THROWER_FUEL = 10000;
}
