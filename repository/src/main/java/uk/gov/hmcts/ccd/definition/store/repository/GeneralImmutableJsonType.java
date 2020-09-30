package uk.gov.hmcts.ccd.definition.store.repository;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public abstract class GeneralImmutableJsonType<T> implements UserType {

    // The Type name to be placed on @UserType annotation
    public static final String TYPE = "uk.gov.hmcts.ccd.definition.data.GeneralImmutableJsonType";

    private final Class<T> dataType;

    public GeneralImmutableJsonType(Class<T> type) {
        this.dataType = type;
    }

    @Override
    public int[] sqlTypes() {
        return new int[] {Types.JAVA_OBJECT};
    }

    @Override
    public Class<T> returnedClass() {
        return dataType;
    }

    @Override
    public boolean equals(Object alpha, Object beta) {
        return ObjectUtils.nullSafeEquals(alpha, beta);
    }

    @Override
    public int hashCode(Object object) {
        return object.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet,
                              String[] names,
                              SharedSessionContractImplementor sharedSessionContractImplementor,
                              Object o) throws HibernateException, SQLException {
        final String cellContent = resultSet.getString(names[0]);
        return JsonUtils.fromString(cellContent, dataType);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement,
                            Object value,
                            int index,
                            SharedSessionContractImplementor sharedSessionContractImplementor)
        throws HibernateException, SQLException {
        if (value == null) {
            preparedStatement.setNull(index, Types.OTHER);
            return;
        }
        preparedStatement.setObject(index, JsonUtils.toString(value), Types.OTHER);
    }

    @Override
    public Object deepCopy(final Object value) {
        return JsonUtils.clone(value);
    }

    @Override
    public boolean isMutable() {
        // Pay Attention to this if me merge is to added this needs to be
        // changed
        return false;
    }

    /**
     * For Immutable Object this is simple.
     */
    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    /**
     * For Immutable Object this is simple.
     */
    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    /**
     * For Immutable Object this is simple.
     */
    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }
}
