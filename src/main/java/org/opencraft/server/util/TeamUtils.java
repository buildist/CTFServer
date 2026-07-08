package org.opencraft.server.util;

import org.opencraft.server.game.impl.GameSettings;

public class TeamUtils {

  public static String getTeamColor(int team) {
    if (team == 0) {
      return getColorCode(GameSettings.getString("Team1Color"));
    } else if (team == 1) {
      return getColorCode(GameSettings.getString("Team2Color"));
    }
    return "&7";
  }

  public static String toTitleCase(String input) {
    StringBuilder titleCase = new StringBuilder(input.length());
    boolean nextTitleCase = true;

    for (char c : input.toCharArray()) {
      if (Character.isSpaceChar(c)) {
        nextTitleCase = true;
      } else if (nextTitleCase) {
        c = Character.toTitleCase(c);
        nextTitleCase = false;
      }

      titleCase.append(c);
    }

    return titleCase.toString();
  }


  public static String getTeamName(int team) {
    if (team == 0) {
      String color = GameSettings.getString("Team1Color");
      return getColorCode(color) + toTitleCase(color);
    }
    else if (team == 1) {
      String color = GameSettings.getString("Team2Color");
      return getColorCode(color) + toTitleCase(color);
    }
    else {
      return "&7Spectators";
    }
  }

  public static String getColorCode(String colorName) {
    if (colorName == null) return "&7";

    return switch (colorName.toLowerCase()) {
      case "lime" -> "&a";
      case "cyan" -> "&b";
      case "red" -> "&c";
      case "pink" -> "&d";
      case "yellow" -> "&k";
      case "blue" -> "&9";
      case "purple" -> "&g";
      case "orange" -> "&i";
      default -> "&7";
    };
  }

  public static String getDarkerColorCode(String colorName) {
    if (colorName == null) return "&7";

    return switch (colorName.toLowerCase()) {
      case "lime" -> "&2";
      case "cyan" -> "&3";
      case "red" -> "&4";
      case "pink" -> "&5";
      case "yellow" -> "&6";
      case "blue" -> "&1";
      case "purple" -> "&h";
      case "orange" -> "&j";
      default -> "&7";
    };
  }
}
