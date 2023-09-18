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
package org.opencraft.server.cmd;

/**
 * An immutable class which holds and parses the parameters passed to a commmand.
 *
 * @author Graham Edgecombe
 */
public class CommandParameters {

  /** Arguments array. */
  public final String[] args;

  /**
   * Creates the command parameters class.
   *
   * @param args The arguments of the command.
   */
  public CommandParameters(String[] args) {
    this.args = args.clone();
  }

  /**
   * Gets the number of arguments.
   *
   * @return The number of arguments.
   */
  public int getArgumentCount() {
    return args.length;
  }

  /**
   * Gets the string argument at <code>pos</code>.
   *
   * @param pos The index in the array.
   * @return The argument.
   */
  public String getStringArgument(int pos) {
    return args[pos];
  }

  /**
   * Gets the double argument at <code>pos</code>.
   *
   * @param pos The index in the array.
   * @return The argument.
   * @throws NumberFormatException if the argument is not a double.
   */
  public double getDoubleArgument(int pos) {
    return Double.valueOf(args[pos]);
  }

  /**
   * Gets the integer argument at <code>pos</code>.
   *
   * @param pos The index in the array.
   * @return The argument.
   * @throws NumberFormatException if the argument is not an integer.
   */
  public int getIntegerArgument(int pos) {
    return Integer.valueOf(args[pos]);
  }
}
