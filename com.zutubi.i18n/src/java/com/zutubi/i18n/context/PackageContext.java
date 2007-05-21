package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class PackageContext implements Context
{
    private final String packageName;

    public PackageContext(String packageName)
    {
        this.packageName = packageName;
    }

    public PackageContext(Object obj)
    {
        packageName = getPackageName(obj);
    }

    public InputStream getResourceAsStream(String name)
    {
        return getClass().getResourceAsStream(name);
    }

    private String getPackageName(Object context)
    {
        if (context == null || context.getClass().getPackage() == null)
        {
            return "";
        }

        if (context instanceof Class)
        {
            return ((Class) context).getPackage().getName();
        }
        else
        {
            return context.getClass().getPackage().getName();
        }
    }

    public String getContext()
    {
        return packageName;
    }


    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof PackageContext))
        {
            return false;
        }

        final PackageContext packageContext = (PackageContext) o;

        if (packageName != null ? !packageName.equals(packageContext.packageName) : packageContext.packageName != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return packageName != null ? packageName.hashCode() : 0;
    }

    public String toString()
    {
        return "<" + packageName + ">";
    }


}
