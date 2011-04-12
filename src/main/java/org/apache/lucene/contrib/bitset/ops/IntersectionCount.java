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

package org.apache.lucene.contrib.bitset.ops;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;

import java.io.IOException;

public class IntersectionCount implements ComparisonOp<Long> {

  @Override
  public Long compute(OpenBitSetDISI accumulator, DocIdSet target, OpenBitSet toCompare) throws IOException {
    if (target instanceof OpenBitSet) {
      return OpenBitSet.intersectionCount((OpenBitSet) target, toCompare);
    } else if (target instanceof SortedVIntList) {
      accumulator.clear(0, accumulator.capacity());
      accumulator.inPlaceOr(target.iterator());
      return OpenBitSet.intersectionCount(accumulator, toCompare);
    } else {
      throw new IllegalArgumentException("Not supported:" + target);
    }
  }
}
