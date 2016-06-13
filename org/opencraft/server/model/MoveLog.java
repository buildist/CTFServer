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
package org.opencraft.server.model;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoveLog {
    private static MoveLog instance = new MoveLog();
    public static MoveLog getInstance() {
        return instance;
    }
    private BufferedWriter writer;
    public MoveLog() {
        try {
            FileOutputStream out = new FileOutputStream("moves.txt", true);
            writer = new BufferedWriter(new OutputStreamWriter(out));
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    public void logPosition(Player p) {
        /*if(p.getPosition().equals(p.getOldPosition()))
            return;
        synchronized(writer) {
            String l = p.getName()+" "+p.team+" "+p.getPosition()+" "+p.hasFlag;
            try {
                writer.write(l+"\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }*/
    }
    public void logMapChange(String name) {
        /*synchronized(writer) {
            String l = name;
            try {
                writer.flush();
                writer.write(l+"\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }*/
    }
    public void flush() {
        /*synchronized(writer) {
            try {
                writer.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }*/
    }
    public void finalize() {
        try {
            if(writer != null)
                writer.close();
        }
        catch(IOException ex ){
            ex.printStackTrace();
        }
    }
}
