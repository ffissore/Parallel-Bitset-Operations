package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.concurrent.Callable;

public class CallableOperation implements Callable<OpenBitSetDISI> {

  private final BitSetOperation operation;
  private final DocIdSet[] bs;
  private final int finalBitsetSize;

  public CallableOperation(DocIdSet[] bs, int finalBitsetSize, BitSetOperation operation) {
    this.finalBitsetSize = finalBitsetSize;
    this.operation = operation;
    this.bs = bs;
  }

  @Override
  public OpenBitSetDISI call() throws Exception {
    OpenBitSetDISI accumulator = new OpenBitSetDISI(finalBitsetSize);
    int i = 0;
    for (DocIdSet bitset : bs) {
      operation.compute(accumulator, bitset);
      i++;
    }
    return accumulator;
  }

}