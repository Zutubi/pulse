package com.cinnamonbob.util;

/**
 * Convenience class to help with asserting the correct state of the system.
 *
 */
public class Assert
{
	public static void notNull(Object object, String message)
    {
		if (object == null)
        {
			throw new IllegalArgumentException(message);
		}
	}
}
