package uk.gov.hmcts.ccd.definition.store.domain.service.response;

public class ServiceResponse<T> {

    private final T responseBody;
    private final IOperationEnum operation;

    public ServiceResponse(T responseBody, IOperationEnum operation) {
        this.responseBody = responseBody;
        this.operation = operation;
    }

    public T getResponseBody() {
        return responseBody;
    }

    public IOperationEnum getOperation() {
        return operation;
    }
}
