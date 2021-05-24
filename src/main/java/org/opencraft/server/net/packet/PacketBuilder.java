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
package org.opencraft.server.net.packet;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for creating <code>Packet</code> objects.
 *
 * @author Graham Edgecombe
 */
public final class PacketBuilder {

  /** The packet definition. */
  private PacketDefinition definition;

  /** The values. */
  private Map<String, Object> values = new HashMap<String, Object>();

  /**
   * Creates the packet builder.
   *
   * @param definition The packet definition.
   */
  public PacketBuilder(PacketDefinition definition) {
    this.definition = definition;
  }

  /**
   * Creates a packet object based on this builder.
   *
   * @return The packet object.
   */
  public Packet toPacket() {
    return new Packet(definition, values);
  }

  /**
   * Validates that a field has been used correctly.
   *
   * @param name The field name.
   * @param type The data type.
   */
  private void validateField(String name, DataType type) {
    for (PacketField f : definition.getFields()) {
      if (f.getName().equals(name)) {
        if (f.getType().equals(type)) {
          return;
        } else {
          throw new IllegalArgumentException(
              "Incorrect data type - expecting " + f.getType() + "" + ".");
        }
      }
    }
    throw new IllegalArgumentException("No field named " + name + ".");
  }

  /**
   * Puts a byte.
   *
   * @param name The name.
   * @param value The value.
   */
  public PacketBuilder putByte(String name, int value) {
    validateField(name, DataType.BYTE);
    values.put(name, (byte) value);
    return this;
  }

  /**
   * Puts a short.
   *
   * @param name The name.
   * @param value The value.
   */
  public PacketBuilder putShort(String name, int value) {
    validateField(name, DataType.SHORT);
    values.put(name, (short) value);
    return this;
  }

  /**
   * Puts an integer.
   *
   * @param name The name.
   * @param value The value.
   */
  public PacketBuilder putInt(String name, int value) {
    validateField(name, DataType.INT);
    values.put(name, value);
    return this;
  }

  /**
   * Puts a long.
   *
   * @param name The name.
   * @param value The value.
   */
  public PacketBuilder putLong(String name, long value) {
    validateField(name, DataType.LONG);
    values.put(name, value);
    return this;
  }

  /**
   * Puts a string.
   *
   * @param name The name.
   * @param value The string.
   */
  public PacketBuilder putString(String name, String value) {
    validateField(name, DataType.STRING);
    if (value.length() > 64) {
      throw new IllegalArgumentException("String exceeds maximum length of 64 characters.");
    }
    values.put(name, value);
    return this;
  }

  /**
   * Puts a byte array.
   *
   * @param name The name.
   * @param value The byte array.
   */
  public PacketBuilder putByteArray(String name, byte[] value) {
    validateField(name, DataType.BYTE_ARRAY);
    if (value.length > 1024) {
      throw new IllegalArgumentException("Byte array exceeds maximum length of 1024 characters.");
    }
    values.put(name, value);
    return this;
  }
}
