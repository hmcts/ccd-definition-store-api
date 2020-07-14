package uk.gov.hmcts.ccd.definition.store.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;

public class PostgreSQLEnumType<T extends Enum<T>> extends EnumType {

    private Class<T> clazz;

    @Override
    public void setParameterValues(Properties parameters) {
        String className = (String) parameters.get("type");

        if (className == null) {
            throw new HibernateException("Parameter 'type' must be set to determine the Enum class");
        }

        try {
            this.clazz = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new HibernateException("Couldn't get the class for name [" + className + "].", e);
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws SQLException {
        return EnumUtil.getEnumFromString(clazz, rs.getString(names[0]));
    }

    @Override
    public void nullSafeSet(
        PreparedStatement st,
        Object value,
        int index,
        SharedSessionContractImplementor session) throws SQLException {

        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(
                index,
                value.toString(),
                Types.OTHER
            );
        }
    }

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.OTHER };
    }

}
