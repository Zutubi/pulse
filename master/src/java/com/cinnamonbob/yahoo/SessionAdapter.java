package com.cinnamonbob.yahoo;

import ymsg.network.event.*;

/**
 * An implementation of the SessionListener interface that will allow subclasses to implement
 * only that portion of the interface they are interested in.
 * 
 */
public class SessionAdapter implements SessionListener
{
    public void buzzReceived(SessionEvent sessionEvent)
    {

    }

    public void chatConnectionClosed(SessionEvent sessionEvent)
    {

    }

    public void chatLogoffReceived(SessionChatEvent sessionChatEvent)
    {

    }

    public void chatLogonReceived(SessionChatEvent sessionChatEvent)
    {

    }

    public void chatMessageReceived(SessionChatEvent sessionChatEvent)
    {

    }

    public void chatUserUpdateReceived(SessionChatEvent sessionChatEvent)
    {

    }

    public void conferenceInviteDeclinedReceived(SessionConferenceEvent sessionConferenceEvent)
    {

    }

    public void conferenceInviteReceived(SessionConferenceEvent sessionConferenceEvent)
    {

    }

    public void conferenceLogoffReceived(SessionConferenceEvent sessionConferenceEvent)
    {

    }

    public void conferenceLogonReceived(SessionConferenceEvent sessionConferenceEvent)
    {

    }

    public void conferenceMessageReceived(SessionConferenceEvent sessionConferenceEvent)
    {

    }

    public void connectionClosed(SessionEvent sessionEvent)
    {

    }

    public void contactRejectionReceived(SessionEvent sessionEvent)
    {

    }

    public void contactRequestReceived(SessionEvent sessionEvent)
    {

    }

    public void errorPacketReceived(SessionErrorEvent sessionErrorEvent)
    {

    }

    public void fileTransferReceived(SessionFileTransferEvent sessionFileTransferEvent)
    {

    }

    public void friendAddedReceived(SessionFriendEvent sessionFriendEvent)
    {

    }

    public void friendRemovedReceived(SessionFriendEvent sessionFriendEvent)
    {

    }

    public void friendsUpdateReceived(SessionFriendEvent sessionFriendEvent)
    {

    }

    public void inputExceptionThrown(SessionExceptionEvent sessionExceptionEvent)
    {

    }

    public void listReceived(SessionEvent sessionEvent)
    {

    }

    public void messageReceived(SessionEvent sessionEvent)
    {

    }

    public void newMailReceived(SessionNewMailEvent sessionNewMailEvent)
    {

    }

    public void notifyReceived(SessionNotifyEvent sessionNotifyEvent)
    {

    }

    public void offlineMessageReceived(SessionEvent sessionEvent)
    {

    }
}
