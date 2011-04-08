package org.apache.lucene.contrib.bitset;

import java.lang.reflect.Array;

class ArrayUtils {

  @SuppressWarnings({"unchecked"})
  public static <T> T[] typedArray(Object[] src, Class<T> clazz) {
    T[] dest = (T[]) Array.newInstance(clazz, src.length);
    System.arraycopy(src, 0, dest, 0, src.length);
    return dest;

  }

}
