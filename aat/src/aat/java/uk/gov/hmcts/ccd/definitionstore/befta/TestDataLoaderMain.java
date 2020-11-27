package uk.gov.hmcts.ccd.definitionstore.befta;

public final class TestDataLoaderMain {
    private TestDataLoaderMain() {
    }

    public static void main(String[] args) {
        new DefinitionStoreTestAutomationAdapter().getDataLoader().loadTestDataIfNecessary();
    }
}
