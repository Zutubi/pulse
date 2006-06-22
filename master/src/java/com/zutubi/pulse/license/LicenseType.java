package com.zutubi.pulse.license;

/**
 * The LicenseType represents the different types / classes / categories of available licenses.
 * 
 */
public enum LicenseType
{
    EVALUATION
    {
        public String getKey()
        {
            return "license.type.evaluation";
        }
    },
    COMMERCIAL
    {
        public String getKey()
        {
            return "license.type.commercial";
        }
    },
    NON_PROFIT
    {
        public String getKey()
        {
            return "license.type.nonprofit";
        }
    },
    CUSTOM
    {
        public String getKey()
        {
            return "license.type.custom";
        }
    };

    public static LicenseType valueOf(int i)
    {
        for (LicenseType type : LicenseType.values())
        {
            if (type.ordinal() == i)
            {
                return type;
            }
        }
        return null;
    }

    public abstract String getKey();
}
