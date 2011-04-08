package org.apache.lucene.contrib.bitset;

import org.junit.Before;

public class SlicingBitsetBasicOperationTest extends AbstractBitsetBasicOperationsTest {

  @Before
  public void setup() {
    super.setup();
    bitsetOperationsExecutor = new BitsetOperationsExecutor(threadPool, 1);
  }

}
