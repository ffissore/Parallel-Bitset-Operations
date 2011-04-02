package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.concurrent.Callable;

public class CallableOperation implements Callable<OpenBitSetDISI> {

  private final BitSetOperation operation;
  private final DocIdSet[] bs;
  private final int finalBitsetSize;
  private final int startIndex;
  private final int toIndex;

  public CallableOperation(DocIdSet[] bs, int startIndex, int toIndex, int finalBitsetSize, BitSetOperation operation) {
    this.startIndex = startIndex;
    this.toIndex = toIndex;
    this.finalBitsetSize = finalBitsetSize;
    this.operation = operation;
    this.bs = bs;
  }

  @Override
  public OpenBitSetDISI call() throws Exception {
    OpenBitSetDISI accumulator = operation.newAccumulator(finalBitsetSize);
    for (int i = startIndex; i < toIndex; i++) {
      operation.compute(accumulator, bs[i]);
    }
    return accumulator;
  }

}