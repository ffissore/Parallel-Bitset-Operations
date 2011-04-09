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

import org.apache.lucene.contrib.bitset.ops.ComparisonOp;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.concurrent.Callable;

public class ComparisonOpCallable<T> implements Callable<T[]> {

  private final DocIdSet[] bs;
  private final int fromIndex;
  private final int toIndex;
  private final int finalBitsetSize;
  private final OpenBitSet toCompare;
  private final ComparisonOp<T> operation;

  public ComparisonOpCallable(DocIdSet[] bs, int fromIndex, int toIndex, int finalBitsetSize, OpenBitSet toCompare, ComparisonOp<T> operation) {
    this.bs = bs;
    this.fromIndex = fromIndex;
    this.toIndex = toIndex;
    this.finalBitsetSize = finalBitsetSize;
    this.toCompare = toCompare;
    this.operation = operation;

    if (bs.length == 0) {
      throw new IllegalArgumentException("DocIdSet array cannot be empty");
    }
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public T[] call() throws Exception {
    OpenBitSetDISI accumulator = new OpenBitSetDISI(finalBitsetSize);

    OpenBitSetDISI toCompareDisi = new OpenBitSetDISI(finalBitsetSize);
    toCompareDisi.inPlaceOr(toCompare.iterator());

    Object[] result = new Object[toIndex - fromIndex];
    for (int i = fromIndex; i < toIndex; i++) {
      result[i - fromIndex] = operation.compute(accumulator, bs[i], toCompareDisi);
    }

    return ArrayUtils.typedArray(result, (Class<T>) result[0].getClass());
  }
}
