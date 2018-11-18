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
package org.opencraft.server.persistence;


import com.thoughtworks.xstream.XStream;

import org.opencraft.server.Constants;
import org.opencraft.server.io.PersistenceManager;
import org.opencraft.server.model.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The core class of the saved game system.
 *
 * @author Graham Edgecombe
 */
public class SavedGameManager {

  /**
   * The singleton instance of the persistence manager.
   */
  private static final SavedGameManager INSTANCE = new SavedGameManager();
  /**
   * The executor service in which persistence requests are executed.
   */
  private ExecutorService service = Executors.newSingleThreadExecutor();

  /**
   * Gets the saved game manager instance.
   *
   * @return The saved game manager instance.
   */
  public static SavedGameManager getSavedGameManager() {
    return INSTANCE;
  }

  /**
   * Gets the xstream instance.
   *
   * @return The xstream instance.
   */
  public XStream getXStream() {
    return PersistenceManager.getPersistenceManager().getXStream();
  }

  /**
   * Gets the path to a saved game.
   *
   * @param player The player.
   * @return The path to their saved game.
   */
  public String getPath(Player player) {
    return Constants.ROOT_PATH + "/savedGames/" + player.getName().toLowerCase() + ".xml";
  }

  /**
   * Queues a persistence request.
   *
   * @param req The request to queue.
   * @return The future object.
   */
  public Future<?> queuePersistenceRequest(PersistenceRequest req) {
    return service.submit(req);
  }

}
