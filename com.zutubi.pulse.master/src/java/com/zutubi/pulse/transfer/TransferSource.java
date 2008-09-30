package com.zutubi.pulse.transfer;

/**
 * 
 *
 */
public interface TransferSource
{
    void transferTo(TransferTarget target) throws TransferException;
}
