package org.opencraft.server.net.websocket;

class WebSocketHandShakeResponse {
    
    private String response;
    WebSocketHandShakeResponse(String response){
        this.response = response;
    }
    
    String getResponse(){
        return this.response;
    }
}
