package com.cinnamonbob.api;

import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.model.GrantedAuthority;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 */
public class TokenManager
{
    private Set<String> validTokens = new TreeSet<String>();
    private UserManager userManager;

    public synchronized String login(String username, String password) throws AuthenticationException
    {
        return login(username, password, Constants.MINUTE * 30);
    }

    public String login(String username, String password, long expiry) throws AuthenticationException
    {
        User user = userManager.getUser(username);
        if (user == null)
        {
            // This allows guessing of usernames: do we care?
            throw new AuthenticationException("Invalid username");
        }

        if (!password.equals(user.getPassword()))
        {
            throw new AuthenticationException("Invalid password");
        }

        // Generate a token which is good for 30 minutes for this user
        long expiryTime = System.currentTimeMillis() + expiry;
        String signatureValue = getTokenSignature(username, expiryTime, password);
        String tokenValue = username + ":" + expiryTime + ":" + signatureValue;
        String encoded = new String(Base64.encodeBase64(tokenValue.getBytes()));

        validTokens.add(encoded);

        return encoded;
    }

    public synchronized boolean logout(String token)
    {
        try
        {
            verifyToken(token);
        }
        catch (AuthenticationException e)
        {
            return false;
        }

        validTokens.remove(token);
        return true;
    }

    public void verifyAdmin(String token) throws AuthenticationException
    {
        verifyRoleIn(token, GrantedAuthority.ADMINISTRATOR);
    }

    public void verifyUser(String token) throws AuthenticationException
    {
        verifyRoleIn(token, GrantedAuthority.USER);
    }

    public void verifyRoleIn(String token, GrantedAuthority... allowedAuthorities) throws AuthenticationException
    {
        User user = verifyToken(token);
        for (GrantedAuthority authority : user.getAuthorities())
        {
            for (GrantedAuthority allowedAuthority : allowedAuthorities)
            {
                if (authority.getAuthority().equals(allowedAuthority.getAuthority()))
                {
                    return;
                }
            }
        }

        throw new AuthenticationException("Access denied");
    }

    private synchronized User verifyToken(String token) throws AuthenticationException
    {
        if (!validTokens.contains(token))
        {
            throw new AuthenticationException("Invalid token");
        }

        // Token cannot have a bad format or expiry as it was found in
        // validTokens.  We don't even have to verify the signature
        // separately!
        String decoded = new String(Base64.decodeBase64(token.getBytes()));
        String parts[] = decoded.split(":");

        long expiry = Long.parseLong(parts[1]);
        if (System.currentTimeMillis() > expiry)
        {
            validTokens.remove(token);
            throw new AuthenticationException("Token expired");
        }

        User user = userManager.getUser(parts[0]);

        // This can happen if the user is removed in the mean time
        if (user == null)
        {
            throw new AuthenticationException("Unknown user");
        }

        return user;
    }

    private String getTokenSignature(String username, long expiryTime, String password)
    {
        return DigestUtils.md5Hex(username + ":" + expiryTime + ":" + password);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

}
