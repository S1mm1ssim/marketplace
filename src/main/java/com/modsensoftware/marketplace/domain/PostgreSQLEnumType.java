package com.modsensoftware.marketplace.domain;

import com.modsensoftware.marketplace.enums.Role;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.EnumType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author andrey.demyanchik on 11/7/2022
 */
public class PostgreSQLEnumType extends EnumType<Role> {
    @Override
    public void nullSafeSet(PreparedStatement st,
                            Object value,
                            int index,
                            SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setObject(index, value.toString(), Types.OTHER);
        }
    }
}
