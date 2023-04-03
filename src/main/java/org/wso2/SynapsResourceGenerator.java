package org.wso2;

import org.wso2.exception.ExecutorException;
import org.wso2.executor.Executor;
import org.wso2.executor.ExecutorFactory;

public class SynapsResourceGenerator {

    public static void main(String[] args) {

        try {
            Executor executor = ExecutorFactory.getExecutor(args);
            executor.execute();
        } catch (ExecutorException e) {
            System.out.println(e.getMessage());
        }
    }
}