package com.zutubi.pulse.master.transfer;

import java.util.Map;

/**
 *
 *
 */
public interface TransferTarget
{
    void start() throws TransferException;

    void startTable(Table table) throws TransferException;

    void row(Map<String, Object> row) throws TransferException;

    void endTable() throws TransferException;

    void end() throws TransferException;

    void close() throws TransferException;
}
