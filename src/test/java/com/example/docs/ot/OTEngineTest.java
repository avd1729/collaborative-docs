package com.example.docs.ot;

import com.example.docs.ot.engine.OTEngine;
import com.example.docs.ot.model.Operation;
import com.example.docs.ot.utils.OperationTransformer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OTEngineTest {

    @Test
    void testApplyInsert() {
        OTEngine engine = new OTEngine();

        Operation insert = new Operation();
        insert.setType(Operation.Type.INSERT);
        insert.setPosition(0);
        insert.setCharacter("H");
        insert.setVersion(0);

        engine.applyOperation(insert);

        assertEquals("H", engine.getDocument());
        assertEquals(1, engine.getVersion());
    }

    @Test
    void testApplyMultipleInserts() {
        OTEngine engine = new OTEngine();

        Operation op1 = new Operation();
        op1.setType(Operation.Type.INSERT);
        op1.setPosition(0);
        op1.setCharacter("H");
        op1.setVersion(0);

        Operation op2 = new Operation();
        op2.setType(Operation.Type.INSERT);
        op2.setPosition(1);
        op2.setCharacter("i");
        op2.setVersion(1);

        engine.applyOperation(op1);
        engine.applyOperation(op2);

        assertEquals("Hi", engine.getDocument());
        assertEquals(2, engine.getVersion());
    }

    @Test
    void testApplyDelete() {
        OTEngine engine = new OTEngine();

        Operation insert = new Operation();
        insert.setType(Operation.Type.INSERT);
        insert.setPosition(0);
        insert.setCharacter("H");
        insert.setVersion(0);

        engine.applyOperation(insert);

        Operation delete = new Operation();
        delete.setType(Operation.Type.DELETE);
        delete.setPosition(0);
        delete.setVersion(1);

        engine.applyOperation(delete);

        assertEquals("", engine.getDocument());
        assertEquals(2, engine.getVersion());
    }

    @Test
    void testTransformInsertVsInsert() {
        Operation op1 = new Operation();
        op1.setType(Operation.Type.INSERT);
        op1.setPosition(0);
        op1.setCharacter("A");

        Operation op2 = new Operation();
        op2.setType(Operation.Type.INSERT);
        op2.setPosition(0);
        op2.setCharacter("B");

        Operation transformed = OperationTransformer.transform(op1, op2);

        assertEquals(1, transformed.getPosition());
    }

    @Test
    void testTransformDeleteVsInsert() {
        Operation op1 = new Operation();
        op1.setType(Operation.Type.DELETE);
        op1.setPosition(0);

        Operation op2 = new Operation();
        op2.setType(Operation.Type.INSERT);
        op2.setPosition(1);
        op2.setCharacter("X");

        Operation transformed = OperationTransformer.transform(op1, op2);

        // delete at 0 shifts insert left
        assertEquals(0, transformed.getPosition());
    }
}
