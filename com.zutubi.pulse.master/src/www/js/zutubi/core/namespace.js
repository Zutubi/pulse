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
            }
        }
    }(jQuery));
}
