package org.opencraft.server.replay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opencraft.server.Server;
import org.opencraft.server.model.Player;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.UnparsedPacket;
import org.opencraft.server.util.Pair;

public class ReplayFile implements Closeable {

  public record ReplayChunk(int deltaMillis, List<Packet> packets) {

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void sleepUntilSendingThisChunk(
        Player watcher, long timestampWhenReplayStarted
    ) throws InterruptedException {
      long delta = System.currentTimeMillis() - timestampWhenReplayStarted;
      long timeToSleep = Math.max(deltaMillis - delta, 0);
      long count100 = timeToSleep / 100L; // actually should never be >0
      long finalTimeToSleep = timeToSleep - (count100 * 100L);

      long syncLatency = 0L;
      while (count100-- > 0) {
        Thread.sleep(100L - syncLatency);

        long timestamp = System.currentTimeMillis();
        synchronized (watcher) {
          if (watcher.requestedToLeaveReplay) {
            return;
          }
        }
        syncLatency = System.currentTimeMillis() - timestamp;
      }

      Thread.sleep(finalTimeToSleep);
    }
  }

  public static final int MAGIC = 0x8A82F239;
  public static final int CURRENT_FILE_VERSION = 1;
  public static final int FAILED_TO_LOCATE_FREE_ID = 0;
  public static final int MIN_ID = 1;
  public static final int MAX_ID = 200;
  public static final int BUFFER_SIZE = 65536;

  public static final String REPLAY_DIRECTORY = "./replays/";

  private final String path;
  private final File file;
  private boolean reading;
  private Closeable stream;
  private boolean headerRead;
  private int fileVersion;
  private String map;
  private long roundStartTimestamp;
  private long recordingStartTimestamp;

  public ReplayFile(int day, int month, int year, int id) {
    Pair<String, String> possibleFilenames = getFilename(day, month, year, id);
    String importantFilename = possibleFilenames.getSecond();
    boolean important = (new File(importantFilename)).exists();

    this.path = (important ? importantFilename : possibleFilenames.getFirst());
    this.file = new File(path);
  }

  public static Pair<String, String> getFilename(int day, int month, int year, int id) {
    String filename = REPLAY_DIRECTORY +
        adjust(day, 2) + "." +
        adjust(month, 2) + "." +
        adjust(year, 4) + "_" +
        id;
    String importantFilename = filename + "_important";

    return Pair.of(filename + ".ltr", importantFilename + ".ltr");
  }

  public static void checkReplayDirectory() {
    File replayDir = new File(REPLAY_DIRECTORY);
    if (!replayDir.isDirectory() && !replayDir.mkdir()) {
      Server.log("Failed to create " + REPLAY_DIRECTORY +
          " directory, this may lead to errors. Please create it manually");
    }
  }

  // only used by ReplayManager
  public static int locateFreeId(int day, int month, int year) {
    for (int id = MIN_ID; id <= MAX_ID; id++) {
      Pair<String, String> possibleFilenames = getFilename(day, month, year, id);

      if (!(new File(possibleFilenames.getFirst())).exists() &&
          !(new File(possibleFilenames.getSecond())).exists()) return id;
    }

    return FAILED_TO_LOCATE_FREE_ID;
  }

  public static List<Integer> availableIds(int day, int month, int year) {
    List<Integer> result = new ArrayList<>();
    for (int id = MIN_ID; id <= MAX_ID; id++) {
      Pair<String, String> possibleFilenames = getFilename(day, month, year, id);

      String generalName = possibleFilenames.getFirst();
      boolean anyFile = (
          (new File(generalName)).exists() ||
          (new File(possibleFilenames.getSecond())).exists()
      );
      if (anyFile && !ReplayManager.getInstance().isBusy(generalName)) {
        result.add(id);
      }
    }

    return result;
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean canRead() {
    return file != null && file.exists() && fileVersion <= CURRENT_FILE_VERSION;
  }

  public void setReading(boolean reading) {
    this.reading = reading;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public void open() throws IOException {
    if (reading && !canRead()) {
      throw new IllegalStateException("Cannot read from this ReplayFile instance");
    }
    if (!reading) {
      file.createNewFile();
    }
    stream = (reading ?
        new DataInputStream(new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) :
        new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE)));
  }

  @SuppressWarnings("unchecked")
  private <T extends Closeable> T castStream() {
    return (T) stream;
  }

