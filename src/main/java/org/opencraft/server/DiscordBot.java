package org.opencraft.server;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.opencraft.server.model.World;

public class DiscordBot implements Runnable {

  private static final long CHANNEL_ID = 603405367031889940L;

  @Override
  public void run() {
    DiscordApi api = new DiscordApiBuilder()
        .setToken(Configuration.getConfiguration().getDiscordToken()).login().join();
    api.addMessageCreateListener(event -> {
      if (event.getMessageAuthor().isYourself() || event.getMessageAuthor().isWebhook()
          || event.getChannel().getId() != CHANNEL_ID  || event.getMessage().getContent().isBlank()) {
        return;
      }
      System.err.println("[Discord] " + event.getMessageAuthor().getDisplayName()
          + ": " + event.getMessage().getContent());
      World.getWorld().broadcast("&5[Discord] &f" + event.getMessageAuthor().getDisplayName()
          + ": " + event.getMessage().getContent());
    });
  }
}
