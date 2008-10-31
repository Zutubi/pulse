package com.zutubi.pulse.master.transfer.xml;

import com.zutubi.pulse.master.transfer.TransferException;
import org.apache.ws.commons.util.Base64;

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
        switch (type)
        {
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.REAL :
            case Types.FLOAT :
            case Types.DOUBLE :
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.BOOLEAN:
            case Types.BIT:
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.TIMESTAMP:
            case Types.DATE:
            case Types.CLOB:
                return obj.toString();
            case Types.NULL:
                return null;
            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                byte[] data = (byte[]) obj;
                return Base64.encode(data, 0, data.length, 0, null);
            default:
                throw new RuntimeException("unsupported type: " + type + " value: " + obj.toString());
        }
    }

    public Object fromText(int type, String str) throws TransferException
    {
        if(str == null)
        {
            return null;
        }
        
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
            case Types.CLOB:
                return str;
            case Types.BINARY :
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                try
                {
                    return Base64.decode(str);
                }
                catch (Base64.DecodingException e)
                {
                    throw new TransferException(e);
                }
            case Types.TIMESTAMP:
                return Timestamp.valueOf(str);
            case Types.DATE:
                return Date.valueOf(str);
            default:
                throw new RuntimeException("unsupported type: " + type + " value: " + str);
        }
    }
}
