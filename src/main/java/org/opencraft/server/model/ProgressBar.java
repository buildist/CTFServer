package org.opencraft.server.model;

public class ProgressBar {
  private float displayValue;
  private int targetValue;
  private float valueDiff;

  public void set(int value) {
    targetValue = value;
    valueDiff = Math.abs(displayValue - value);
  }

  public int getTargetValue() {
    return targetValue;
  }

  public float get() {
    return displayValue;
  }

  public void update() {
    if (displayValue < targetValue) {
      if (displayValue + valueDiff > targetValue) {
        displayValue = targetValue;
      } else {
        displayValue += valueDiff / 8;
      }
    } else {
      if (displayValue - valueDiff < targetValue) {
        displayValue = targetValue;
      } else {
        displayValue -= valueDiff / 8;
      }
    }
  }
}