  private void checkStream() {
    if (stream == null) {
      throw new IllegalStateException("Stream has not been opened");
    }
  }

  private void checkReading() {
    checkStream();

    if (!reading) {
      throw new IllegalStateException("Not in reading mode");
    }
  }

  private void checkWriting() {
    checkStream();

    if (reading) {
      throw new IllegalStateException("Not in writing mode");
    }
  }

  @SuppressWarnings("resource")
  public void readHeader() throws IOException {
    checkReading();

    if (headerRead) return;

    DataInputStream stream = castStream();
    if (stream.readInt() != MAGIC) return;
    fileVersion = stream.readInt();
    if (fileVersion > CURRENT_FILE_VERSION) return;

    int mapNameLength = stream.readUnsignedByte();
    byte[] mapName = new byte[mapNameLength];
    stream.readFully(mapName);
    map = new String(mapName, StandardCharsets.US_ASCII);
    roundStartTimestamp = stream.readLong();
    recordingStartTimestamp = stream.readLong();

    headerRead = true;
  }

  @SuppressWarnings("resource")
  public boolean isNextChunkAvailable() throws IOException {
    checkReading();

    DataInputStream stream = castStream();

    return stream.available() >= 4;
  }

  @SuppressWarnings("resource")
  public ReplayChunk readNextChunk() throws IOException {
    checkReading();

    DataInputStream stream = castStream();
    int deltaMillis = stream.readInt();
    int packetCount = stream.readShort();
    List<Packet> packets = new ArrayList<>(packetCount);
    for (int i = 0; i < packetCount; i++) {
      int length = stream.readInt();
      byte[] data = new byte[length];
      stream.readFully(data, 0, length);
      packets.add(new UnparsedPacket(data));
    }

    return new ReplayChunk(deltaMillis, Collections.unmodifiableList(packets));
  }

  @SuppressWarnings("resource")
  public void writeHeader() throws IOException {
    checkWriting();

    DataOutputStream stream = castStream();
    stream.writeInt(MAGIC);
    stream.writeInt(CURRENT_FILE_VERSION);

    byte[] mapName = map.getBytes(StandardCharsets.US_ASCII);
    stream.writeByte(mapName.length);
    stream.write(mapName, 0, mapName.length);

    stream.writeLong(roundStartTimestamp);
    stream.writeLong(recordingStartTimestamp);
  }

  @SuppressWarnings("resource")
  public void writeChunk(ReplayChunk chunk) throws IOException {
    checkWriting();

    DataOutputStream stream = castStream();
    stream.writeInt(chunk.deltaMillis);
    stream.writeShort(chunk.packets.size());
    for (Packet packet : chunk.packets) {
      byte[] encodedData = packet.toByteArray();

      stream.writeInt(encodedData.length);
      stream.write(encodedData);
    }
  }

  public String getFilename() {
    return path;
  }

  public int getFileVersion() {
    checkReading();

    return fileVersion;
  }

  public String getMap() {
    checkReading();

    return map;
  }

  public long getRoundStartTimestamp() {
    checkReading();

    return roundStartTimestamp;
  }

  public long getRecordingStartTimestamp() {
    return recordingStartTimestamp;
  }

  public void setMap(String map) {
    checkWriting();

    this.map = map;
  }

  public void setRoundStartTimestamp(long roundStartTimestamp) {
    checkWriting();

    this.roundStartTimestamp = roundStartTimestamp;
  }

  public void setRecordingStartTimestamp(long recordingStartTimestamp) {
    checkWriting();

    this.recordingStartTimestamp = recordingStartTimestamp;
  }

  @Override
  public void close() throws IOException {
    if (stream != null) stream.close();
  }

  /* ======== Util methods ======== */

  public static String adjust(int dateElement, int count) {
    return dateElementToString(String.valueOf(dateElement), count, '0');
  }

  public static String dateElementToString(String element, int minLength, char padding) {
    int length = element.length();
    if (length >= minLength) {
      return element;
    }
    int delta = minLength - length;
    char[] chars = new char[minLength];
    for (int i = 0; i < chars.length; i++) {
      if (i < delta) {
        chars[i] = padding;
      } else {
        chars[i] = element.charAt(i - delta);
      }
    }

    return String.valueOf(chars);
  }
}
