package org.wso2.executor;

import org.wso2.executor.impl.OasGenerationExecutor;

import static org.wso2.executor.Constants.OAS_GENERATION;

public class ExecutorFactory {

    private ExecutorFactory() {
        //To hide the implicit public constructor
    }

    public static Executor getExecutor(String executorType) {
        if (executorType == null) {
            return null;
        }
        if (executorType.equalsIgnoreCase(OAS_GENERATION)) {
            return new OasGenerationExecutor();
        }
        return null;
    }
}
