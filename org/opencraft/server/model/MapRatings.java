/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opencraft.server.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opencraft.server.Server;

/**
 *
 * @author Jacob
 */
public class MapRatings {
  private static final Map<String, List<Rating>> playerRatings = new HashMap<String, List<Rating>>();
  private static final Map<String, Double> averageRatings = new HashMap<String, Double>();
  
  public static void load() {
    playerRatings.clear();
    averageRatings.clear();
    
    File mapRatingsFolder = new File("mapratings");
    if (!mapRatingsFolder.exists()) {
      mapRatingsFolder.mkdir();
    }
    for(File file : mapRatingsFolder.listFiles()) {
      try {
      String mapName = file.getName().replace(".txt", "");
      playerRatings.put(mapName, new ArrayList<Rating>());
      String[] lines = Server.readFileAsString(file.getAbsolutePath()).split("\n");
      for(String line : lines) {
        String[] parts =  line.split(" ");
        String playerName = parts[0];
        int value = Integer.parseInt(parts[1]);
        playerRatings.get(mapName).add(new Rating(playerName, value));
      }
      updateRating(mapName);
      } catch(IOException ex) {
        Server.log(ex);
      }
    }
  }
  
  public static void save(String mapName) {
    try {
      String filename = "mapratings/" + mapName + ".txt";
      new File(filename).delete();
      FileOutputStream out = new FileOutputStream(filename);
      for (Rating rating : playerRatings.get(mapName)) {
        out.write((rating.playerName + " " + rating.value + "\n").getBytes());
      }
    } catch(IOException ex) {
      Server.log(ex);
    }
  }
  
  public static void setPlayerRating(String playerName, String mapName, int value) {
    mapName = mapName.replace("/", "_");
    if (!playerRatings.containsKey(mapName)) {
      playerRatings.put(mapName, new ArrayList<Rating>());      
    }
    Rating rating = null;
    for (Rating mapRating : playerRatings.get(mapName)) {
      if (mapRating.playerName.equals(playerName)) {
        rating = mapRating;
        break;
      }
    }
    if (rating == null) {
      rating = new Rating(playerName, value);
      playerRatings.get(mapName).add(rating);
    } else {
      rating.value = value;
    }
    save(mapName);
    updateRating(mapName);
  }
  
  public static String getRating(String mapName) {
    mapName = mapName.replace("/", "_");
    double rating = averageRatings.containsKey(mapName) ? averageRatings.get(mapName) : 0.5;
    return Math.round(rating * 100)+"%";
  }
  
  private static void updateRating(String mapName) {
    if (!playerRatings.containsKey(mapName) || playerRatings.get(mapName).isEmpty()) {
      averageRatings.put(mapName, 0.5);
      return;
    }
    
    double sum = 0;
    for (Rating rating : playerRatings.get(mapName)) {
      sum += rating.value;
    }
    averageRatings.put(mapName, sum / playerRatings.get(mapName).size());
  }
  
  static class Rating {
    public String playerName;
    public int value;
    
    public Rating(String name, int value) {
      this.playerName = name;
      this.value = value;
    }
  }
}
