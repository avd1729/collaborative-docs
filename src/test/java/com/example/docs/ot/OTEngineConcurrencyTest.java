package com.example.docs.ot;

import com.example.docs.ot.engine.OTEngine;
import com.example.docs.ot.model.Operation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OTEngineConcurrencyTest {

    @Test
    void testConcurrentInserts() throws InterruptedException {
        OTEngine engine = new OTEngine();
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);

        List<Operation> ops = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Operation insert = new Operation();
            insert.setType(Operation.Type.INSERT);
            insert.setPosition(0);  // everyone inserts at front
            insert.setCharacter(String.valueOf((char) ('A' + i)));
            insert.setVersion(0);
            ops.add(insert);
        }

        for (Operation op : ops) {
            executor.submit(() -> {
                try {
                    latch.await(); // wait until all threads ready
                    engine.applyOperation(op);
                } catch (InterruptedException ignored) {}
            });
        }

        latch.countDown(); // start all threads
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        String doc = engine.getDocument();
        System.out.println("Final doc = " + doc);

        // Document must contain all inserted chars, order may vary
        for (Operation op : ops) {
            assertTrue(doc.contains(op.getCharacter()));
        }
        assertTrue(doc.length() == threads);
    }

    @Test
    void testConcurrentInsertAndDelete() throws InterruptedException {
        OTEngine engine = new OTEngine();

        // Insert initial char
        Operation insert = new Operation();
        insert.setType(Operation.Type.INSERT);
        insert.setPosition(0);
        insert.setCharacter("X");
        insert.setVersion(0);
        engine.applyOperation(insert);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        Operation op1 = new Operation();
        op1.setType(Operation.Type.INSERT);
        op1.setPosition(1);
        op1.setCharacter("Y");
        op1.setVersion(engine.getVersion());

        Operation op2 = new Operation();
        op2.setType(Operation.Type.DELETE);
        op2.setPosition(0);
        op2.setVersion(engine.getVersion());

        executor.submit(() -> {
            try { latch.await(); engine.applyOperation(op1); } catch (InterruptedException ignored) {}
        });
        executor.submit(() -> {
            try { latch.await(); engine.applyOperation(op2); } catch (InterruptedException ignored) {}
        });

        latch.countDown();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        String doc = engine.getDocument();
        System.out.println("Final doc = " + doc);

        // Final doc should be either "Y" (delete then insert) or "" (insert invalidated)
        assertTrue(doc.equals("Y") || doc.equals(""));
    }
}
