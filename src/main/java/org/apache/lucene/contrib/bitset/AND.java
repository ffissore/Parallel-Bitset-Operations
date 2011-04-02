package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;

import java.io.IOException;

public class AND implements BitSetOperation {

  @Override
  public void compute(OpenBitSetDISI accumulator, DocIdSet bitset) throws IOException {
    if (bitset instanceof OpenBitSet) {
      accumulator.and((OpenBitSet) bitset);
    } else if (bitset instanceof SortedVIntList) {
      accumulator.inPlaceAnd(bitset.iterator());
    } else {
      throw new IllegalArgumentException("Not supported:" + bitset);
    }
  }

  @Override
  public OpenBitSetDISI newAccumulator(int bitsetSize) {
    OpenBitSetDISI bs = new OpenBitSetDISI(bitsetSize);
    long[] bits = bs.getBits();
    for (int i = 0; i < bits.length; i++) {
      bits[i] = Long.MAX_VALUE;
    }
    bs.setBits(bits);
    return bs;
  }

}