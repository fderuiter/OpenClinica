package org.akaza.openclinica.service.clinical;

public interface WorkflowTransactionCallback<T> {
    T doInTransaction();
}
