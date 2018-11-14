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
package org.opencraft.server.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.opencraft.server.Server;

/**
 * Manages the task queue.
 *
 * @author Graham Edgecombe
 */
public final class TaskQueue {

  /**
   * The task queue singleton.
   */
  private static final TaskQueue INSTANCE = new TaskQueue();
  /**
   * The scheduled executor service backing this task queue.
   */
  private ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

  /**
   * Default private constructor.
   */
  private TaskQueue() {
        /* empty */
  }

  /**
   * Gets the task queue instance.
   *
   * @return The task queue instance.
   */
  public static TaskQueue getTaskQueue() {
    return INSTANCE;
  }

  /**
   * Pushes a task onto the task queue.
   *
   * @param task The task to be executed.
   */
  public void push(final Task task) {
    service.submit(new Runnable() {
      public void run() {
        try {
          task.execute();
        } catch (Throwable t) {
          Server.log("[E] Error during task execution." + t);
          Server.log(t);
        }
      }
    });
  }

  /**
   * Schedules a task to run in the future.
   *
   * @param task The scheduled task.
   */
  public void schedule(final ScheduledTask task) {
    schedule(task, task.getDelay());
  }

  /**
   * Internally schedules the task.
   *
   * @param task  The task.
   * @param delay The remaining delay.
   */
  private void schedule(final ScheduledTask task, final long delay) {
    service.schedule(new Runnable() {
      public void run() {
        long start = System.currentTimeMillis();
        try {
          task.execute();
        } catch (Throwable t) {
          System.err.println("Error during task execution." + t);
          Server.log(t);
        }
        if (!task.isRunning()) {
          return;
        }
        long elapsed = System.currentTimeMillis() - start;
        long waitFor = task.getDelay() - elapsed;
        if (waitFor < 0) {
          waitFor = 0;
        }
        schedule(task, waitFor);
      }
    }, delay, TimeUnit.MILLISECONDS);
  }

}