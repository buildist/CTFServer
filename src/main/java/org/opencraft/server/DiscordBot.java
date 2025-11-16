package org.opencraft.server;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import net.dv8tion.jda.api.JDABuilder;

public class DiscordBot implements Runnable {


  private static final long SERVER_ID = 187774973543317504L;
  private static final long CHANNEL_ID = 603405367031889940L;

  public static String sanitizeDiscordInput(String input) {
    return input.replaceAll("[^\\p{ASCII}]", "");
  }

  @Override
  public void run() {
    JDA api = JDABuilder.createLight(Configuration.getConfiguration().getDiscordToken(), EnumSet.of(
            GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
        .addEventListeners(new ListenerAdapter() {
          @Override
          public void onMessageReceived(MessageReceivedEvent event) {
            if (event.isWebhookMessage() || event.getChannel().getIdLong() != CHANNEL_ID
                || event.getMember() == null || event.getMessage().getContentStripped().isBlank()
                || event.getMember().getUser().isBot()) {
              return;
            }
            String message = event.getMessage().getContentStripped();
            String nickname = event.getMember().getEffectiveName();
            System.out.println("[Discord] " + nickname + ": " + message);

            switch (message) {
              case ".who", ".players" -> {
                List<Player> players = World.getWorld().getPlayerList().getPlayers();
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Players:");
                for (Player p : players) {
                  messageBuilder.append(" ").append(p.getName());
                }
                event.getChannel().sendMessage(messageBuilder.toString()).queue();
              }
              default -> World.getWorld()
                  .broadcast("&5[Discord] &f" + sanitizeDiscordInput(nickname) + ": "
                      + sanitizeDiscordInput(message));
            }
          }
        })
        .build();
    try {
      api.awaitReady();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

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
        api.getChannelById(TextChannel.class, CHANNEL_ID).getManager().setTopic(topic).queue();
      }

      try {
        Thread.sleep(60 * 1000);
      } catch (InterruptedException e) {
      }
    }
  }
}
