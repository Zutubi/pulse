// dependency: ./namespace.js
// dependency: zutubi/core/package.js
// dependency: zutubi/config/package.js

(function($)
{
    var currentNavigation = null,

    Navigation = function(url, targets, callback)
    {
        this.url = url;
        this.targets = targets;
        this.callback = callback;
        this.cancelled = false;
    };

    Navigation.prototype = {
        begin: function()
        {
            var that = this,
                i;

            for (i = 0; i < this.targets.length; i++)
            {
                that.targets[i].beginNavigation();
            }

            Zutubi.core.ajax({
                type: "GET",
                url: that.url,
                success: function (data)
                {
                    if (!that.cancelled)
                    {
                        that.end(that.callback(data));
                    }
                },
                error: function (jqXHR)
                {
                    if (!that.cancelled)
                    {
                        that.end("Load error: " + Zutubi.core.ajaxError(jqXHR));
                    }
                }
            });
        },

        end: function(error)
        {
            var i;

            for (i = 0; i < this.targets.length; i++)
            {
                this.targets[i].endNavigation(error);
            }
        },

        cancel: function()
        {
            this.end();
            this.cancelled = true;
        }
    };

    jQuery.extend(Zutubi.admin, {
        navigate: function(url, targets, callback)
        {
            if (currentNavigation)
            {
                currentNavigation.cancel();
            }

            currentNavigation = new Navigation(url, targets, callback);
            currentNavigation.begin();
        }
    });
}(jQuery));
