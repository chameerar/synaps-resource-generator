package org.wso2;

import org.wso2.executor.Executor;
import org.wso2.executor.ExecutorFactory;

import static org.wso2.executor.Constants.OAS_GENERATION;

public class SynapsResourceGenerator {

    public static void main(String[] args) {
        Executor oasGeneration = ExecutorFactory.getExecutor(OAS_GENERATION);
        oasGeneration.execute();
    }
}