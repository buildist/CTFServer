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
package org.opencraft.server.extensions.brushes;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;

/**
 * @author S�ren Enevoldsen
 * @author Graham Edgecombe
 */

public final class BrushCommand implements Command {

  /**
   * The instance of this command.
   */
  private static final BrushCommand INSTANCE = new BrushCommand();

  /**
   * Default private constructor.
   */
  private BrushCommand() {
        /* empty */
  }

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static BrushCommand getBrushCommand() {
    return INSTANCE;
  }

  private void usage(Player player) {
    player.getActionSender().sendChatMessage("/brush radius [radius]");
    player.getActionSender().sendChatMessage("/brush dim width height length");
    player.getActionSender().sendChatMessage("/brush dim [width|height|length] x");
    player.getActionSender().sendChatMessage("/brush [standard|default]");
    player.getActionSender().sendChatMessage("/brush delete [1|0]");
    player.getActionSender().sendChatMessage("/brush type [square|diamond|line|flat|box]");
  }

  public void execute(Player player, CommandParameters parameters) {

    String action = parameters.getStringArgument(0).toLowerCase();

    if (parameters.getArgumentCount() == 1) {
      if (action.equals("default") || action.equals("standard")) {
        player.setAttribute("brush", new StandardBrush());
        player.getActionSender().sendChatMessage("Now using standard brush");
      } else
        usage(player);
    }

    // If no brush then return
    if (player.getAttribute("brush") == null) {
      player.getActionSender().sendChatMessage("You don't have a brush!");
      player.getActionSender().sendChatMessage("Use: \"/brush standard\" to get one");
      return;
    }

    if (parameters.getArgumentCount() == 2) {

      if (action.equals("radius")) {
        try {
          int newRadius = parameters.getIntegerArgument(1);
          ((Brush) player.getAttribute("brush")).setRadius(newRadius);
          player.getActionSender().sendChatMessage("Brush radius changed");
        } catch (Exception e) {
          player.getActionSender().sendChatMessage("/brush radius [radius]");
          return;
        }

      } else if (action.equals("delete")) {
        String onOff = parameters.getStringArgument(1);
        if (onOff.equals("1")) {
          ((Brush) player.getAttribute("brush")).useForDelete(true);
          player.getActionSender().sendChatMessage("Using this brush to delete");
        } else if (onOff.equals("0")) {
          ((Brush) player.getAttribute("brush")).useForDelete(false);
          player.getActionSender().sendChatMessage("Using standard brush to delete");
        } else
          player.getActionSender().sendChatMessage("/brush delete [1|0]");
      } else if (action.equals("type")) {
        String brush = parameters.getStringArgument(1).toLowerCase();
        int bRadius = ((Brush) player.getAttribute("brush")).getRadius();
        boolean delete = false;
        if (player.getAttribute("brush").getClass() != StandardBrush.class)
          delete = ((Brush) player.getAttribute("brush")).getUseForDelete();
        Brush newBrush;
        if (brush.equals("square"))
          newBrush = new SquareBrush();
        else if (brush.equals("diamond"))
          newBrush = new DiamondBrush();
        else if (brush.equals("line"))
          newBrush = new LineBrush();
        else if (brush.equals("flat"))
          newBrush = new FlatBrush();
        else if (brush.equals("box"))
          newBrush = new BoxBrush();
        else {
          player.getActionSender().sendChatMessage("/brush type [square|diamond|line|flat]");
          return;
        }
        newBrush.setRadius(bRadius);
        newBrush.useForDelete(delete);
        player.setAttribute("brush", newBrush);
        player.getActionSender().sendChatMessage("Brush type changed to " + brush);
      } else
        usage(player);

    } else if (parameters.getArgumentCount() == 3) {
      if (action.equals("dim")) {
        String axis = parameters.getStringArgument(1);
        int newSize;
        try {
          newSize = parameters.getIntegerArgument(2);
        } catch (Exception e) {
          player.getActionSender().sendChatMessage("Error: Could not read number");
          player.getActionSender().sendChatMessage("/brush dim [width|height|length] x");
          return;
        }
        Brush brush = (Brush) player.getAttribute("brush");
        if (axis.equals("length")) {
          brush.setLength(newSize);
        } else if (axis.equals("width")) {
          brush.setWidth(newSize);
        } else if (axis.equals("height")) {
          brush.setHeight(newSize);
        } else
          player.getActionSender().sendChatMessage("/brush dim [width|height|length] x");
      } else
        usage(player);
    } else if (parameters.getArgumentCount() == 4) {
      if (action.equals("dim")) {
        int w, h, l;
        try {
          w = parameters.getIntegerArgument(1);
          h = parameters.getIntegerArgument(2);
          l = parameters.getIntegerArgument(3);
        } catch (Exception e) {
          player.getActionSender().sendChatMessage("Error: Could not read numbers");
          player.getActionSender().sendChatMessage("/brush dim width height length");
          return;
        }
        Brush brush = (Brush) player.getAttribute("brush");
        brush.setWidth(w);
        brush.setHeight(h);
        brush.setLength(l);
      }
    } else
      usage(player);
  }
}
