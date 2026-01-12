package org.opencraft.server.cmd.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.replay.ReplayFile;
import org.opencraft.server.replay.ReplayThread;

public class ReplayCommand implements Command {

  public static final int UNSPECIFIED = -2;
  public static final int BAD = -1;
  public static final int SHOULD_EXIT = 0;
  public static final int CONTEXT_EXPECTING_DAY = 1;
  public static final int CONTEXT_EXPECTING_MONTH = 2;
  public static final int CONTEXT_EXPECTING_YEAR = 3;
  public static final int CONTEXT_EXPECTING_ID = 4;
  public static final byte MODE_REPLAY = 0;
  public static final byte MODE_ONLY_VIEW_METADATA = 1;
  public static final byte MODE_VIEW_IDS = 2;

  private static final ReplayCommand INSTANCE = new ReplayCommand();

  private final byte mode;

  public ReplayCommand() {
    this(MODE_REPLAY);
  }

  public ReplayCommand(byte mode) {
    this.mode = mode;
  }

  public static ReplayCommand getCommand() {
    return INSTANCE;
  }

  public static int[] parseArguments(Player player, CommandParameters params) {
    int day = UNSPECIFIED;
    int month = UNSPECIFIED;
    int year = UNSPECIFIED;
    int id = UNSPECIFIED;

    int context = 0;
    for (int i = 0; i < params.getArgumentCount(); i++) {
      String argument = params.getStringArgument(i);
      boolean shouldClearContextLater = (context != 0);
      switch (context) {
        case CONTEXT_EXPECTING_DAY: {
          if (checkAlreadySpecified(player, day)) return result(false, day, month, year, id);
          day = nonNegativeInteger(player, params.getStringArgument(i));

          break;
        }
        case CONTEXT_EXPECTING_MONTH: {
          if (checkAlreadySpecified(player, month)) return result(false, day, month, year, id);
          month = nonNegativeInteger(player, params.getStringArgument(i));

          break;
        }
        case CONTEXT_EXPECTING_YEAR: {
          if (checkAlreadySpecified(player, year)) return result(false, day, month, year, id);
          year = nonNegativeInteger(player, params.getStringArgument(i));

          break;
        }
        case CONTEXT_EXPECTING_ID: {
          if (checkAlreadySpecified(player, id)) return result(false, day, month, year, id);
          id = nonNegativeInteger(player, params.getStringArgument(i));

          break;
        }
        default: {
          if (argument.equalsIgnoreCase("day")) {
            context = CONTEXT_EXPECTING_DAY;
          } else if (argument.equalsIgnoreCase("month")) {
            context = CONTEXT_EXPECTING_MONTH;
          } else if (argument.equalsIgnoreCase("year")) {
            context = CONTEXT_EXPECTING_YEAR;
          } else if (argument.equalsIgnoreCase("id")) {
            context = CONTEXT_EXPECTING_ID;
          } else {
            if (player != null) {
              player.sendMessage("- &eUnrecognized parameter (at i=" + i + "): " + argument);
            }

            return result(false, day, month, year, id);
          }

          break;
        }
      }
      if (shouldClearContextLater) context = 0;
    }

    return result(true, day, month, year, id);
  }

  private static int[] result(boolean success, int day, int month, int year, int id) {
    return new int[] { (success ? 1 : SHOULD_EXIT), day, month, year, id };
  }

  private static boolean checkAlreadySpecified(Player player, int parameter) {
    if (parameter >= BAD) {
      if (player != null) player.sendMessage("- &eDetected a parameter that was specified twice");

      return true;
    }

    return false;
  }

  private static int nonNegativeInteger(Player player, String parameter) {
    int result;
    try {
      result = Integer.parseInt(parameter);
    } catch (NumberFormatException e) {
      if (player != null) player.sendMessage("- &e\"" + parameter + "\" cannot be converted to integer");

      return BAD;
    }
    if (result < 0) {
      if (player != null) player.sendMessage("- &e\"" + parameter + "\": expected a non-negative integer");

      return BAD;
    }

    return result;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (player.team != -1) {
      player.sendMessage("- &ePlease join the spectator team to use this command");

      return;
    }
    int[] result = parseArguments(player, params);
    if (result[0] == SHOULD_EXIT) return;

    int day = result[1];
    int month = result[2];
    int year = result[3];
    int id = result[4];

    boolean needId = (mode != MODE_VIEW_IDS);
    if ((day < 0) || (month < 0) || (year < 0) || (id < 0 && needId)) {
      player.sendMessage("- &eMissing required parameter(s)");
      player.sendMessage("- &eExpected to receive &fday <number> month <number>");
      player.sendMessage("   year <number>" + (needId ? " id <number>" : "") + " &ein any order");
      if (needId) {
        player.sendMessage("- &eUse &f/availablereplays &eif you need help with finding out");
        player.sendMessage("   &ethe replay id. Provide day, month and year in the same way");
      }

      return;
    }
    boolean onlyViewMetadata = false;
    if (mode == MODE_REPLAY || (onlyViewMetadata = (mode == MODE_ONLY_VIEW_METADATA))) {
      (new ReplayThread(player, day, month, year, id, onlyViewMetadata)).start();
    } else { // mode == MODE_VIEW_IDS
      List<Integer> availableIds = ReplayFile.availableIds(day, month, year);
      String answer = (availableIds.isEmpty() ? "&c<none>" :
          availableIds.stream().map(String::valueOf).collect(Collectors.joining(", ")));

      player.sendMessage("- &eAvailable IDs: " + answer);
    }
  }
}
