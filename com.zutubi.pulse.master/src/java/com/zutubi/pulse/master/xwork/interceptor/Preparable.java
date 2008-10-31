package com.zutubi.pulse.master.xwork.interceptor;

import java.util.List;

/**
 * <class-comment/>
 */
public interface Preparable extends com.opensymphony.xwork.Preparable
{
    List<String> getPrepareParameterNames();
}
