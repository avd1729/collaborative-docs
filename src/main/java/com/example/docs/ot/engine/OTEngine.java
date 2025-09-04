package com.example.docs.ot.engine;

import com.example.docs.ot.model.Operation;
import com.example.docs.ot.utils.OperationTransformer;

import java.util.ArrayList;
import java.util.List;

public class OTEngine {
    private StringBuilder document = new StringBuilder();
    private int version = 0;
    private final List<Operation> history = new ArrayList<>();

    public synchronized void applyOperation(Operation op){
        for (int i = op.getVersion(); i < history.size(); i++) {
            op = OperationTransformer.transform(history.get(i), op);
        }

        document = new StringBuilder(OperationTransformer.apply(document.toString(), op));
        history.add(op);
        version++;
    }

    public String getDocument() {
        return document.toString();
    }

    public int getVersion() {
        return version;
    }
}
