// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CvsException.java

package com.zutubi.pulse.core.scm.cvs.client;


public class CvsException extends Exception
{

    public CvsException()
    {
    }

    public CvsException(String message)
    {
        super(message);
    }

    public CvsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CvsException(Throwable cause)
    {
        super(cause);
    }
}
