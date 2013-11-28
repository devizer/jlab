package org.universe.jcl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AggregateException extends RuntimeException {

    static final String message = "Parallel operation failed";
    List<Exception> innerExceptions;

    AggregateException(List<Exception> exceptions) {
        super(message, exceptions.get(0));
        this.innerExceptions = Collections.unmodifiableList(new ArrayList<Exception>(exceptions));
    }

    List<Exception> getInnerExceptions() {
        return innerExceptions;
    }

    AggregateException() {
        super();
    }

    AggregateException(String message) {
        super(message);
    }

    AggregateException(String message, Throwable cause) {
        super(message, cause);
    }

    AggregateException(Throwable cause) {
        super(cause);
    }

}
