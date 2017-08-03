package org.opencraft.server.net;

public class PingList {

  private final PingEntry[] entries = new PingEntry[10];

  public PingList() {
    for (int i = 0; i < entries.length; i++) {
      entries[i] = new PingEntry();
    }
  }

  public int nextData() {
    for (int i = 0; i < entries.length; i++) {
      if (entries[i].sendTime != 0) continue;;

      int prev = i > 0 ? entries[i - 1].data : 0;
      return setPing(i, prev);
    }

    for (int i = 0; i < entries.length - 1; i++) {
      entries[i] = entries[i + 1];
    }

    return setPing(entries.length - 1, entries[entries.length - 1].data);
  }

  private int setPing(int i, int prev) {
    entries[i].data = prev + 1;
    entries[i].sendTime = System.currentTimeMillis();
    entries[i].receiveTime = 0;
    return prev + 1;
  }

  public void update(int data) {
    for (PingEntry entry : entries) {
      if (entry.data != data) continue;

      entry.receiveTime = System.currentTimeMillis();
      return;
    }
  }

  private int lowestPing() {
    int total = Integer.MAX_VALUE;
    for (PingEntry entry : entries) {
      if (entry.sendTime == 0 || entry.receiveTime == 0) continue;

      total = Math.min(total, entry.getLatency());
    }
    return total;
  }

  public int averagePing() {
    int total = 0;
    int count = 0;
    for (PingEntry entry : entries) {
      if (entry.sendTime == 0 || entry.receiveTime == 0) continue;

      total += entry.getLatency();
      count++;
    }
    return count == 0 ? 0 : total / count;
  }

  private int highestPing() {
    int total = 0;
    for (PingEntry entry : entries) {
      if (entry.sendTime == 0 || entry.receiveTime == 0) continue;

      total = Math.max(total, entry.getLatency());
    }
    return total;
  }

  @Override
  public String toString() {
    return String.format(
        "Lowest ping %dms, average %dms, highest %dms", lowestPing(), averagePing(), highestPing());
  }

  private static class PingEntry {
    long sendTime = 0;
    long receiveTime = 0;
    public int data = 0;

    int getLatency() {
      return (int) (receiveTime - sendTime) / 2;
    }
  }
}
