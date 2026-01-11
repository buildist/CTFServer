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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single packet.
 *
 * @author Graham Edgecombe
 */
public class Packet {

  /** The packet definition. */
  public final PacketDefinition definition;

  /** A map of field name to field data. */
  public final Map<String, Object> fields;

  /**
   * Creates the packet.
   *
   * @param definition The definition.
   * @param fields The field map.
   */
  public Packet(PacketDefinition definition, Map<String, Object> fields) {
    this.definition = definition;
    this.fields = Collections.unmodifiableMap(new HashMap<String, Object>(fields));
  }

  /**
   * Gets the definition of this packet.
   *
   * @return The definition of this packet.
   */
  public PacketDefinition getDefinition() {
    return definition;
  }

  /**
   * Gets a numeric field.
   *
   * @param fieldName The name of the field.
   * @return The value of the numeric field.
   */
  public Number getNumericField(String fieldName) {
    return (Number) fields.get(fieldName);
  }

  /**
   * Gets a string field.
   *
   * @param fieldName The name of the field.
   * @return The value of the string field.
   */
  public String getStringField(String fieldName) {
    return (String) fields.get(fieldName);
  }

  /**
   * Gets a byte array field.
   *
   * @param fieldName The name of the field.
   * @return The value of the byte array field.
   */
  public byte[] getByteArrayField(String fieldName) {
    return (byte[]) fields.get(fieldName);
  }

  public int getLength() {
    return definition.getLength() + 1;
  }

  public byte[] toByteArray() {
    byte[] result;
    try (ByteArrayOutputStream stream0 = new ByteArrayOutputStream(getLength())) {
      DataOutputStream stream = new DataOutputStream(stream0);

      PacketDefinition def = getDefinition();
      stream.write(def.getOpcode());
      for (PacketField field : def.getFields()) {
        switch (field.getType()) {
          case BYTE:
            stream.write(getNumericField(field.getName()).byteValue());
            break;
          case SHORT:
            stream.writeShort(getNumericField(field.getName()).shortValue());
            break;
          case INT:
            stream.writeInt(getNumericField(field.getName()).intValue());
            break;
          case LONG:
            stream.writeLong(getNumericField(field.getName()).longValue());
            break;
          case BYTE_ARRAY_256:
            byte[] data2 = getByteArrayField(field.getName());
            byte[] resized2 = Arrays.copyOf(data2, 256);
            stream.write(resized2);
            break;
          case BYTE_ARRAY_320:
            byte[] data3 = getByteArrayField(field.getName());
            byte[] resized3 = Arrays.copyOf(data3, 320);
            stream.write(resized3);
            break;
          case BYTE_ARRAY:
            byte[] data = getByteArrayField(field.getName());
            byte[] resized = Arrays.copyOf(data, 1024);
            stream.write(resized);
            break;
          case STRING:
            String str = getStringField(field.getName());
            byte[] bytes = str.getBytes("Cp437");
            for (int i = 0; i < 64; i++) {
              if (i >= bytes.length) {
                stream.write(0x20);
              } else {
                stream.write(bytes[i]);
              }
            }
            break;
        }
      }
      result = stream0.toByteArray();
    } catch (IOException e) { // should never happen
      throw new RuntimeException(e);
    }

    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(definition.getName()).append(" [");
    for (PacketField field : definition.getFields()) {
      builder.append(field.getName()).append("=").append(fields.get(field.getName())).append(" ");
    }
    builder.append("]");
    return builder.toString();
  }
}
