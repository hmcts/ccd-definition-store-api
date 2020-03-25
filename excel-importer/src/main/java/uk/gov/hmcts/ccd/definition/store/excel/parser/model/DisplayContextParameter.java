package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

public class DisplayContextParameter {
    public enum DisplayContextParameterValues {
        TABLE("#TABLE("),
        LIST("#LIST("),
        DATETIMEDISPLAY("#DATETIMEDISPLAY("),
        DATETIMEENTRY("#DATETIMEENTRY(");

        private String value;

        DisplayContextParameterValues(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }
}
