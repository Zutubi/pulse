package com.zutubi.pulse.transfer;

/**
 *
 *
 */
public class TransferColumn implements Column
{
    private String name;
    private int sqlTypeCode;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getSqlTypeCode()
    {
        return sqlTypeCode;
    }

    public void setSqlTypeCode(int sqlTypeCode)
    {
        this.sqlTypeCode = sqlTypeCode;
    }
}
