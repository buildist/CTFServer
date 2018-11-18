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
package org.opencraft.server.model;

import com.flowpowered.nbt.ByteArrayTag;
import com.flowpowered.nbt.ByteTag;
import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.ShortTag;
import com.flowpowered.nbt.Tag;
import com.flowpowered.nbt.TagType;
import com.flowpowered.nbt.stream.NBTInputStream;

import java.awt.Color;

import org.apache.mina.util.byteaccess.ByteArray;
import org.opencraft.server.Server;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import org.opencraft.server.Constants;

/**
 * Represents the actual level.
 *
 * @author Graham Edgecombe
 * @author Brett Russell
 */
public final class Level implements Cloneable {

  public static final int CTF = 0;
  public static final int TDM = 1;
  public static final int PAYLOAD = 2;

  /**
   * The level width.
   */
  public int width;
  /**
   * The level height.
   */
  public int height;
  /**
   * The level depth.
   */
  public int depth;
  public Properties props;
  public int divider;
  public int ceiling;
  public int floor;
  public int mode = CTF;
  public String filename;
  public String id;
  public int votes = 0;

  public int sideBlock = 7;
  public int edgeBlock = 8;
  public int sideLevel = depth / 2;
  public String textureUrl;
  public short viewDistance = 0;
  public short[][] colors = new short[Constants.DEFAULT_COLORS.length][3];
  public HashSet<Integer> blockTypes = new HashSet<Integer>();
  
  /**
   * The blocks.
   */
  private byte[][][] blocks;
  private byte[] blocks1D;
  /**
   * Light depth array.
   */
  private short[][] lightDepths;
  /**
   * The spawn rotation.
   */
  private Rotation spawnRotation;
  /**
   * The spawn position.
   */
  private Position spawnPosition;
  private ArrayList<Position> tdmSpawns = new ArrayList<Position>();
  private ArrayList<Position> payloadPath = new ArrayList<>();
  private HashSet<Position> solidBlocks = new HashSet<Position>();
  private HashSet<Integer> solidTypes = new HashSet<Integer>();
  private boolean allSolidTypes = false;
  public final ArrayList<CustomBlockDefinition> customBlockDefinitions = new ArrayList<CustomBlockDefinition>();
  /**
   * The active "thinking" blocks on the map.
   */
  private Map<Integer, ArrayDeque<Position>> activeBlocks = new HashMap<Integer,
      ArrayDeque<Position>>();
  /**
   * The timers for the active "thinking" blocks on the map.
   */
  private Map<Integer, Long> activeTimers = new HashMap<Integer, Long>();
  /**
   * A queue of positions to update at the next tick.
   */
  private final Queue<Position> updateQueue = new ArrayDeque<Position>();

  private final Queue<UpdateBlock> iceBlocks = new LinkedList<>();

  /**
   * Generates a level.
   */
  public Level() {
    this.width = 256;
    this.height = 256;
    this.depth = 64;
    this.blocks = new byte[width][height][depth];
    this.lightDepths = new short[width][height];
    this.spawnRotation = new Rotation(0, 0);
    for (int i = 0; i < 256; i++) {
      BlockDefinition b = BlockManager.getBlockManager().getBlock(i);
      if (b != null && b.doesThink()) {
        activeBlocks.put(i, new ArrayDeque<Position>());
        activeTimers.put(i, System.currentTimeMillis());
      }
    }
    recalculateAllLightDepths();
    for (int i = 0 ; i < colors.length; i++) {
      for (int j = 0; j < 3; j++) {
        colors[i][j] = Constants.DEFAULT_COLORS[i][j];
      }
    }
    customBlockDefinitions.clear();
  }

