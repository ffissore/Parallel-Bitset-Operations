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

  private int lastIndex(int numberOfBitsets, int startIndex, int sliceSize) {
    int remaining = numberOfBitsets - startIndex;
    return remaining > sliceSize ? startIndex + sliceSize : startIndex + remaining;
  }

  public <T> T[] bitsetOperations(DocIdSet[] bs, DocIdSet toCompare, int finalBitsetSize, BitSetComparisonOperation<T> operation) throws Exception {
    OpenBitSetDISI toCompareDisi = new OpenBitSetDISI(finalBitsetSize);
    toCompareDisi.inPlaceOr(toCompare.iterator());

    if (bs.length <= minArraySize) {
      return new CallableComparison<T>(bs, 0, bs.length, finalBitsetSize, toCompareDisi, operation).call();
    }

    int sliceSize = bs.length / Runtime.getRuntime().availableProcessors();

    Collection<CallableComparison<T>> ops = sliceBitsets(bs, finalBitsetSize, toCompareDisi, operation, sliceSize);

    List<Future<T[]>> futureOps = threadPool.invokeAll(ops);

    return accumulate(futureOps);
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

  protected <T> Collection<CallableComparison<T>> sliceBitsets(DocIdSet[] bs, int finalBitSetSize, OpenBitSet toCompare, BitSetComparisonOperation<T> operation, int sliceSize) {
    int numOfOps = bs.length / sliceSize;
    if (bs.length % sliceSize != 0) {
      numOfOps++;
    }

    Collection<CallableComparison<T>> ops = new LinkedList<CallableComparison<T>>();
    for (int i = 0; i < numOfOps; i++) {
      int startIndex = i * sliceSize;
      ops.add(new CallableComparison<T>(bs, startIndex, lastIndex(bs.length, startIndex, sliceSize), finalBitSetSize, toCompare, operation));
    }

    return ops;
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

  @SuppressWarnings({"unchecked"})
  private <T> T[] accumulate(List<Future<T[]>> futureOps) throws ExecutionException, InterruptedException {
    Object[][] partitionResults = new Object[futureOps.size()][];
    int i = 0;
    int sum = 0;
    for (Future<T[]> op : futureOps) {
      partitionResults[i] = op.get();
      sum += partitionResults[i].length;
      i++;
    }

    Object[] result = new Object[sum];
    int lastIndex = 0;
    for (Object[] partial : partitionResults) {
      System.arraycopy(partial, 0, result, lastIndex, partial.length);
      lastIndex += partial.length;
    }

    return ArrayUtils.typedArray(result, (Class<T>) result[0].getClass());
  }
}
