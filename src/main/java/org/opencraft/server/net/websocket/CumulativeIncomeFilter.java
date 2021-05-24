package org.opencraft.server.net.websocket;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.filterchain.IoFilterChain;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.AbstractProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.util.Queue;


abstract class CumulativeIncomeFilter extends IoFilterAdapter {
  private final AttributeKey BUFFER = new AttributeKey(getClass(), "buffer");

  @Override
  public void onPostRemove(IoFilterChain parent, String name, NextFilter nextFilter)
      throws Exception {
    removeSessionBuffer(parent.getSession());
  }

  @Override
  public void sessionClosed(NextFilter nextFilter, IoSession session)
      throws Exception {
    removeSessionBuffer(session);
    nextFilter.sessionClosed(session);
  }

  private void storeRemainingInSession(IoBuffer buf, IoSession session) {
    final IoBuffer remainingBuf = IoBuffer.allocate(buf.capacity())
        .setAutoExpand(true);

    remainingBuf.order(buf.order());
    remainingBuf.put(buf);

    session.setAttribute(BUFFER, remainingBuf);
  }

  /**
   * Implement this method to consume the specified cumulative buffer and decode its content into
   * message(s).
   *
   * @param in the cumulative buffer
   * @return <tt>non-null</tt> if and only if there's more to decode in the buffer and you want to
   * have <tt>doDecode</tt> method invoked again. Return <tt>null</tt> if remaining data is not
   * enough to decode, then this method will be invoked again when more data is cumulated.
   * @throws Exception if cannot decode <tt>in</tt>.
   */
  protected abstract boolean doDecode(
      IoSession session, IoBuffer in, NextFilter nextFilter, ProtocolDecoderOutput out)
      throws Exception;

  public void messageReceived(NextFilter nextFilter, IoSession session,
                              Object message) throws Exception {
    if (!(message instanceof IoBuffer)) {
      nextFilter.messageReceived(session, message);
      return;
    }
    ProtocolDecoderOutput decoderOut = getDecoderOut(session);
    synchronized (decoderOut) {
      IoBuffer in = (IoBuffer) message;
      // IoBuffer out = null;
      if (!session.getTransportMetadata().hasFragmentation()) {
        while (in.hasRemaining()) {
          if (doDecode(session, in, nextFilter, decoderOut)) {
            break;
          }
        }

        return;
      }

      boolean usingSessionBuffer = true;
      IoBuffer buf = (IoBuffer) session.getAttribute(BUFFER);

      if (buf != null) {
        boolean appended = false;
        // Make sure that the buffer is auto-expanded.
        if (buf.isAutoExpand()) {
          try {
            buf.put(in);
            appended = true;
          } catch (IllegalStateException | IndexOutOfBoundsException e) {}
        }

        if (appended) {
          buf.flip();
        } else {
          // Reallocate the buffer if append operation failed due to
          // derivation or disabled auto-expansion.
          buf.flip();
          IoBuffer newBuf = IoBuffer.allocate(
              buf.remaining() + in.remaining()).setAutoExpand(
              true);
          newBuf.order(buf.order());
          newBuf.put(buf);
          newBuf.put(in);
          newBuf.flip();
          buf = newBuf;

          // Update the session attribute.
          session.setAttribute(BUFFER, buf);
        }
      } else {
        buf = in;
        usingSessionBuffer = false;
      }

      for (; ; ) {
        int oldPos = buf.position();
        boolean decoded = doDecode(session, buf, nextFilter, decoderOut);
        if (decoded) {
          if (buf.position() == oldPos) {
            throw new IllegalStateException(
                "doDecode() can't return true when buffer is not consumed.");
          }

          if (!buf.hasRemaining()) {
            break;
          }
        } else {
          break;
        }
      }

      // if there is any data left that cannot be decoded, we store
      // it in a buffer in the session and next time this decoder is
      // invoked the session buffer gets appended to
      if (buf.hasRemaining()) {
        if (usingSessionBuffer && buf.isAutoExpand()) {
          buf.compact();
        } else {
          storeRemainingInSession(buf, session);
        }
      } else {
        if (usingSessionBuffer) {
          removeSessionBuffer(session);
        }
      }
    }

    decoderOut.flush(nextFilter, session);
  }

  private void removeSessionBuffer(IoSession session) {
    session.removeAttribute(BUFFER);
  }

  private static class ProtocolDecoderOutputImpl extends
      AbstractProtocolDecoderOutput {
    public void flush(NextFilter nextFilter, IoSession session) {
      Queue<Object> messageQueue = getMessageQueue();

      while (!messageQueue.isEmpty()) {
        nextFilter.messageReceived(session, messageQueue.poll());
      }
    }
  }

  /**
   * Return a reference to the decoder callback. If it's not already created and stored into the
   * session, we create a new instance.
   */
  private ProtocolDecoderOutput getDecoderOut(IoSession session) {
    ProtocolDecoderOutput out = (ProtocolDecoderOutput) session.getAttribute(DECODER_OUT);
    if (out == null) {
      out = new ProtocolDecoderOutputImpl();
      session.setAttribute(DECODER_OUT, out);
    }
    return out;
  }

  private final AttributeKey DECODER_OUT =
      new AttributeKey(ProtocolCodecFilter.class, "decoderOut");
}
