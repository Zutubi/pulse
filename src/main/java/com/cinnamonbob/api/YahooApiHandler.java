package com.cinnamonbob.api;

import ymsg.network.event.*;
import ymsg.network.Session;

import java.io.IOException;

/**
 *
 */
public class YahooApiHandler
{
    private Session yahooSession;
    private class YahooSessionAdapter implements SessionListener
    {
        public void fileTransferReceived(SessionFileTransferEvent sessionFileTransferEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.fileTransferReceived");
        }
        public void connectionClosed(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.connectionClosed");
        }
        public void listReceived(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.listReceived");
        }
        public void messageReceived(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.messageReceived");
        }
        public void buzzReceived(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.buzzReceived");
        }
        public void offlineMessageReceived(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.offlineMessageReceived");
        }
        public void errorPacketReceived(SessionErrorEvent sessionErrorEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.errorPacketReceived");
        }
        public void inputExceptionThrown(SessionExceptionEvent sessionExceptionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.inputExceptionThrown");
        }
        public void newMailReceived(SessionNewMailEvent sessionNewMailEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.newMailReceived");
        }
        public void notifyReceived(SessionNotifyEvent sessionNotifyEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.notifyReceived");
        }
        public void contactRequestReceived(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.contactRequestReceived");
        }
        public void contactRejectionReceived(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.contactRejectionReceived");
        }
        public void conferenceInviteReceived(SessionConferenceEvent sessionConferenceEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.conferenceInviteReceived");
        }
        public void conferenceInviteDeclinedReceived(SessionConferenceEvent sessionConferenceEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.conferenceInviteDeclinedReceived");
        }
        public void conferenceLogonReceived(SessionConferenceEvent sessionConferenceEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.conferenceLogonReceived");
        }
        public void conferenceLogoffReceived(SessionConferenceEvent sessionConferenceEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.conferenceLogoffReceived");
        }
        public void conferenceMessageReceived(SessionConferenceEvent sessionConferenceEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.conferenceMessageReceived");
        }
        public void friendsUpdateReceived(SessionFriendEvent sessionFriendEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.friendsUpdateReceived");
        }
        public void friendAddedReceived(SessionFriendEvent sessionFriendEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.friendAddedReceived");
        }
        public void friendRemovedReceived(SessionFriendEvent sessionFriendEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.friendRemovedReceived");
        }
        public void chatLogonReceived(SessionChatEvent sessionChatEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.chatLogonReceived");
        }
        public void chatLogoffReceived(SessionChatEvent sessionChatEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.chatLogoffReceived");
        }
        public void chatMessageReceived(SessionChatEvent sessionChatEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.chatMessageReceived");
        }
        public void chatUserUpdateReceived(SessionChatEvent sessionChatEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.chatUserUpdateReceived");
        }
        public void chatConnectionClosed(SessionEvent sessionEvent){
            System.out.println("YahooContactPoint$YahooSessionAdapter.chatConnectionClosed");
        }
    }

    private void handleIncomingMessage(SessionEvent evt) throws IOException
    {
        String message = evt.getMessage();
        String yahooid = evt.getFrom();
        if (message == null || message.length() == 0)
        {
            return;
        }
        if ("help".equals(message))
        {
            yahooSession.sendMessage(yahooid, "Usage:");
            yahooSession.sendMessage(yahooid, "    details:");
            yahooSession.sendMessage(yahooid, "    thank you:");
            yahooSession.sendMessage(yahooid, "    exit:");
            yahooSession.sendMessage(yahooid, "    help:");
        }
        else if ("thank you".equals(message))
        {
            yahooSession.sendMessage(yahooid, "your welcome.");
            yahooSession.logout();
        }
        else if ("exit".equals(message))
        {
            yahooSession.logout();
        }
        else if ("details".equals(message))
        {
            yahooSession.sendMessage(yahooid, "details are available from http://localhost:8080/");
        }
        else {
            yahooSession.sendMessage(yahooid, "Unknown message, try help for a list of commands.");
        }
    }

//    public void notify(Project project, BuildResult result)
//    {
//        try
//        {
//            yahooSession = new Session();
//            yahooSession.addSessionListener(new YahooSessionAdapter()
//            {
//                public void messageReceived(SessionEvent evt)
//                {
//                    try
//                    {
//                        handleIncomingMessage(evt.getMessage());
//                    }
//                    catch (IOException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//
//                public void notifyReceived(SessionNotifyEvent sessionNotifyEvent)
//                {
//
//                }
//
//            });
////            yahooSession.login("bob_cinnamon", "c1nnam0n");
//            yahooSession.login("cinnamon_robert", "c1nnam0n");
//            yahooSession.sendMessage(getYahooId(), "new build has finished.");
//        }
//        catch (AccountLockedException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        catch (LoginRefusedException e)
//        {
//            e.printStackTrace();
//        }
//    }
}
