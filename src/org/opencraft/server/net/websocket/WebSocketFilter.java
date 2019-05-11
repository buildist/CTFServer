package org.opencraft.server.net.websocket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.DefaultWriteRequest;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.io.UnsupportedEncodingException;
import java.util.Map;


public class WebSocketFilter extends CumulativeIncomeFilter {

  private static class ParsedFrame {
    byte opCode;
    IoBuffer unMaskedPayLoad;
  }

  private static ParsedFrame buildWSDataBuffer(IoBuffer in, IoSession session) {
    ParsedFrame parsedFrame = new ParsedFrame();
    IoBuffer resultBuffer = null;
    do {
      byte frameInfo = in.get();
      byte opCode = (byte) (frameInfo & 0x0f);
      parsedFrame.opCode = opCode;
      if (opCode == 8) {
        // opCode 8 means close. See RFC 6455 Section 5.2
        // return what ever is parsed till now.
        session.close(true);
        return null;
      }

      if (opCode == 9) { // PING
        // TODO handler ping frame
        return parsedFrame;
      }
      int frameLen = (in.get() & (byte) 0x7F);
      if (frameLen == 126) {
        frameLen = in.getShort();
      }

      // Validate if we have enough data in the buffer to completely
      // parse the WebSocket DataFrame. If not return null.
      if (frameLen + 4 > in.remaining()) {
        return null;
      }
      byte mask[] = new byte[4];
      for (int i = 0; i < 4; i++) {
        mask[i] = in.get();
      }

            /*  now un-mask frameLen bytes as per Section 5.3 RFC 6455
                Octet i of the transformed data ("transformed-octet-i") is the XOR of
                octet i of the original data ("original-octet-i") with octet at index
                i modulo 4 of the masking key ("masking-key-octet-j"):

                j                   = i MOD 4
                transformed-octet-i = original-octet-i XOR masking-key-octet-j
            * 
            */

      byte[] unMaskedPayLoad = new byte[frameLen];
      for (int i = 0; i < frameLen; i++) {
        byte maskedByte = in.get();
        unMaskedPayLoad[i] = (byte) (maskedByte ^ mask[i % 4]);
      }

      if (resultBuffer == null) {
        resultBuffer = IoBuffer.wrap(unMaskedPayLoad);
        resultBuffer.position(resultBuffer.limit());
        resultBuffer.setAutoExpand(true);
      } else {
        resultBuffer.put(unMaskedPayLoad);
      }
    }
    while (in.hasRemaining());

    resultBuffer.flip();
    parsedFrame.unMaskedPayLoad = resultBuffer;
    return parsedFrame;

  }

  private void sendReponse(
      WebSocketHandShakeResponse wsResponse, IoSession session, NextFilter nextFilter)
      throws UnsupportedEncodingException {
    byte[] bytes = wsResponse.getResponse().getBytes("utf-8");
    IoBuffer buf = IoBuffer.allocate(bytes.length);
    buf.put(bytes);
    buf.flip();
    nextFilter.filterWrite(session, new DefaultWriteRequest(buf));
  }

  /**
   * Try parsing the message as a websocket handshake request. If it is such a request, then send
   * the corresponding handshake response (as in Section 4.2.2 RFC 6455).
   */
  private boolean tryWebSockeHandShake(IoSession session, IoBuffer in, NextFilter nextFilter) {
    try {
      String payLoadMsg = new String(in.array());
      Map<String, String> headers = WebSocketUtils.parseRequest(payLoadMsg);

      String socketKey = headers.get("Sec-WebSocket-Key");

      if (socketKey == null || socketKey.length() <= 0) {
        return false;
      }

      String challengeAccept = WebSocketUtils.getWebSocketKeyChallengeResponse(socketKey);
      WebSocketHandShakeResponse wsResponse = WebSocketUtils.buildWSHandshakeResponse
          (challengeAccept);
      session.setAttribute(WebSocketUtils.SessionAttribute, true);
      sendReponse(wsResponse, session, nextFilter);
      return true;
    } catch (Exception e) {
      // input is not a websocket handshake request.
      return false;
    }
  }

  @Override
  public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest request) {
    if (session.containsAttribute(WebSocketUtils.SessionAttribute)
        && session.getAttribute(WebSocketUtils.SessionAttribute) == Boolean.TRUE) {
      IoBuffer resultBuffer = WebSocketFilter.buildWSDataFrameBuffer((IoBuffer) request
          .getMessage());
      nextFilter.filterWrite(
          session,
          new DefaultWriteRequest(resultBuffer, request.getFuture(), request.getDestination())
      );
    } else {
      nextFilter.filterWrite(session, request);
    }
  }

  // Encode the in buffer according to the Section 5.2. RFC 6455
  private static IoBuffer buildWSDataFrameBuffer(IoBuffer buf) {
    IoBuffer buffer = IoBuffer.allocate(buf.limit() + 2, false);
    buffer.setAutoExpand(true);
    buffer.put((byte) 0x82);
    if (buffer.capacity() <= 125) {
      byte capacity = (byte) (buf.limit());
      buffer.put(capacity);
    } else {
      buffer.put((byte) 126);
      buffer.putShort((short) buf.limit());
    }
    buffer.put(buf);
    buffer.flip();
    return buffer;
  }

  @Override
  protected boolean doDecode(IoSession session, IoBuffer in, NextFilter nextFilter,
                             ProtocolDecoderOutput out) throws Exception {
    IoBuffer resultBuffer;
    if (!session.containsAttribute(WebSocketUtils.SessionAttribute)) {
      // first message on a new connection. see if its from a websocket or a
      // native socket.
      // if(tryWebSockeHandShake(session, in, out)){
      if (tryWebSockeHandShake(session, in, nextFilter)) {
        // websocket handshake was successful. Don't write anything to output
        // as we want to abstract the handshake request message from the handler.
        in.position(in.limit());
        return true;
      } else {
        // message is from a native socket. Simply wrap and pass through.
        resultBuffer = IoBuffer.wrap(in.array(), 0, in.limit());
        in.position(in.limit());
        session.setAttribute(WebSocketUtils.SessionAttribute, false);
      }
    } else if (session.containsAttribute(WebSocketUtils.SessionAttribute)
        && session.getAttribute(WebSocketUtils.SessionAttribute) == Boolean.TRUE) {
      // there is incoming data from the websocket. Decode and send to handler or next filter.
      int startPos = in.position();
      // resultBuffer = buildWSDataBuffer(in, session);
      ParsedFrame parsedFrame = buildWSDataBuffer(in, session);
      if (parsedFrame == null) {
        // There was not enough data in the buffer to parse. Reset the in buffer
        // position and wait for more data before trying again.
        in.position(startPos);
        return false;
      }
      if (parsedFrame.opCode == 9) { // PING
        // do nothing instead of sending a PONG. It works fine with firefox 38.0.5
        // TODO send a PONG frame
        return true;
      } else if (parsedFrame.opCode == 0xA) { // PONG
        return true;
      }
      resultBuffer = parsedFrame.unMaskedPayLoad;
    } else {
      // session is known to be from a native socket. So
      // simply wrap and pass through.
      resultBuffer = IoBuffer.wrap(in.array(), 0, in.limit());
      in.position(in.limit());
    }

    if (resultBuffer != null) {
      out.write(resultBuffer);
      return true;
    } else {
      return false;
    }
  }
}
