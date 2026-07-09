package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.rule.expression.Context;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.Hibernate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.HibernateException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.usertype.EnhancedUserType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.usertype.ParameterizedType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.io.Serializable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.PreparedStatement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.ResultSet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.sql.SQLException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Properties;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 * A generic UserType that handles String-based JDK 5.0 Enums.
 *
 * @author Gavin King
 */
public class IntegerEnumUserType implements EnhancedUserType, ParameterizedType {

    private Class<Context> enumClass;

    public void setParameterValues(Properties parameters) {
        String enumClassName = parameters.getProperty("enumClassname");
        try {
            enumClass = (Class<Context>) Class.forName(enumClassName);
        } catch (ClassNotFoundException cnfe) {
            throw new HibernateException("Enum class not found", cnfe);
        }
    }

    public Class returnedClass() {
        return enumClass;
    }

    public int getSqlType() {
        return java.sql.Types.INTEGER;
    }

    public int[] sqlTypes() {
        return new int[] { java.sql.Types.INTEGER };
    }

    public boolean isMutable() {
        return false;
    }

    public Object deepCopy(Object value) {
        return value;
    }

    public Serializable disassemble(Object value) {
        return (Context) value;
    }

    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    public boolean equals(Object x, Object y) {
        return x == y;
    }

    public int hashCode(Object x) {
        return x.hashCode();
    }

    public Object fromXMLString(String xmlValue) {
        return Enum.valueOf(enumClass, xmlValue);
    }

    public String objectToSQLString(Object value) {
        return '\'' + ((Context) value).getCode().toString() + '\'';
    }

    public String toXMLString(Object value) {
        return ((Context) value).getCode().toString();
    }

    public Object nullSafeGet(ResultSet rs, int position, org.hibernate.engine.spi.SharedSessionContractImplementor session, Object owner) throws SQLException {
        String name = rs.getString(position);
        // return rs.wasNull() ? null : Enum.valueOf(enumClass, name);
        return rs.wasNull() ? null : Context.getByCode(Integer.parseInt(name));
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index, org.hibernate.engine.spi.SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, java.sql.Types.INTEGER);
        } else {
            // st.setParameter(index, ((Enum) value).name());
            st.setInt(index, ((Context) value).getCode());
        }
    }


    public Object fromStringValue(CharSequence sequence) {
        return fromXMLString(sequence.toString());
    }

    public String toSqlLiteral(Object value) {
        return toXMLString(value);
    }

    public String toString(Object value) {
        return toXMLString(value);
}
}