  public void drawFire(Position pos, Rotation r) {
    pos = pos.toBlockPos();
    int heading = (int) (Server.getUnsigned(r.getRotation()) * ((float) 360 / 256)) - 90;
    int pitch = 0;

    int distance = GameSettings.getInt("FlameThrowerStartDistanceFromPlayer");
    int length = GameSettings.getInt("FlameThrowerLength");

    double px = (pos.getX());
    double py = (pos.getY());
    double pz = (pos.getZ()) - 1;

    double vx = Math.cos(Math.toRadians(heading));
    double vz = Math.tan(Math.toRadians(pitch));
    double vy = Math.sin(Math.toRadians(heading));
    double x = px;
    double y = py;
    double z = pz;
    for (int i = 0; i < length + distance; i++) {
      int bx = (int) Math.round(x);
      int by = (int) Math.round(y);
      int bz = (int) Math.round(z);

      // Check to make sure we are not above the build height.
      if (bz > World.getWorld().getLevel().ceiling) {
        return;
      }

      int oldBlock = getBlock(bx, by, bz);

      if (i < distance) {
        if (oldBlock != 0 && oldBlock != BlockConstants.STILL_LAVA) {
          return;
        }
      } else {
        // Turn sand into glass.
        if (oldBlock == BlockConstants.SAND) {
          World.getWorld().getLevel().setBlock(bx, by, bz, BlockConstants.GLASS);
        }
        CTFGameMode ctf = (CTFGameMode) World.getWorld().getGameMode();
        // Can't go through sand, glass, obsidian, water, or non explodable blocks
        if (oldBlock == BlockConstants.WATER ||
                oldBlock == BlockConstants.STILL_WATER ||
                oldBlock == BlockConstants.SAND ||
                oldBlock == BlockConstants.GLASS ||
                oldBlock == BlockConstants.OBSIDIAN ||
                !ctf.isExplodableBlock(World.getWorld().getLevel(), bx, by, bz)) {
          return;
        }
        World.getWorld().getLevel().setBlock(bx, by, bz, BlockConstants.STILL_LAVA);
      }

      x += vx;
      y += vy;
      z += vz;
    }
  }

  public void clearFire(Position pos, Rotation r) {
    pos = pos.toBlockPos();
    int heading = (int) (Server.getUnsigned(r.getRotation()) * ((float) 360 / 256)) - 90;
    int pitch = 0;

    int distance = GameSettings.getInt("FlameThrowerStartDistanceFromPlayer");
    int length = GameSettings.getInt("FlameThrowerLength");

    double px = (pos.getX());
    double py = (pos.getY());
    double pz = (pos.getZ()) - 1;

    double vx = Math.cos(Math.toRadians(heading));
    double vz = Math.tan(Math.toRadians(pitch));
    double vy = Math.sin(Math.toRadians(heading));
    double x = px + vx * distance;
    double y = py + vy * distance;
    double z = pz + vz * distance;
    for (int i = 0; i < length; i++) {
      int bx = (int) Math.round(x);
      int by = (int) Math.round(y);
      int bz = (int) Math.round(z);
      int oldBlock = getBlock(bx, by, bz);
      if (oldBlock == BlockConstants.STILL_LAVA) {
        World.getWorld().getLevel().setBlock(bx, by, bz, 0);
      }
      x += vx;
      y += vy;
      z += vz;
    }
  }

  public Level getCopy() {
    Level copy = new Level();
    copy.load(filename, id);
    return copy;
  }

  public String getCreator() {
    if (props.containsKey("author")) {
      return props.getProperty("author");
    } else {
      return MapController.getCreator(id);
    }
  }

  public void loadProps() {
    props = new Properties() {
      @Override
      public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
      }
    };
    try {
      String propsPath = filename.substring(0, filename.indexOf(".")) + ".properties";
      boolean newMap = false;
      if (!new File(propsPath).exists()) {
        propsPath = "./template.properties";
        newMap = true;
      }
      FileInputStream ps = new FileInputStream(propsPath);
      props.load(ps);
      ps.close();
      if (newMap) {
        World.getWorld().broadcast("Properties not found for this map, using defaults.");
        props.setProperty("divider", "" + width / 2);
        props.setProperty("buildCeiling", "" + height);
        props.setProperty("redSpawnX", "" + 0);
        props.setProperty("redSpawnY", "" + height);
        props.setProperty("redSpawnZ", "" + depth / 2);
        props.setProperty("blueSpawnX", "" + (width - 1));
        props.setProperty("blueSpawnY", "" + height);
        props.setProperty("blueSpawnZ", "" + depth / 2);
        props.setProperty("redFlagX", "" + 0);
        props.setProperty("redFlagY", "" + height / 2);
        props.setProperty("redFlagZ", "" + depth / 2);
        props.setProperty("blueFlagX", "" + (width - 1));
        props.setProperty("blueFlagY", "" + height / 2);
        props.setProperty("blueFlagZ", "" + depth / 2);
        saveProps();
      }
      ps.close();
    } catch (Exception ex) {
      Server.log("[E] Could not load props for " + id + ": " + ex);
    }

