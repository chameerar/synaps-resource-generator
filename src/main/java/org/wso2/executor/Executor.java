package org.wso2.executor;

import org.wso2.exception.ExecutorException;

public interface Executor {

    void init(String [] args);
    void execute() throws ExecutorException;

}
