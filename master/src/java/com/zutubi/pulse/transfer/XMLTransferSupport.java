package com.zutubi.pulse.transfer;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;

/**
 *
 *
 */
public class XMLTransferSupport
{
    public String toText(int type, Object obj)
    {
        return obj.toString();
    }

    public Object fromText(int type, String str)
    {
        switch (type)
        {
            case Types.BIGINT:
                return Long.parseLong(str);
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.SMALLINT:
                return Integer.parseInt(str);
            case Types.NULL:
                return null;
            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
                return Double.parseDouble(str);
            case Types.NUMERIC:
            case Types.DECIMAL:
                return new BigDecimal(str);
            case Types.BOOLEAN:
            case Types.BIT:
                return Boolean.parseBoolean(str);
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
                return str;
            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                return str.getBytes();
            case Types.TIMESTAMP:
                return Timestamp.valueOf(str);
            case Types.DATE:
                return Date.valueOf(str);
            default:
                throw new RuntimeException("unsupported type: " + type + " value: " + str);
        }
    }
}
