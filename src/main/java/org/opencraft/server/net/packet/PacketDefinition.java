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

/**
 * Represents a type of packet.
 *
 * @author Graham Edgecombe
 */
public final class PacketDefinition {

  /** The opcode of this packet. */
  private final int opcode;

  /** The name of this packet. */
  private final String name;

  /** The fields in this packet. */
  private final PacketField[] fields;

  /** The length of this packet. */
  private final transient int length;

  /**
   * Creates the packet definition.
   *
   * @param opcode The opcode.
   * @param name The name.
   * @param fields The fields.
   */
  public PacketDefinition(int opcode, String name, PacketField[] fields) {
    this.opcode = opcode;
    this.name = name;
    this.fields = fields;
    // compute packet length
    int length = 0;
    for (PacketField field : fields) {
      length += field.getType().getLength();
    }
    this.length = length;
  }

  /**
   * Resolves this object.
   *
   * @return The object with the correct packet length.
   */
  private Object readResolve() {
    // ensures length is computed
    return new PacketDefinition(opcode, name, fields);
  }

  /**
   * Gets the opcode of this packet.
   *
   * @return The opcode of this packet.
   */
  public int getOpcode() {
    return opcode;
  }

  /**
   * Gets the name of this packet.
   *
   * @return The name of this packet.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the fields in this packet.
   *
   * @return The fields in this packet.
   */
  public PacketField[] getFields() {
    return fields;
  }

  /**
   * Gets the length of this packet.
   *
   * @return The length of this packet, in bytes.
   */
  public int getLength() {
    return length;
  }
}
