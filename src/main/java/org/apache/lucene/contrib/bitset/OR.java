package org.apache.lucene.contrib.bitset;

import java.io.IOException;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;

public class OR implements BitSetOperation {

  @Override
  public void compute(OpenBitSetDISI accumulator, DocIdSet bitset) throws IOException {
    if (bitset instanceof OpenBitSet) {
      accumulator.or((OpenBitSet) bitset);
    } else if (bitset instanceof SortedVIntList) {
      accumulator.inPlaceOr(bitset.iterator());
    } else {
      throw new IllegalArgumentException("Not supported:" + bitset);
    }
  }

}
