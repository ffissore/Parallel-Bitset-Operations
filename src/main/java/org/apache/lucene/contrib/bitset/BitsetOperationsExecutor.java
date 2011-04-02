package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class BitsetOperationsExecutor {

  private static final int MIN_ARRAY_SIZE = 20000;

  private final ExecutorService threadPool;

  public BitsetOperationsExecutor(ExecutorService threadPool) {
    this.threadPool = threadPool;
  }

  public OpenBitSetDISI bitsetOperations(DocIdSet[] bs, int finalBitsetSize, BitSetOperation operation) throws Exception {
    if (bs.length <= MIN_ARRAY_SIZE) {
      return new CallableOperation(bs, 0, bs.length, finalBitsetSize, operation).call();
    }

    int sliceSize = bs.length / Runtime.getRuntime().availableProcessors();

    Collection<CallableOperation> ops = sliceBitsets(bs, finalBitsetSize, operation, sliceSize);

    List<Future<OpenBitSetDISI>> futureOps = threadPool.invokeAll(ops);

    OpenBitSetDISI[] accumulated = accumulate(futureOps);

    return new CallableOperation(accumulated, 0, accumulated.length, finalBitsetSize, operation).call();
  }

  private OpenBitSetDISI[] accumulate(List<Future<OpenBitSetDISI>> futureOps) throws ExecutionException, InterruptedException {
    OpenBitSetDISI[] accumulated = new OpenBitSetDISI[futureOps.size()];
    int i = 0;
    for (Future<OpenBitSetDISI> op : futureOps) {
      accumulated[i] = op.get();
      i++;
    }
    return accumulated;
  }

  protected Collection<CallableOperation> sliceBitsets(DocIdSet[] bs, int finalBitSetSize, BitSetOperation operation, int sliceSize) {
    int numOfOps = bs.length / sliceSize;
    if (bs.length % sliceSize != 0) {
      numOfOps++;
    }

    Collection<CallableOperation> ops = new LinkedList<CallableOperation>();
    for (int i = 0; i < numOfOps; i++) {
      int startIndex = i * sliceSize;
      ops.add(new CallableOperation(bs, startIndex, lastIndex(bs.length, startIndex, sliceSize), finalBitSetSize, operation));
    }

    return ops;
  }

  private int lastIndex(int numberOfBitsets, int startIndex, int sliceSize) {
    int remaining = numberOfBitsets - startIndex;
    return remaining > sliceSize ? startIndex + sliceSize : startIndex + remaining;
  }
}
