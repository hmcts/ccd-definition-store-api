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
class UserTypeTest {

    @Test
    void testClassUserType() throws SQLException {
        
        ClassUserType classUserType = new ClassUserType();
        assertEquals(Types.JAVA_OBJECT, classUserType.getSqlType());

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString(1)).thenReturn("java.lang.String");
        Object result = classUserType.nullSafeGet(resultSet, 1, null, null);
        assertEquals(String.class, result);
    }

    @Test
    void testGeneralImmutableJsonType() throws HibernateException, SQLException {
        GIJT<Banner> gijt = new GIJT<Banner>(Banner.class);
        assertEquals(Types.JAVA_OBJECT, gijt.getSqlType());
        assertEquals(Banner.class, gijt.returnedClass());

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
