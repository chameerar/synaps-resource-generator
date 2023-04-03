package org.wso2.executor;

import org.wso2.exception.ExecutorException;
import org.wso2.executor.impl.OasGenerationExecutor;

import static org.wso2.executor.Constants.OAS_GENERATION;

public class ExecutorFactory {

    private ExecutorFactory() {
        //To hide the implicit public constructor
    }

    public static Executor getExecutor(String[] args) throws ExecutorException {

        if (args == null || args.length == 0) {
            throw new ExecutorException("No executor type specified");
        }
        String executorType = args[0];
        if (executorType.equalsIgnoreCase(OAS_GENERATION)) {
            return new OasGenerationExecutor(args);
        }
        throw new ExecutorException("No executor found for type: " + executorType );
    }
}
