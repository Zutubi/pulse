package com.zutubi.pulse.prototype.record;

/**
 * The one object of this type is used to represent nulls in Records.  As
 * Records are stored as Maps, using null is not an option.
 */
public class Null
{
    public static final Null VALUE = new Null();

    private Null()
    {
    }
}
