package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
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
  private final int minArraySize;

  public BitsetOperationsExecutor(ExecutorService threadPool) {
    this(threadPool, MIN_ARRAY_SIZE);
  }

  public BitsetOperationsExecutor(ExecutorService threadPool, int minArraySize) {
    this.threadPool = threadPool;
    this.minArraySize = minArraySize;
  }

  public OpenBitSetDISI bitsetOperations(DocIdSet[] bs, int finalBitsetSize, BitSetOperation operation) throws Exception {
    if (bs.length <= minArraySize) {
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

  public long[] bitsetOperations(DocIdSet[] bs, DocIdSet toCompare, int finalBitsetSize, BitSetComparisonOperation operation) throws Exception {
    OpenBitSetDISI toCompareDisi = new OpenBitSetDISI(finalBitsetSize);
    toCompareDisi.inPlaceOr(toCompare.iterator());

    if (bs.length <= minArraySize) {
      return new CallableComparison(bs, 0, bs.length, finalBitsetSize, toCompareDisi, operation).call();
    }

    int sliceSize = bs.length / Runtime.getRuntime().availableProcessors();

    Collection<CallableComparison> ops = sliceBitsets(bs, finalBitsetSize, toCompareDisi, operation, sliceSize);

    List<Future<long[]>> futureOps = threadPool.invokeAll(ops);

    return accumulate(futureOps);
  }

  protected Collection<CallableComparison> sliceBitsets(DocIdSet[] bs, int finalBitSetSize, OpenBitSet toCompare, BitSetComparisonOperation operation, int sliceSize) {
    int numOfOps = bs.length / sliceSize;
    if (bs.length % sliceSize != 0) {
      numOfOps++;
    }

    Collection<CallableComparison> ops = new LinkedList<CallableComparison>();
    for (int i = 0; i < numOfOps; i++) {
      int startIndex = i * sliceSize;
      ops.add(new CallableComparison(bs, startIndex, lastIndex(bs.length, startIndex, sliceSize), finalBitSetSize, toCompare, operation));
    }

    return ops;
  }

  private long[] accumulate(List<Future<long[]>> futureOps) throws ExecutionException, InterruptedException {
    long[][] partialResults = new long[futureOps.size()][];
    int i = 0;
    int sum = 0;
    for (Future<long[]> op : futureOps) {
      partialResults[i] = op.get();
      sum += partialResults[i].length;
      i++;
    }
    long[] result = new long[sum];
    int lastIndex = 0;
    for (long[] partial : partialResults) {
      System.arraycopy(partial, 0, result, lastIndex, partial.length);
      lastIndex += partial.length;
    }
    return result;
  }
}
