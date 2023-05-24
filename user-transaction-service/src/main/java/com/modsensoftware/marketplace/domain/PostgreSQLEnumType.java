package com.modsensoftware.marketplace.domain;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;

/**
 * @author andrey.demyanchik on 12/29/2022
 */
public class PostgreSQLEnumType extends EnumType<UserTransactionStatus> {
    @Override
    public void nullSafeSet(java.sql.PreparedStatement st,
                            Object value,
                            int index,
                            SharedSessionContractImplementor session)
            throws HibernateException, java.sql.SQLException {
        if (value == null) {
            st.setNull(index, java.sql.Types.VARCHAR);
        } else {
            st.setObject(index, value.toString(), java.sql.Types.OTHER);
        }
    }
}
