/*
 * Parallel Bitset Operations
 * Copyright (C) 2011 Federico Fissore
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */

package org.apache.lucene.contrib.bitset;

import java.lang.reflect.Array;

class ArrayUtils {

  @SuppressWarnings({"unchecked"})
  public static <T> T[] typedArray(Object[] src) {
    T[] dest = (T[]) Array.newInstance(src[0].getClass(), src.length);
    System.arraycopy(src, 0, dest, 0, src.length);
    return dest;
  }

  @SuppressWarnings({"unchecked"})
  public static <T> T[] flatten(T[][] src) {
    int length = 0;
    for (T[] t : src) {
      length += t.length;
    }

    Object[] result = new Object[length];
    int lastIndex = 0;
    for (T[] partial : src) {
      System.arraycopy(partial, 0, result, lastIndex, partial.length);
      lastIndex += partial.length;
    }

    return typedArray(result);
  }

}