    if (props.getProperty("isTDM") != null) {
      mode = TDM;

      if (props.getProperty("spawnsX") != null && !props.containsKey("tdmSpawns")) {
        String[] spawnX = props.getProperty("spawnsX").split(" ");
        String[] spawnY = props.getProperty("spawnsY").split(" ");
        String[] spawnZ = props.getProperty("spawnsZ").split(" ");
        String spawns = "";
        for (int i = 0; i < spawnX.length; i++) {
          spawns += spawnX[i] + "," + spawnY[i] + "," + spawnZ[i];
          if (i != spawnX.length - 1) {
            spawns += " ";
          }
        }
        props.setProperty("tdmSpawns", spawns);
        props.remove("spawnsX");
        props.remove("spawnsY");
        props.remove("spawnsZ");
        saveProps();
      }

      tdmSpawns.clear();
      if (props.getProperty("tdmSpawns") != null) {
        String[] spawns = props.getProperty("tdmSpawns").split(" ");
        for (String spawn : spawns) {
          String[] parts = spawn.split(",");
          tdmSpawns.add(new Position(Integer.parseInt(parts[0]) * 32 + 16,
              Integer.parseInt(parts[2]) * 32 + 16, Integer.parseInt(parts[1]) * 32 + 16));
        }
      }
    } else if(props.getProperty("isPayload") != null) {
      mode = PAYLOAD;
      payloadPath.clear();
      String[] path = props.getProperty("payloadPath").split(" ");
      for (String position : path) {
        String[] parts = position.split(",");
        payloadPath.add(new Position(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])));
      }
    } else {
      mode = CTF;
      divider = Integer.parseInt(props.getProperty("divider"));
    }
    if (props.getProperty("solidBlocks") != null) {
      if (props.getProperty("solidBlocks").equals("all")) {
        allSolidTypes = true;
      } else {
        String[] solidTypesString = props.getProperty("solidBlocks").split(" ");
        for (String t : solidTypesString) {
          solidTypes.add(Integer.parseInt(t));
        }
      }
    }
    ceiling = Integer.parseInt(props.getProperty("buildCeiling"));
    floor = Integer.parseInt(props.getProperty("buildFloor", "-8"));

    setMapColors();

    if(props.getProperty("viewDistance") != null) {
      try {
        viewDistance = (short) Integer.parseInt(props.getProperty("viewDistance"));
      } catch(NumberFormatException ex) {
        viewDistance = 0;
      }
    }

    if(props.getProperty("sideBlock") != null) {
      try {
        sideBlock = (short) Integer.parseInt(props.getProperty("sideBlock"));
      } catch(NumberFormatException ex) {
        sideBlock = 7;
      }
    }

    if(props.getProperty("edgeBlock") != null) {
      try {
        edgeBlock = (short) Integer.parseInt(props.getProperty("edgeBlock"));
      } catch(NumberFormatException ex) {
        edgeBlock = 8;
      }
    }

    if(props.getProperty("sideLevel") != null) {
      try {
        sideLevel = (short) Integer.parseInt(props.getProperty("sideLevel"));
      } catch(NumberFormatException ex) {
        sideLevel = depth/2;
      }
    }

    if (props.getProperty("spawnPosition") != null) {
      String[] parts = props.getProperty("spawnPosition").split(",");
      spawnPosition = new Position(Integer.parseInt(parts[0]) * 32 + 16,
          Integer.parseInt(parts[1]) * 32 + 16, Integer.parseInt(parts[2]) * 32 + 16);
    }

    if (props.getProperty("spawnRotation") != null) {
      String[] parts = props.getProperty("spawnRotation").split(",");
      spawnRotation = new Rotation(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    if (World.getWorld().getLevel() == this) {
      World.getWorld().getGameMode().resetRedFlagPos();
      World.getWorld().getGameMode().resetBlueFlagPos();
      World.getWorld().getGameMode().placeRedFlag();
      World.getWorld().getGameMode().placeBlueFlag();
    }
  }

  private void setMapColors() {
    setMapColor("skyColor", 0);
    setMapColor("cloudColor", 1);
    setMapColor("fogColor", 2);
    setMapColor("ambientColor", 3);
    setMapColor("diffuseColor", 4);
  }
  
  private void setMapColor(String propertyName, int id) {
    if (props.getProperty(propertyName) != null) {
      String hexColor = props.getProperty(propertyName);
      Color color;
      try {
        color = Color.decode(hexColor);
      } catch(NumberFormatException ex) {
        colors[id][0] = -1;
        colors[id][1] = -1;
        colors[id][2] = -1;
        return;
      }
      colors[id][0] = (short) color.getRed();
      colors[id][1] = (short) color.getGreen();
      colors[id][2] = (short) color.getBlue();
    }
  }

  public Level load(String filename, String id) {
    solidTypes.add(BlockConstants.ADMINIUM);
    this.filename = filename;
    this.id = id;
    if (filename.endsWith(".lvl")) {
      FileInputStream fis;
      GZIPInputStream gzis;
      DataInputStream inputstream;
      try {
        fis = new FileInputStream(filename);
        gzis = new GZIPInputStream(fis);
        inputstream = new DataInputStream(gzis);

        short version = inputstream.readShort();
        short[] vars = new short[6];
        byte[] rot = new byte[2];
        if (version == 20999) {
          byte[] header = new byte[16];
          inputstream.read(header);
          ByteBuffer bb = ByteBuffer.wrap(header);
          bb.order(ByteOrder.LITTLE_ENDIAN);
          for (int i = 0; i < 6; i++) {
            vars[i] = bb.getShort();
          }
          rot[0] = header[12];
          rot[1] = header[13];
        } else {
          vars[0] = version;
          vars[1] = inputstream.readShort();
          vars[2] = inputstream.readShort();
          vars[3] = inputstream.readShort();
          vars[4] = inputstream.readShort();
          vars[5] = inputstream.readShort();
          rot[0] = inputstream.readByte();
          rot[1] = inputstream.readByte();
        }

        width = vars[0];
        height = vars[1];
        depth = vars[2];
        blocks = new byte[width][height][depth];
        blocks1D = new byte[width * height * depth];
        byte[] tmpBlocks = new byte[width * height * depth];
        inputstream.readFully(tmpBlocks);
        this.spawnPosition = new Position(vars[3] * 32 + 16, vars[4] * 32 + 16, vars[5] * 32 + 16);

        Server.log("Loading map: " + id);
        loadProps();

        loadBlocks(tmpBlocks);

        try {
          if (inputstream.readByte() == (byte) 0xBD) {
            // https://github.com/Hetal728/MCGalaxy/blob/b5cf22c3c06d5b6ff0e255bfc769118f427d5d06/MCGalaxy/Levels/IO/Importers/LvlImporter.cs#L80

            int chunksX = ceilDiv16(width);
            int chunksY = ceilDiv16(height);
            int chunksZ = ceilDiv16(depth);
            byte[][] customBlocks = new byte[chunksX * chunksY * chunksZ][];
            int index = 0;
            for (int y = 0; y < chunksY; y++) {
              for (int z = 0; z < chunksZ; z++) {
                for (int x = 0; x < chunksX; x++) {
                  if (inputstream.readByte() == 1) {
                    byte[] chunk = new byte[16 * 16 * 16];
                    inputstream.readFully(chunk);
                    customBlocks[index] = chunk;
                  }
                  index++;
                }
              }
            }
          }
        } catch (EOFException ex) {}
        gzis.close();
        fis.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else if (filename.endsWith(".dat")) {
      FileInputStream fis = null;
      GZIPInputStream gzis = null;
      ObjectInputStream in = null;
      DataInputStream inputstream = null;
      com.mojang.minecraft.level.Level l = null;
      try {
        fis = new FileInputStream(filename);
        gzis = new GZIPInputStream(fis);
        inputstream = new DataInputStream(gzis);
        if ((inputstream.readInt()) != 0x271bb788) {
          return null;
        }
        if ((inputstream.readByte()) > 2) {
          System.out.println("Error: Level version > 2, this is unexpected!");
          return null;
        }
        in = new ObjectInputStream(gzis);
        l = (com.mojang.minecraft.level.Level) in.readObject();
        inputstream.close();
        in.close();
        gzis.close();
        fis.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
      }
      l.initTransient();
      byte[] tmpBlocks = l.blocks;
      width = (short) l.width;
      height = (short) l.height;
      depth = (short) l.depth;
      blocks = new byte[width][height][depth];
      blocks1D = new byte[width * height * depth];
      this.spawnPosition = new Position(l.xSpawn * 32 + 16, (l.zSpawn) * 32 + 16, l.ySpawn  * 32 + 16);

      Server.log("Loading map: " + id);
      loadProps();

      loadBlocks(tmpBlocks);

    } else if (filename.endsWith(".cw")) {
      FileInputStream fileIn;
      NBTInputStream nbtIn;
      try {
        fileIn = new FileInputStream(filename);
        nbtIn = new NBTInputStream(fileIn);

        CompoundMap classicWorld = ((CompoundTag) nbtIn.readTag()).getValue();

        width = ((ShortTag)classicWorld.get("X")).getValue();
        height = ((ShortTag)classicWorld.get("Z")).getValue();
        depth = ((ShortTag)classicWorld.get("Y")).getValue();
        blocks = new byte[width][height][depth];
        blocks1D = new byte[width * height * depth];

        byte[] tmpBlocks = ((ByteArrayTag)classicWorld.get("BlockArray")).getValue();

        CompoundMap spawn = ((CompoundTag)classicWorld.get("Spawn")).getValue();
        int spawnX = ((ShortTag)spawn.get("X")).getValue();
        int spawnY = ((ShortTag)spawn.get("Y")).getValue();
        int spawnZ = ((ShortTag)spawn.get("Z")).getValue();
        int spawnH = ((ByteTag)spawn.get("H")).getValue();
        int spawnP = ((ByteTag)spawn.get("P")).getValue();
        this.spawnPosition = new Position(spawnX * 32 + 16, spawnZ * 32 + 16, spawnY * 32 + 16);
        this.spawnRotation = new Rotation(spawnH, spawnP);

        Server.log("Loading map: " + id);
        loadProps();

        loadBlocks(tmpBlocks);
        loadMetadata(((CompoundTag)classicWorld.get("Metadata")).getValue());

      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return this;
  }

  private void loadBlocks(byte[] blockArray) {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        for (int z = 0; z < depth; z++) {
          int type = blockArray[(z * height + y) * width + x];
          blockTypes.add(type);

          if (GameSettings.getBoolean("Chaos")) {
            if (type == 7) {
              type = 1;
            } else if (type == 8) {
              type = 9;
            } else if (type == 10) {
              type = 11;
            }
            if (z == 0) {
              type = 7;
            }
          }
          if ((allSolidTypes && type != 0 && type != 8 && type != 9 && type != 10 && type !=
              11) || solidTypes.contains(type)) {
            solidBlocks.add(new Position(x, y, z));
          }
          blocks[x][y][z] = (byte) type;
          blocks1D[(z * height + y) * width + x] = (byte) type;
        }
      }
    }
  }

  private void loadMetadata(CompoundMap metadata) {
    CompoundMap cpe = ((CompoundTag)metadata.get("CPE")).getValue();

    if (cpe.containsKey("BlockDefinitions")) {
      CompoundMap blockDefinitions = ((CompoundTag) cpe.get("BlockDefinitions")).getValue();
      for (Tag blockTag : blockDefinitions.values()) {
        if (!(blockTag instanceof CompoundTag)) continue;
        ;
        CompoundMap block = ((CompoundTag) blockTag).getValue();
        int blockDraw = (Byte) block.get("BlockDraw").getValue();
        int collideType = (Byte) block.get("CollideType").getValue();
        byte[] coords = ((ByteArrayTag) block.get("Coords")).getValue();
        byte[] fog = ((ByteArrayTag) block.get("Fog")).getValue();
        int fullBright = (Byte) block.get("FullBright").getValue();
        int id = (Byte) block.get("ID").getValue();
        String name = (String) block.get("Name").getValue();
        int shape = (Byte) block.get("Shape").getValue();
        float speed = (Float) block.get("Speed").getValue();
        byte[] textures = ((ByteArrayTag) block.get("Textures")).getValue();
        int transmitsLight = (Byte) block.get("TransmitsLight").getValue();
        int walkSound = (Byte) block.get("WalkSound").getValue();

        //if (!blockTypes.contains(id)) continue;

        CustomBlockDefinition blockDef = new CustomBlockDefinition(
            Server.getUnsigned(id),
            name,
            collideType,
            128,
            textures[0],
            textures[2],
            textures[3],
            textures[4],
            textures[5],
            textures[1],
            transmitsLight == 1,
            walkSound,
            fullBright == 1,
            coords[0],
            coords[1],
            coords[2],
            coords[3],
            coords[4],
            coords[5],
            blockDraw,
            fog[0],
            fog[1],
            fog[2],
            fog[3],
            -1);
        customBlockDefinitions.add(blockDef);
        BlockManager.getBlockManager().addCustomBlock(blockDef);
      }
    }

    if (cpe.containsKey("EnvColors")) {
      CompoundMap envColors = ((CompoundTag) cpe.get("EnvColors")).getValue();
      String ambient = getColor(((CompoundTag)envColors.get("Ambient")).getValue());
      String cloud = getColor(((CompoundTag)envColors.get("Cloud")).getValue());
      String fog = getColor(((CompoundTag)envColors.get("Fog")).getValue());
      String sky = getColor(((CompoundTag)envColors.get("Sky")).getValue());
      String sunlight = getColor(((CompoundTag)envColors.get("Sunlight")).getValue());
      props.put("ambientColor", ambient);
      props.put("cloudColor", cloud);
      props.put("fogColor", fog);
      props.put("skyColor", sky);
      props.put("diffuseColor", sunlight);
      setMapColors();
    }

    if (cpe.containsKey("EnvMapAppearance")) {
      CompoundMap envMapAppearance = ((CompoundTag)cpe.get("EnvMapAppearance")).getValue();
      this.edgeBlock = (Byte) envMapAppearance.get("EdgeBlock").getValue();
      this.sideBlock = (Byte) envMapAppearance.get("SideBlock").getValue();
      this.sideLevel = (Short) envMapAppearance.get("SideLevel").getValue();
      this.textureUrl = (String) envMapAppearance.get("TextureURL").getValue();
      props.put("edgeBlock", edgeBlock + "");
      props.put("sideBlock", sideBlock + "");
      props.put("sideLevel", sideLevel + "");
    }
  }

  private String getColor(CompoundMap map) {
    int r = (Short) map.get("R").getValue();
    int g = (Short) map.get("G").getValue();
    int b = (Short) map.get("B").getValue();
    return String.format("#%02x%02x%02x", r, g, b);
  }

  public void saveProps() {
    try {
      props.store(new FileOutputStream(filename.substring(0, filename.indexOf(".")) + "" +
          ".properties"), filename);
    } catch (IOException ex) {
      Server.log(ex);
    }
  }

  public boolean isSolid(int x, int y, int z) {
    return solidBlocks.contains(new Position(x, y, z));
  }

  public boolean isSolid(Position position) {
    return solidBlocks.contains(position);
  }

  public Object getProp(String p) {
    return props.get(p);
  }

  public Position getTeamSpawn(String team) {
    if (mode == TDM) {
      if (tdmSpawns.isEmpty()) {
        return Player.getSpawnPos();
      } else {
        return tdmSpawns.get((int) (Math.random() * tdmSpawns.size()));
      }
    } else {
      if (team.equals("spec")) {
        return Math.random() < 0.5 ? getTeamSpawn("red") : getTeamSpawn("blue");
      } else {
        return new Position(Integer.parseInt((String) getProp(team + "SpawnX")) * 32 + 16,
            Integer.parseInt((String) getProp(team + "SpawnZ")) * 32 + 16, (Integer.parseInt(
            (String) getProp(team + "SpawnY"))) * 32 + 16);
      }
    }
  }

  public List<Position> getPayloadPath() {
    return payloadPath;
  }

  /**
   * Recalculates all light depths. WARNING: this is a costly function and should only be used when
   * it really is necessary.
   */
  public void recalculateAllLightDepths() {
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        recalculateLightDepth(x, y);
      }
    }
  }

  /**
   * Recalculates the light depth of the specified coordinates.
   *
   * @param x The x coordinates.
   * @param y The y coordinates.
   */
  public void recalculateLightDepth(int x, int y) {
    for (int z = depth - 1; z >= 0; z--) {
      if (BlockManager.getBlockManager().getBlock(blocks[x][y][z]).doesBlockLight()) {
        lightDepths[x][y] = (short) z;
        return;
      }
    }
    lightDepths[x][y] = (short) -1;
  }

  /**
   * Manually assign a light depth to a given Cartesian coordinate.
   *
   * @param x     The X coordinate.
   * @param y     The Y coordinate.
   * @param depth The lowest-lit block.
   */
  public void assignLightDepth(int x, int y, int depth) {
    if (depth > this.height) {
      return;
    }
    lightDepths[x][y] = (short) depth;
  }

  /**
   * Gets the light depth at the specific coordinate.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @return The light depth.
   */
  public int getLightDepth(int x, int y) {
    return lightDepths[x][y];
  }

  /**
   * Performs physics updates on queued blocks.
   */
  public void applyBlockBehaviour() {
    Queue<Position> currentQueue;
    synchronized (updateQueue) {
      currentQueue = new ArrayDeque<>(updateQueue);
      updateQueue.clear();
    }
    for (Position pos : currentQueue) {
      BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ()))
          .behavePassive(this, pos.getX(), pos.getY(), pos.getZ());
    }
    // we only process up to 500 of each type of thinking block every tick,
    // or we'd probably be here all day.
    for (int type = 0; type < 256; type++) {
      if (activeBlocks.containsKey(type)) {
        if (System.currentTimeMillis() - activeTimers.get(type) > BlockManager.getBlockManager()
            .getBlock(type).getTimer()) {
          int cyclesThisTick = (activeBlocks.get(type).size() > 500 ? 500 : activeBlocks.get(type)
              .size());
          for (int i = 0; i < cyclesThisTick; i++) {
            Position pos = activeBlocks.get(type).poll();
            if (pos == null) {
              break;
            }
            // the block that occupies this space might have
            // changed.
            if (this.getBlock(pos.getX(), pos.getY(), pos.getZ()) == type) {
              // World.getWorld().broadcast("Processing thinker at ("+pos.getX()+","+pos.getY()
              // +","+pos.getZ()+")");
              BlockManager.getBlockManager().getBlock(type).behaveSchedule(this, pos.getX(), pos
                  .getY(), pos.getZ());
            }
          }
          activeTimers.put(type, System.currentTimeMillis());
        }
      }
    }

    while (!iceBlocks.isEmpty()) {
      UpdateBlock block = iceBlocks.peek();
      if (System.currentTimeMillis() - block.time > Constants.ICE_MELT_TIME) {
        iceBlocks.remove();
        if (getBlock(block.position) == 60) {
          setBlock(block.position, 0);
        }
      } else {
        break;
      }
    }
  }

  /**
   * Gets all of the blocks.
   *
   * @return All of the blocks.
   */
  public byte[][][] getBlocks() {
    return blocks;
  }

  public byte[] getBlocks1D() {
    return blocks1D;
  }

  /**
   * Gets the width of the level.
   *
   * @return The width of the level.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Gets the height of the level.
   *
   * @return The height of the level.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Gets the depth of the level.
   *
   * @return The depth of the level.
   */
  public int getDepth() {
    return depth;
  }

  public void setBlock(Position pos, int type) {
    setBlock(pos.getX(), pos.getY(), pos.getZ(), type);
  }

  /**
   * Sets a block and updates the neighbours.
   *
   * @param x    The x coordinate.
   * @param y    The y coordinate.
   * @param z    The z coordinate.
   * @param type The type id.
   */
  public void setBlock(int x, int y, int z, int type) {
    setBlock(x, y, z, type, true);
  }

  /**
   * Sets a block.
   *
   * @param x          The x coordinate.
   * @param y          The y coordinate.
   * @param z          The z coordinate.
   * @param type       The type id.
   * @param updateSelf Update self flag.
   */
  public void setBlock(int x, int y, int z, int type, boolean updateSelf) {
    if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
      return;
    }
    blocks1D[(z * this.height + y) * this.width + x] = (byte) type;
    int formerBlock = this.getBlock(x, y, z);
    blocks[x][y][z] = (byte) type;
    if (type != formerBlock) {
      for (Player player : World.getWorld().getPlayerList().getPlayers()) {
        player.getSession().getActionSender().sendBlock(x, y, z, (byte) type);
      }
    }
    if (updateSelf) {
      queueTileUpdate(x, y, z);
    }
    if (type == 0) {
      BlockManager.getBlockManager().getBlock(formerBlock).behaveDestruct(this, x, y, z);
      updateNeighboursAt(x, y, z);
      if (this.getLightDepth(x, y) == z) {
        //this.recalculateLightDepth(x, y);
        //this.scheduleZPlantThink(x, y, z);
      }
    }
    Position position = new Position(x, y, z);
    if (BlockManager.getBlockManager().getBlock(type).doesThink()) {
      activeBlocks.get(type).add(position);
    }
    if (BlockManager.getBlockManager().getBlock(type).doesBlockLight()) {
      this.assignLightDepth(x, y, z);
      this.scheduleZPlantThink(x, y, z);
    }
    if (type == 60) {
      iceBlocks.add(new UpdateBlock(position, System.currentTimeMillis()));
    }
  }

  /**
   * Schedules plants to think in a Z coordinate if a block above them changed.
   *
   * @param x X coordinate.
   * @param y Y coordinate.
   * @param z Z coordinate.
   */
  public void scheduleZPlantThink(int x, int y, int z) {
    for (int i = z - 1; i > 0; i--) {
      if (BlockManager.getBlockManager().getBlock(this.getBlock(x, y, i)).isPlant()) {
        queueActiveBlockUpdate(x, y, i);
      }
      if (BlockManager.getBlockManager().getBlock(this.getBlock(x, y, i)).doesBlockLight()) {
        return;
      }
    }
  }

  /**
   * Updates neighbours at the specified coordinate.
   *
   * @param x X coordinate.
   * @param y Y coordinate.
   * @param z Z coordinate.
   */
  private void updateNeighboursAt(int x, int y, int z) {
    queueTileUpdate(x - 1, y, z);
    queueTileUpdate(x + 1, y, z);
    queueTileUpdate(x, y - 1, z);
    queueTileUpdate(x, y + 1, z);
    queueTileUpdate(x, y, z - 1);
    queueTileUpdate(x, y, z + 1);
    //recalculateLightDepth(x, y);
  }

  /**
   * Queues a tile update.
   *
   * @param x X coordinate.
   * @param y Y coordinate.
   * @param z Z coordinate.
   */
  private void queueTileUpdate(int x, int y, int z) {
    if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
      Position pos = new Position(x, y, z);
      synchronized (updateQueue) {
        if (!updateQueue.contains(pos)) {
          updateQueue.add(pos);
        }
      }
    }
  }

  /**
   * Forces a tile update to be queued. Use with caution.
   *
   * @param x X coordinate.
   * @param y Y coordinate.
   * @param z Z coordinate.
   */
  public void queueActiveBlockUpdate(int x, int y, int z) {
    if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
      int blockAt = this.getBlock(x, y, z);
      if (BlockManager.getBlockManager().getBlock(blockAt).doesThink()) {
        activeBlocks.get(blockAt).add(new Position(x, y, z));
      }
    }
  }

  /**
   * Gets a block.
   *
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   * @return The type id.
   */
  public int getBlock(int x, int y, int z) {
    if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
      return Server.getUnsigned(blocks[x][y][z]);
    } else {
      return 0;
    }
  }

  public int getBlock(Position pos) {
    return getBlock(pos.getX(), pos.getY(), pos.getZ());
  }

  /**
   * Get the spawning rotation.
   *
   * @return The spawning rotation.
   */
  public Rotation getSpawnRotation() {
    return spawnRotation;
  }

  /**
   * Get the spawn position.
   *
   * @return The spawn position.
   */
  public Position getSpawnPosition() {
    return spawnPosition;
  }

  private static int ceilDiv16(int x) {
    return (x + 15) / 16;
  }
}

class UpdateBlock {
  public final Position position;
  public final long time;

  public UpdateBlock(Position position, long time) {
    this.position = position;
    this.time = time;
  }
}
