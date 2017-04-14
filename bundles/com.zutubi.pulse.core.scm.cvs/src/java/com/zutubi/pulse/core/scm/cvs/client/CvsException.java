/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
