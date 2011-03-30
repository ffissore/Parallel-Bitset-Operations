package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class BitsetOperationsExecutor {

  private static final int DEFAULT_SLICE_SIZE = 10000;

  private final ExecutorService threadPool;
  private final int sliceSize;

  public BitsetOperationsExecutor(ExecutorService threadPool) {
    this(threadPool, DEFAULT_SLICE_SIZE);
  }

  public BitsetOperationsExecutor(ExecutorService threadPool, int sliceSize) {
    this.threadPool = threadPool;
    this.sliceSize = sliceSize;
  }

  public OpenBitSetDISI bitsetOperations(DocIdSet[] bs, int finalBitsetSize, BitSetOperation operation) throws Exception {
    if (bs.length <= sliceSize) {
      new CallableOperation(bs, finalBitsetSize, operation).call();
    }

    Collection<CallableOperation> ops = sliceBitsets(bs, finalBitsetSize, operation);

    List<Future<OpenBitSetDISI>> futureOps = threadPool.invokeAll(ops);

    OpenBitSetDISI[] accumulated = new OpenBitSetDISI[futureOps.size()];
    int i = 0;
    for (Future<OpenBitSetDISI> op : futureOps) {
      accumulated[i] = op.get();
      i++;
    }

    return new CallableOperation(accumulated, finalBitsetSize, operation).call();
  }

  protected Collection<CallableOperation> sliceBitsets(DocIdSet[] bs, int finalBitSetSize, BitSetOperation operation) {
    int numOfOps = bs.length / sliceSize;
    if (bs.length % sliceSize != 0) {
      numOfOps++;
    }

    Collection<CallableOperation> ops = new LinkedList<CallableOperation>();
    for (int i = 0; i < numOfOps; i++) {
      int startIndex = i * sliceSize;
      ops.add(new CallableOperation(Arrays.copyOfRange(bs, startIndex, lastIndex(bs.length, startIndex)), finalBitSetSize, operation));
    }

    return ops;
  }

  private int lastIndex(int numberOfBitsets, int startIndex) {
    int remaining = numberOfBitsets - startIndex;
    return remaining > sliceSize ? startIndex + sliceSize : startIndex + remaining;
  }
}
