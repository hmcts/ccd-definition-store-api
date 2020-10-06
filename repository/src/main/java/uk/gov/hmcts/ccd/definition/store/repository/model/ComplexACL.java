package uk.gov.hmcts.ccd.definition.store.repository.model;

public class ComplexACL extends AccessControlList {
    private String listElementCode;

    public ComplexACL() {

    }

    public ComplexACL(String role,
                      Boolean create,
                      Boolean read,
                      Boolean update,
                      Boolean delete, String listElementCode) {
        super(role, create, read, update, delete);
        this.listElementCode = listElementCode;
    }

    public String getListElementCode() {
        return listElementCode;
    }

    public void setListElementCode(String listElementCode) {
        this.listElementCode = listElementCode;
    }
}
