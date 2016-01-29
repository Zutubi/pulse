// dependency: zutubi/namespace.js
// dependency: kendo/package.js

if (window.Zutubi.core === undefined)
{
    var feedbackHandlers = {
        success: function(m){},
        error: function(m){},
        warning: function(m){}
    };

    window.Zutubi.core = (function($)
    {
        return {
            registerFeedbackHandler: function(obj)
            {
                var name;
                for (name in feedbackHandlers)
                {
                    if (feedbackHandlers.hasOwnProperty(name) && obj[name])
                    {
                        feedbackHandlers[name] = jQuery.proxy(obj[name], obj);
                    }
                }
            },

            reportSuccess: function(message)
            {
                feedbackHandlers.success(message);
            },

            reportError: function(message)
            {
                feedbackHandlers.error(message);
            },

            reportWarning: function(message)
            {
                feedbackHandlers.warning(message);
            },

            arraysEqual: function(a1, a2)
            {
                var i;

                if (a1.length !== a2.length)
                {
                    return false;
                }

                for (i = 0; i < a1.length; i++)
                {
                    if (a1[i] !== a2[i])
                    {
                        return false;
                    }
                }

                return true;
            }
        };
    }(jQuery));
}
