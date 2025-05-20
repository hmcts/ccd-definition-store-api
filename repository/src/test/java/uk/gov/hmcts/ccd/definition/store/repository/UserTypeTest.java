package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Boost test coverage for GeneralImmutableJsonType and ClassUserType
public class UserTypeTest {

    @Test
    public void testClassUserType() throws SQLException {
        
        ClassUserType classUserType = new ClassUserType();
        assertEquals(classUserType.getSqlType(), Types.JAVA_OBJECT);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("java.lang.String");
        Object result = classUserType.nullSafeGet(resultSet, 1, null, null);
        assertEquals(String.class, result);
    }

    @Test
    public void testGeneralImmutableJsonType() throws HibernateException, SQLException {
        GIJT<Banner> gijt = new GIJT<Banner>(Banner.class);
        assertEquals(gijt.getSqlType(), Types.JAVA_OBJECT);
        assertEquals(gijt.returnedClass(), Banner.class);

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("{\"id\":\"value\"}");
        Banner result = gijt.nullSafeGet(resultSet, 1, null, null);
        assertEquals("value", result.getId());
    }

    private class GIJT<T> extends GeneralImmutableJsonType<T> {
        public GIJT(Class<T> type) {
            super(type);
        }
    }
}
