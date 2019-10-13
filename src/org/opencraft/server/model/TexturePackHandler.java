package org.opencraft.server.model;

import org.opencraft.server.Server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TexturePackHandler {
  public static boolean hasCustomTexturePack(String map) {
    File texturePackFile = new File("texturepacks/terrain_" + map + ".zip");
    return  texturePackFile.exists();
  }

  public static void createPatchedTexturePack(String map) {
    try {
      File texturePackFile = new File("texturepacks/terrain_" + map + ".zip");
      File outputFile = new File("texturepacks_cache/terrain_" + map + ".zip");
      if (outputFile.exists()) {
        return;
      }

      File fontFile = new File("texturepack_patch/default.png");

      ZipFile in = new ZipFile(texturePackFile);
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
      Enumeration<? extends  ZipEntry> entries = in.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        out.putNextEntry(new ZipEntry(entry.getName()));
        switch (entry.getName()) {
          case "default.png":
            Files.copy(fontFile.toPath(), out);
            break;
          default:
            DataInputStream dataIn = new DataInputStream(in.getInputStream(entry));
            byte[] bytes = new byte[(int) entry.getSize()];
            dataIn.readFully(bytes);
            out.write(bytes);
            dataIn.close();
            break;
        }
      }
      in.close();
      out.close();
    } catch (IOException ex) {
      Server.log(ex);
    }
  }
}
