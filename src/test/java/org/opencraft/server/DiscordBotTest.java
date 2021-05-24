package org.opencraft.server;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DiscordBotTest {
  @Test
  void sanitizeDiscordInput() {
    String input = "\uD83D\uDCA9 POOP";
    assertEquals(" POOP", DiscordBot.sanitizeDiscordInput(input));
  }
}
