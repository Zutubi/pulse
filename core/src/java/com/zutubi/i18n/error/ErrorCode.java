package com.zutubi.i18n.error;

/**
 * <class-comment/>
 */
public class ErrorCode
{

    private int errorCode;

    /**
     * Factory method to create an error code object
     *
     * @param errorCode number of the error code
     * @return created error code
     */
    public static ErrorCode error(int errorCode)
    {
        return new ErrorCode(errorCode);
    }

    /**
     * Construct an error code from the error number
     *
     * @param errorCode errorCode
     */
    public ErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    /**
     * Return a representation of the error code
     * with the error number
     *
     * @return representation of the error code
     */
    public String getError()
    {
        return String.valueOf(errorCode);
    }

    /**
     * Return the error code
     *
     * @return error code
     */
    public int getCode()
    {
        return errorCode;
    }

    public int hashCode()
    {
        return errorCode;
    }

    public boolean equals(Object obj)
    {
        if (null == obj || ! (obj instanceof ErrorCode))
        {
            return false;
        }
        return ((ErrorCode) obj).errorCode == errorCode;
    }
}