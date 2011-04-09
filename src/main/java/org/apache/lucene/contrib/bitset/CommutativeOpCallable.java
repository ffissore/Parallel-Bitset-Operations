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

import org.apache.lucene.contrib.bitset.ops.CommutativeOp;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.concurrent.Callable;

public class CommutativeOpCallable implements Callable<OpenBitSetDISI> {

  private final CommutativeOp operation;
  private final DocIdSet[] bs;
  private final int finalBitsetSize;
  private final int startIndex;
  private final int toIndex;

  public CommutativeOpCallable(DocIdSet[] bs, int startIndex, int toIndex, int finalBitsetSize, CommutativeOp operation) {
    this.startIndex = startIndex;
    this.toIndex = toIndex;
    this.finalBitsetSize = finalBitsetSize;
    this.operation = operation;
    this.bs = bs;

    if (bs.length == 0) {
      throw new IllegalArgumentException("DocIdSet array cannot be empty");
    }
  }

  @Override
  public OpenBitSetDISI call() throws Exception {
    OpenBitSetDISI accumulator = operation.newAccumulator(finalBitsetSize, bs[startIndex]);
    for (int i = startIndex + 1; i < toIndex; i++) {
      operation.compute(accumulator, bs[i]);
    }
    return accumulator;
  }

}