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
import org.apache.lucene.contrib.bitset.ops.AND;
import org.apache.lucene.contrib.bitset.ops.NOT;
import org.apache.lucene.contrib.bitset.ops.OR;
import org.apache.lucene.contrib.bitset.ops.XOR;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class AbstractBitsetBasicOperationsTest {

  private DocIdSet[] dis;
  protected ExecutorService threadPool;
  protected BitsetOperationsExecutor bitsetOperationsExecutor;

  @Before
  public void setup() {
    dis = new DocIdSet[4];

    OpenBitSet bs = new OpenBitSet(10);
    bs.fastSet(1);
    bs.fastSet(5);
    dis[0] = bs;
    bs = new OpenBitSet(10);
    bs.fastSet(0);
    bs.fastSet(1);
    bs.fastSet(2);
    dis[1] = bs;

    dis[2] = new SortedVIntList(1, 4);
    dis[3] = new SortedVIntList(1, 2, 6);

    threadPool = Executors.newCachedThreadPool();
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }

  @Test
  public void shouldOrTheDocIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.perform(dis, 10, new OR());
    assertTrue(bs.get(0));
    assertTrue(bs.get(1));
    assertTrue(bs.get(2));
    assertFalse(bs.get(3));
    assertTrue(bs.get(4));
    assertTrue(bs.get(5));
    assertTrue(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }

  @Test
  public void shouldAndTheDocIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.perform(dis, 10, new AND());
    assertFalse(bs.get(0));
    assertTrue(bs.get(1));
    assertFalse(bs.get(2));
    assertFalse(bs.get(3));
    assertFalse(bs.get(4));
    assertFalse(bs.get(5));
    assertFalse(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }

  @Test
  public void shouldNotTheDocIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.perform(dis, 10, new NOT());
    assertFalse(bs.get(0));
    assertFalse(bs.get(1));
    assertFalse(bs.get(2));
    assertFalse(bs.get(3));
    assertFalse(bs.get(4));
    assertTrue(bs.get(5));
    assertFalse(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }

  @Test
  public void shouldXORTheDodIdSets() throws Exception {
    OpenBitSetDISI bs = bitsetOperationsExecutor.perform(dis, 10, new XOR());
    assertTrue(bs.get(0));
    assertFalse(bs.get(1));
    assertFalse(bs.get(2));
    assertFalse(bs.get(3));
    assertTrue(bs.get(4));
    assertTrue(bs.get(5));
    assertTrue(bs.get(6));
    assertFalse(bs.get(7));
    assertFalse(bs.get(8));
    assertFalse(bs.get(9));
  }


}
