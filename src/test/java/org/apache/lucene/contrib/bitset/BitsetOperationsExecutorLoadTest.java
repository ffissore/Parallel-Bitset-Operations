package org.apache.lucene.contrib.bitset;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.lucene.util.SortedVIntList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

public class BitsetOperationsExecutorLoadTest {

  private static final int BS_COUNT = 200000;
  private static final int BS_SIZE = 10000;

  private Random random;
  private BitsetOperationsExecutor bitsetOperationsExecutor;
  private long maxSingleBitsetCardinality;
  private DocIdSet[] docIdSets;
  private ExecutorService threadPool;
  private BitSetOperation operation;

  @Before
  public void setup() {
    random = new Random();

    // OpenBitSet[] bs = new OpenBitSet[BS_COUNT];
    // int howManyBits = random.nextInt(BS_SIZE / 2);
    // for (int i = 0; i < bs.length; i++) {
    // bs[i] = new OpenBitSet(BS_SIZE);
    // for (int b = 0; b < howManyBits; b++) {
    // bs[i].fastSet(random.nextInt(BS_SIZE));
    // }
    // maxSingleBitsetCardinality = Math.max(maxSingleBitsetCardinality,
    // bs[i].cardinality());
    // }

    SortedVIntList[] svil = new SortedVIntList[BS_COUNT];
    int howManyBits = random.nextInt(BS_SIZE / 10);
    for (int i = 0; i < svil.length; i++) {
      int[] ints = new int[howManyBits];
      for (int b = 0; b < howManyBits; b++) {
        ints[b] = random.nextInt(BS_SIZE);
      }
      Arrays.sort(ints);
      svil[i] = new SortedVIntList(ints);
    }

    //List<DocIdSet> l = new LinkedList<DocIdSet>();
    //for (int i = 0; i < 100; i++) {
    // l.addAll(Arrays.asList(bs));
    //l.addAll(Arrays.asList(svil));
    //}
    //Collections.shuffle(l);

    // docIdSets = l.toArray(new DocIdSet[l.size()]);
    // System.arraycopy(bs, 0, docIdSets, 0, bs.length);
    // System.arraycopy(svil, 0, docIdSets, bs.length, svil.length);
    docIdSets = svil;
    //System.arraycopy(svil, 0, docIdSets, 0, svil.length);

    threadPool = Executors.newCachedThreadPool();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool);

    operation = new OR();
  }

  @After
  public void teardown() {
    threadPool.shutdownNow();
  }

  @Test
  public void shouldBeSlow() throws IOException {
    System.out.println("========= SLOW: START");
    long startAt = System.nanoTime();
    OpenBitSetDISI finalBs = new OpenBitSetDISI(BS_SIZE);
    for (int i = 0; i < docIdSets.length; i++) {
      operation.compute(finalBs, docIdSets[i]);
    }
    long stopAt = System.nanoTime();
    System.out.println("========= SLOW: end");
    System.out.println("shouldBeSlow: " + (stopAt - startAt) / 1000000);
    assertTrue(finalBs.cardinality() > maxSingleBitsetCardinality);
    System.out.println(finalBs.get(random.nextInt((int) finalBs.cardinality())));
  }

  @Test
  public void shouldBeFast() throws Exception {
    System.out.println("========= FAST: START");
    long startAt = System.nanoTime();
    OpenBitSetDISI finalBs = bitsetOperationsExecutor.bitsetOperations(docIdSets, BS_SIZE, operation);
    long stopAt = System.nanoTime();
    System.out.println("========= FAST: end");
    System.out.println("shouldBeFast: " + (stopAt - startAt) / 1000000);
    assertTrue(finalBs.cardinality() > maxSingleBitsetCardinality);
    System.out.println(finalBs.get(random.nextInt((int) finalBs.cardinality())));
  }
}
