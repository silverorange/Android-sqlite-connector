/**
 * Adapted from https://github.com/OutSystems/Android-sqlite-native-driver/pull/1/
 * 
 * Gluegen can generate classes for returned structs but it requires adding
 * a runtime dependency. To avoid this, we are manually creating bindings.
 */

package io.liteglue;

public class SQLiteResponse {
  private int result;
  private long handle;

  public SQLiteResponse(int result, long handle) {
    this.result = result;
    this.handle = handle;
  }

  public int getResult() {
      return this.result;
  }

  public long getHandle() {
      return this.handle;
  }
}
