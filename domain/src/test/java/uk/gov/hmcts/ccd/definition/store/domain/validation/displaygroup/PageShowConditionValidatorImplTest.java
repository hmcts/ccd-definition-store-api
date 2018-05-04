package uk.gov.hmcts.ccd.definition.store.domain.validation.displaygroup;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.InvalidShowConditionException;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowCondition;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;

import java.util.List;

public class PageShowConditionValidatorImplTest {
    private static final List<DisplayGroupEntity> UNUSED_DISPLAY_GROUPS = Lists.newArrayList();

    @Mock
    private ShowConditionParser mockShowConditionParser;
    PageShowConditionValidatorImpl testObj;
    DisplayGroupEntity displayGroup;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new PageShowConditionValidatorImpl(mockShowConditionParser);
        displayGroup = new DisplayGroupEntity();
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsEmpty() throws InvalidShowConditionException {

        displayGroup.setShowCondition(null);
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    public void shouldNotExecuteWhenShowConditionIsBlank() throws InvalidShowConditionException {

        displayGroup.setShowCondition("");
        displayGroup.setType(DisplayGroupType.PAGE);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    public void shouldNotExecuteWhenShowTypeIsNotPage() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.TAB);

        testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        verify(mockShowConditionParser, never()).parseShowCondition(anyString());
    }

    @Test
    public void returnsNoValidationErrorsOnSuccess() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField("field")).thenReturn(true);
        displayGroup.setEvent(event);
        ShowCondition validParsedShowCondition = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenReturn(validParsedShowCondition);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(true));
    }

    @Test
    public void returnsDisplayGroupInvalidShowConditionErrorWhenUnableToParseShowCondition() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenThrow(new InvalidShowConditionException("someShowCondition"));

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidShowConditionError.class));
    }

    @Test
    public void returnsDisplayGroupInvalidShowConditionFieldWhenShowConditionReferencesInvalidField() throws InvalidShowConditionException {

        displayGroup.setShowCondition("someShowCondition");
        displayGroup.setType(DisplayGroupType.PAGE);
        EventEntity event = mock(EventEntity.class);
        when(event.hasField("field")).thenReturn(false);
        displayGroup.setEvent(event);
        ShowCondition sc = new ShowCondition.Builder().showConditionExpression("parsedSC").field("field").build();
        when(mockShowConditionParser.parseShowCondition("someShowCondition"))
                .thenReturn(sc);

        ValidationResult result = testObj.validate(displayGroup, UNUSED_DISPLAY_GROUPS);

        assertThat(result.isValid(), is(false));
        assertThat(result.getValidationErrors(), hasSize(1));
        assertThat(result.getValidationErrors().get(0), instanceOf(DisplayGroupInvalidEventFieldShowCondition.class));
    }


}
