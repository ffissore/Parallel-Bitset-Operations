package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.concurrent.Callable;

public class CallableComparison implements Callable<long[]> {

  private final DocIdSet[] bs;
  private final int startIndex;
  private final int toIndex;
  private final int finalBitsetSize;
  private final OpenBitSet toCompare;
  private final BitSetComparisonOperation operation;

  public CallableComparison(DocIdSet[] bs, int startIndex, int toIndex, int finalBitsetSize, OpenBitSet toCompare, BitSetComparisonOperation operation) {
    this.bs = bs;
    this.startIndex = startIndex;
    this.toIndex = toIndex;
    this.finalBitsetSize = finalBitsetSize;
    this.toCompare = toCompare;
    this.operation = operation;
  }

  @Override
  public long[] call() throws Exception {

    OpenBitSetDISI accumulator = new OpenBitSetDISI(finalBitsetSize);

    OpenBitSetDISI toCompareDisi = new OpenBitSetDISI(finalBitsetSize);
    toCompareDisi.inPlaceOr(toCompare.iterator());

    long[] result = new long[toIndex - startIndex];
    for (int i = startIndex; i < toIndex; i++) {
      result[i - startIndex] = operation.compute(accumulator, bs[i], toCompareDisi);
    }

    return result;

  }
}
