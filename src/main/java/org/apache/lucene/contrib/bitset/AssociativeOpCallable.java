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

import org.apache.lucene.contrib.bitset.ops.AssociativeOp;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

class AssociativeOpCallable extends AbstractOpCallable<OpenBitSetDISI> {

  private final AssociativeOp operation;

  public AssociativeOpCallable(DocIdSet[] bs, int fromIndex, int toIndex, int finalBitsetSize, AssociativeOp operation) {
    super(bs, fromIndex, toIndex, finalBitsetSize);
    this.operation = operation;
  }

  @Override
  public OpenBitSetDISI call() throws Exception {
    OpenBitSetDISI accumulator = operation.newAccumulator(finalBitsetSize, bs[fromIndex]);
    for (int i = fromIndex + 1; i < toIndex; i++) {
      operation.compute(accumulator, bs[i]);
    }
    return accumulator;
  }

}