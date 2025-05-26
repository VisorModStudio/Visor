package me.phoenixra.atumvr.api.exceptions;

public class VRException extends RuntimeException {
    public VRException(String message) {
        super(message);
    }
  public VRException(String message, Throwable origin) {
    super(message, origin);
  }
}
