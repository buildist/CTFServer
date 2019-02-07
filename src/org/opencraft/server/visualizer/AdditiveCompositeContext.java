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
package org.opencraft.server.visualizer;

import java.awt.*;
import java.awt.image.*;

import java.awt.CompositeContext;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

public class AdditiveCompositeContext implements CompositeContext {
  public AdditiveCompositeContext() {};

  public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
    int w1 = src.getWidth();
    int h1 = src.getHeight();
    int chan1 = src.getNumBands();
    int w2 = dstIn.getWidth();
    int h2 = dstIn.getHeight();
    int chan2 = dstIn.getNumBands();

    int minw = Math.min(w1, w2);
    int minh = Math.min(h1, h2);
    int minCh = Math.min(chan1, chan2);

    // This bit is horribly inefficient,
    // getting individual pixels rather than all at once.
    for (int x = 0; x < dstIn.getWidth(); x++) {
      for (int y = 0; y < dstIn.getHeight(); y++) {
        float[] pxSrc = null;
        pxSrc = src.getPixel(x, y, pxSrc);
        float[] pxDst = null;
        pxDst = dstIn.getPixel(x, y, pxDst);

        float alpha = 255;
        if (pxSrc.length > 3) {
          alpha = pxSrc[3];
        }

        for (int i = 0; i < 3 && i < minCh; i++) {
          pxDst[i] = Math.min(255, (pxSrc[i] * (alpha / 255)) + (pxDst[i]));
          dstOut.setPixel(x, y, pxDst);
        }
      }
    }
  }

  public void dispose() {}
}
