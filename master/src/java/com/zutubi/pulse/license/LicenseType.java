package com.zutubi.pulse.license;

/**
 * The LicenseType represents the different types / classes / categories of available licenses.
 * 
 */
public enum LicenseType
{
    EVALUATION
    {
        public final String getCode()
        {
            return "e";
        }
    },
    COMMERCIAL
    {
        public final String getCode()
        {
            return "c";
        }
    },
    NON_PROFIT
    {
        public final String getCode()
        {
            return "n";
        }
    },
    PERSONAL
    {
        public final String getCode()
        {
            return "p";
        }
    };

    public static LicenseType valueBy(String code)
    {
        for (LicenseType type : LicenseType.values())
        {
            if (type.getCode().equals(code))
            {
                return type;
            }
        }
        return null;
    }

    public abstract String getCode();
}
