package org.opencraft.server;

import java.util.List;
import java.util.stream.Collectors;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
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
        .setToken(Configuration.getConfiguration().getDiscordToken())
        .addIntents(Intent.MESSAGE_CONTENT)
        .login().join();
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

    String topic = null;
    while (true) {
      String previousTopic = topic;
      int count = World.getWorld().getPlayerList().size();
      if (count == 0) {
        topic = "";
      } else {
        String players = World.getWorld().getPlayerList().getPlayers().stream()
            .map(Player::getName).collect(
                Collectors.joining(", "));
        topic = "Online Players (%d): %s".formatted(count, players);
      }
      if (!topic.equals(previousTopic)) {
        api.getServerTextChannelById(CHANNEL_ID).get().updateTopic(topic);
      }

      try {
        Thread.sleep(60 * 1000);
      } catch (InterruptedException e) {
      }
    }
  }
}
