package com.example.docs.ot.utils;

import com.example.docs.ot.model.Operation;

public class OperationTransformer {
    public static String apply(String doc, Operation op){
        switch (op.getType()) {
            case INSERT -> {
                return doc.substring(0, op.getPosition()) + op.getCharacter() + doc.substring(op.getPosition());
            }
            case DELETE -> {
                int pos = op.getPosition();
                if (pos < 0 || pos >= doc.length()) {
                    // Invalid delete → ignore safely
                    return doc;
                }
                return doc.substring(0, pos) + doc.substring(pos + 1);
            }
            default -> throw new IllegalArgumentException("Unknown operation type!");
        }
    }

    public static Operation transform(Operation op1, Operation op2){
        if(op1.getType() == Operation.Type.INSERT && op2.getType() == Operation.Type.INSERT){
            if(op1.getPosition() <= op2.getPosition()){
                op2.setPosition(op2.getPosition() + 1);
            }
        } else if (op1.getType() == Operation.Type.DELETE && op2.getType() == Operation.Type.INSERT){
            if(op1.getPosition() < op2.getPosition()){
                op2.setPosition(op2.getPosition() - 1);
            }
        } else if (op1.getType() == Operation.Type.INSERT && op2.getType() == Operation.Type.DELETE){
            if(op1.getPosition() <= op2.getPosition()){
                op2.setPosition(op2.getPosition() + 1);
            }
        } else if (op1.getType() == Operation.Type.DELETE && op2.getType() == Operation.Type.DELETE){
            if(op1.getPosition() < op2.getPosition()){
                op2.setPosition(op2.getPosition() - 1);
            }
        }

        return op2;
    }
}
