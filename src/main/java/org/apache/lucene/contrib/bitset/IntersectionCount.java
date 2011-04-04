package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.io.IOException;

public class IntersectionCount implements BitSetComparisonOperation {

  @Override
  public long compute(OpenBitSetDISI accumulator, DocIdSet target, OpenBitSet toCompare) throws IOException {
    accumulator.clear(0, accumulator.capacity());
    accumulator.inPlaceOr(target.iterator());
    return OpenBitSet.intersectionCount(accumulator, toCompare);
  }
}
