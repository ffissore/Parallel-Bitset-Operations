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

package org.apache.lucene.contrib.bitset.test;

import org.apache.lucene.contrib.bitset.BitsetOperationsExecutor;
import org.apache.lucene.contrib.bitset.ops.IntersectionCount;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.SortedVIntList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public abstract class AbstractComparisonOperationsTest {

  private DocIdSet[] bs;
  protected ExecutorService threadPool;
  protected BitsetOperationsExecutor bitsetOperationsExecutor;

  @Before
  public void setup() {
    bs = new DocIdSet[4];
    bs[0] = new SortedVIntList(1, 2, 3);
    bs[1] = new SortedVIntList(2, 3, 4);
    bs[2] = new SortedVIntList(4, 5, 6);
    bs[3] = new SortedVIntList(7, 8, 9);

    threadPool = Executors.newCachedThreadPool();
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }

  @Test
  public void shouldIntersect() throws Exception {
    Long[] result = bitsetOperationsExecutor.perform(bs, new SortedVIntList(2, 3), 10, new IntersectionCount());
    assertEquals(bs.length, result.length);
    assertEquals(2, result[0].longValue());
    assertEquals(2, result[1].longValue());
    assertEquals(0, result[2].longValue());
    assertEquals(0, result[3].longValue());
  }

}
