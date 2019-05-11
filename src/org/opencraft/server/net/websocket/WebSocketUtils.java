package org.opencraft.server.net.websocket;

import org.apache.mina.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

class WebSocketUtils {
  private static final Base64 base64 = new Base64();

  static final String SessionAttribute = "isWEB";

  // Construct a successful websocket handshake response using the key param
  // (See RFC 6455).
  public static WebSocketHandShakeResponse buildForbiddenResponse() {

    String response = "HTTP/1.1 403 Forbidden\r\n";
    response += "Connection: close\r\n";
    response += "Content-Length: 0\r\n";
    response += "\r\n";
    return new WebSocketHandShakeResponse(response);
  }

  static WebSocketHandShakeResponse buildWSHandshakeResponse(String key) {

    String response = "HTTP/1.1 101 Switching Protocols\r\n";

    response += "Upgrade: websocket\r\n";
    response += "Connection: Upgrade\r\n";
    response += "Sec-WebSocket-Accept: " + key + "\r\n";
    response += "Sec-WebSocket-Protocol: ClassiCube\r\n";

    response += "\r\n";
    return new WebSocketHandShakeResponse(response);
  }


  static Map<String, String> parseRequest(String WSRequest) {
    HashMap<String, String> ret = new HashMap<String, String>();
    String[] headers = WSRequest.split("\r\n");
    for (int i = 1; i < headers.length; i++) {
      String line = headers[i];
      int delimiter = line.indexOf(":");
      if (delimiter <= 0)
        break;
      String name = line.substring(0, delimiter);
      String value = line.substring(delimiter + 1).trim();
      ret.put(name, value);
    }
    return ret;
  }

  // Builds the challenge response to be used in WebSocket handshake.
  // First append the challenge with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11" and then
  // make a SHA1 hash and finally Base64 encode it. (See RFC 6455)
  static String getWebSocketKeyChallengeResponse(String challenge) throws
      NoSuchAlgorithmException, UnsupportedEncodingException {
    challenge += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    MessageDigest cript = MessageDigest.getInstance("SHA-1");
    cript.reset();
    cript.update(challenge.getBytes("utf8"));
    byte[] hashedVal = cript.digest();
    return new String(base64.encode(hashedVal));
  }
}