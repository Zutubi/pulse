package com.zutubi.pulse.acceptance.rpc;

/**
 * Simple base client for wrappers around XML-RPC APIs.
 */
public class ApiClient
{
    private String api;
    private RpcClient rpc;

    /**
     * Creates a new client that will be used to call methods on the given API.
     *
     * @param api API tat calls will be made to
     * @param rpc underlying client used to make RPC calls
     */
    public ApiClient(String api, RpcClient rpc)
    {
        this.api = api;
        this.rpc = rpc;
    }

    /**
     * Calls the given function on this API, without implicitly passing the
     * current login token as the first argument.
     *
     * @param function name of the function to call
     * @param args     arguments to pass to the function
     * @param <T> type of the returned object
     * @return the result of the API call
     * @throws Exception on error
     */
    @SuppressWarnings({"unchecked"})
    public <T> T callWithoutToken(String function, Object... args) throws Exception
    {
        return (T) rpc.callWithoutToken(api, function, args);
    }

    /**
      * Calls the given function on this API, implicitly passing the current
      * login token as the first argument.
      *
      * @param function name of the function to call
      * @param args     arguments to pass to the function
      * @param <T> type of the returned object
      * @return the result of the API call
      * @throws Exception on error
      */
    @SuppressWarnings({"unchecked"})
    public <T> T call(String function, Object... args) throws Exception
    {
        return (T) rpc.callApi(api, function, args);
    }
}
