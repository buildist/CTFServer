package org.opencraft.server;

import java.util.List;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

public class DiscordBot implements Runnable {

  private static final long CHANNEL_ID = 603405367031889940L;

  public static String sanitizeDiscordInput(String input) {
    return input.replaceAll("[^\\p{ASCII}]", "");
  }

  @Override
  public void run() {
    DiscordApi api = new DiscordApiBuilder()
        .setToken(Configuration.getConfiguration().getDiscordToken()).login().join();
    api.addMessageCreateListener(event -> {
      if (event.getMessageAuthor().isYourself() || event.getMessageAuthor().isWebhook()
          || event.getChannel().getId() != CHANNEL_ID || event.getMessage().getContent()
          .isBlank()) {
        return;
      }
      String message = event.getMessage().getContent();
      System.err.println("[Discord] " + event.getMessageAuthor().getDisplayName()
          + ": " + message);
      switch (message) {
        case ".who", ".players" -> {
          List<Player> players = World.getWorld().getPlayerList().getPlayers();
          StringBuilder messageBuilder = new StringBuilder();
          messageBuilder.append("Players:");
          for (Player p : players) {
            messageBuilder.append(" ").append(p.getName());
          }
          event.getChannel().sendMessage(messageBuilder.toString());
        }
        default -> World.getWorld()
            .broadcast("&5[Discord] &f" + event.getMessageAuthor().getDisplayName()
                + ": " + sanitizeDiscordInput(message));
      }
    });
  }
}
