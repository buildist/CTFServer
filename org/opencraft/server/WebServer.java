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
package org.opencraft.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.ConsoleActionSender;
import org.opencraft.server.task.impl.RenderMapTask;

public class WebServer {
    private static Player consolePlayer = new Player(null, "WebConsole");
    private static ExecutorService executor;
    public static ArrayList<String> blockedWords = new ArrayList<String>();
    public static void init() {
        consolePlayer.setActionSender(new ConsoleActionSender());
        consolePlayer.setAttribute("IsOperator", "true");
        consolePlayer.setAttribute("IsOwner", "true");
        try {
            InetSocketAddress addr = new InetSocketAddress(22000);
            HttpServer server = HttpServer.create(addr, 0);
            
            CTFHandler ch = new CTFHandler();
            HttpContext c = server.createContext("/", ch);
            c.getFilters().add(new ParameterFilter());
            executor = Executors.newCachedThreadPool();
            server.setExecutor(executor);
            server.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void run(Runnable r) {
        if(!Configuration.getConfiguration().isTest())
        executor.submit(r);
    }
    static class CTFHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, Object> params = (Map<String, Object>) exchange.getAttribute("parameters");
                if(!params.containsKey("k") || !params.get("k").equals(Integer.toString(Constants.SECRET))) {
                    exchange.close();
                    return;
                }
                if(params.containsKey("x")) {
                    String message = Server.cleanColorCodes(params.get("x").toString());
                    String messageLower = message.toLowerCase();
                    for(String word : blockedWords) {
                        if(messageLower.contains(word)) {
                            exchange.close();
                            return;
                        }
                    }
                    if(message.equals("stop"))
                    {
                        Server.stop();
                    }
                    else if (message.startsWith("/")) {
                            // interpret as command
                            String tokens = message.substring(1);
                            String[] parts = tokens.split(" ");
                            final Map<String, Command> commands = World.getWorld().getGameMode().getCommands();
                            Command c = commands.get(parts[0]);
                            if (c != null) {
                                    parts[0] = null;
                                    List<String> partsList = new ArrayList<String>();
                                    for (String s : parts) {
                                            if (s != null) {
                                                    partsList.add(s);
                                            }
                                    }
                                    parts = partsList.toArray(new String[0]);
                                    c.execute(consolePlayer, new CommandParameters(parts));
                            } else {
                                System.out.println("Invalid command.");
                            }
                    } else {
                            World.getWorld().broadcast("(Console) &e"+message);
                            System.out.println(message);
                    }
                    exchange.sendResponseHeaders(200, 0);
                    exchange.close();
                } else if(params.containsKey("t")) {
                    long time = Long.parseLong(params.get("t").toString());
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "text/plain");
                    responseHeaders.set("Access-Control-Allow-Origin", "*");
                    exchange.sendResponseHeaders(200, 0);
                    exchange.getResponseBody().write(Server.getConsoleMessages(time).getBytes());
                    exchange.close();                    
                } else if(params.containsKey("map")) {
                    Headers responseHeaders = exchange.getResponseHeaders();
                    responseHeaders.set("Content-Type", "image/jpeg");
                    exchange.sendResponseHeaders(200, 0);
                    ImageIO.write(RenderMapTask.mapImage, "jpeg", exchange.getResponseBody());
                    exchange.close();                    
                } else {
                    exchange.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                exchange.close();
            }

        }
    }
}
