package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.concurrent.Callable;

public class CallableComparison<T> implements Callable<T[]> {

  private final DocIdSet[] bs;
  private final int startIndex;
  private final int toIndex;
  private final int finalBitsetSize;
  private final OpenBitSet toCompare;
  private final BitSetComparisonOperation<T> operation;

  public CallableComparison(DocIdSet[] bs, int startIndex, int toIndex, int finalBitsetSize, OpenBitSet toCompare, BitSetComparisonOperation<T> operation) {
    this.bs = bs;
    this.startIndex = startIndex;
    this.toIndex = toIndex;
    this.finalBitsetSize = finalBitsetSize;
    this.toCompare = toCompare;
    this.operation = operation;
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public T[] call() throws Exception {

    OpenBitSetDISI accumulator = new OpenBitSetDISI(finalBitsetSize);

    OpenBitSetDISI toCompareDisi = new OpenBitSetDISI(finalBitsetSize);
    toCompareDisi.inPlaceOr(toCompare.iterator());

    T[] result = (T[]) new Object[toIndex - startIndex];
    for (int i = startIndex; i < toIndex; i++) {
      result[i - startIndex] = operation.compute(accumulator, bs[i], toCompareDisi);
    }

    return result;

  }
}
