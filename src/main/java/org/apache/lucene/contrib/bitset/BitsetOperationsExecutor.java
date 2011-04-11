/*
 * Parallel Bitset Operations
 * Copyright (C) 2011 Federico Fissore
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */

package org.apache.lucene.contrib.bitset;

import org.apache.lucene.contrib.bitset.ops.CommutativeOp;
import org.apache.lucene.contrib.bitset.ops.ComparisonOp;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * BitsetOperationsExecutor is the entry point for performing bitset operations.<br/><br/>
 * You need to create an array of the {@link DocIdSet} you want to operate on, choose the operation to perform (one implementation of {@link CommutativeOp} or {@link ComparisonOp}) and call the appropriate perform method.<br/><br/>
 * The input array will be split in as many parts as available cores (as by {@link Runtime#availableProcessors()}), and the given operation will be performed
 */
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

  /**
   * Performs a commutative operation on the given array of bitsets
   *
   * @param bs              the bitsets on to compute the operation
   * @param finalBitsetSize the final bitset size (tipically IndexReader.numDocs())
   * @param operation       the operation to perform
   * @return an OpenBitSetDISI, result of the operation
   * @throws Exception
   */
  public OpenBitSetDISI perform(DocIdSet[] bs, final int finalBitsetSize, final CommutativeOp operation) throws Exception {
    if (bs.length <= minArraySize) {
      return new CommutativeOpCallable(bs, 0, bs.length, finalBitsetSize, operation).call();
    }

    Collection<Callable<OpenBitSetDISI>> ops = new BitSetSlicer<OpenBitSetDISI>() {

      @Override
      protected Callable<OpenBitSetDISI> newOpCallable(DocIdSet[] bs, int fromIndex, int toIndex) {
        return new CommutativeOpCallable(bs, fromIndex, toIndex, finalBitsetSize, operation);
      }

    }.sliceBitsets(bs);

    List<Future<OpenBitSetDISI>> futures = threadPool.invokeAll(ops);

    OpenBitSetDISI[] accumulated = ArrayUtils.toArray(futures);

    return new CommutativeOpCallable(accumulated, 0, accumulated.length, finalBitsetSize, operation).call();
  }

  /**
   * Performs a comparative operation on the given array of bitsets
   *
   * @param bs              the bitsets on to compute the operation
   * @param toCompare       the bitset to compare to the array of bitsets
   * @param finalBitsetSize the final bitset size (tipically IndexReader.numDocs())
   * @param operation       the operation to compute
   * @param <T>             the return type
   * @return an array of objects (whose type is defined by the operation). The array has the same size of the input array of bitsets and order is preserved so the result of the operation performed at bs[N] is at position N in the returned array
   * @throws Exception
   */
  public <T> T[] perform(DocIdSet[] bs, DocIdSet toCompare, final int finalBitsetSize, final ComparisonOp<T> operation) throws Exception {
    final OpenBitSetDISI toCompareDisi = new OpenBitSetDISI(finalBitsetSize);
    toCompareDisi.inPlaceOr(toCompare.iterator());

    if (bs.length <= minArraySize) {
      return new ComparisonOpCallable<T>(bs, 0, bs.length, finalBitsetSize, toCompareDisi, operation).call();
    }

    Collection<Callable<T[]>> ops = new BitSetSlicer<T[]>() {

      @Override
      protected Callable<T[]> newOpCallable(DocIdSet[] bs, int fromIndex, int toIndex) {
        return new ComparisonOpCallable<T>(bs, fromIndex, toIndex, finalBitsetSize, toCompareDisi, operation);
      }

    }.sliceBitsets(bs);

    List<Future<T[]>> futures = threadPool.invokeAll(ops);

    T[][] partitionResults = ArrayUtils.toArray(futures);

    return ArrayUtils.flatten(partitionResults);
  }

}
