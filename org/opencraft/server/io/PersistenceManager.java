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
package org.opencraft.server.io;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.opencraft.server.model.BlockDefinition;
import org.opencraft.server.model.BlockManager;
import org.opencraft.server.net.packet.PacketDefinition;
import org.opencraft.server.net.packet.PacketField;
import org.opencraft.server.net.packet.PacketManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * A class which manages XStream persistence.
 *
 * @author Graham Edgecombe
 */
public final class PersistenceManager {

  /**
   * The singleton instance.
   */
  private static final PersistenceManager INSTANCE = new PersistenceManager();
  /**
   * The XStream object.
   */
  private final XStream xstream = new XStream(new DomDriver());

  /**
   * Initializes the persistence manager.
   */
  private PersistenceManager() {
    xstream.alias("packets", PacketManager.class);
    xstream.alias("packet", PacketDefinition.class);
    xstream.alias("field", PacketField.class);
    xstream.alias("blocks", BlockManager.class);
    xstream.alias("block", BlockDefinition.class);
  }

  /**
   * Gets the persistence manager instance.
   *
   * @return The persistence manager instance.
   */
  public static PersistenceManager getPersistenceManager() {
    return INSTANCE;
  }

  /**
   * Loads an object from an XML file.
   *
   * @param file The file.
   * @return The object.
   */
  public Object load(String file) {
    try {
      return xstream.fromXML(new FileInputStream(file));
    } catch (FileNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Saves an object to an XML file.
   *
   * @param file The file.
   * @param o    The object.
   */
  public void save(String file, Object o) {
    try {
      xstream.toXML(o, new FileOutputStream(file));
    } catch (FileNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets the xstream object.
   *
   * @return the xstream object.
   */
  public XStream getXStream() {
    return xstream;
  }

}
