package uk.gov.hmcts.ccd.definitionstore.befta;

public final class DefinitionStoreTestDataLoader {
    private DefinitionStoreTestDataLoader(){ }

    public static void main(String[] args) {
        new DefinitionStoreTestAutomationAdapter().loadTestDataIfNecessary();
    }
}
