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

public class ClassUserType implements UserType {

    // The Type name to be placed on @UserType annotation
    public static final String TYPE = "uk.gov.hmcts.ccd.definition.store.repository.ClassUserType";

    public ClassUserType() {
        // default constrcutor
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @Override
    public Class<?> returnedClass() {
        return Class.class;
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
        final String className = resultSet.getString(names[0]);
        return getClass(className);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement,
                            Object value,
                            int index,
                            SharedSessionContractImplementor sharedSessionContractImplementor) throws HibernateException, SQLException {
        if (value == null) {
            preparedStatement.setNull(index, Types.OTHER);
            return;
        }
        preparedStatement.setObject(index, ((Class<?>) value).getName(), Types.OTHER);
    }

    private Class<?> getClass(final String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
