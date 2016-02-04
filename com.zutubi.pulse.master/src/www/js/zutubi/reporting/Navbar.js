// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        MenuNavbarItem = Zutubi.core.MenuNavbarItem,
        Navbar = Zutubi.core.Navbar;

    Zutubi.reporting.BuildNavbarItem = MenuNavbarItem.extend({
        init: function(element, options)
        {
            var items = [],
                build = jQuery.grep(options.builds, function(item)
                {
                    return item.id === options.buildId;
                })[0];

            this.isPersonal = options.personalBuild;
            this.selectedTab = options.selectedTab || "summary";

            if (options.nextSuccessful)
            {
                items.push(this._createItem('next healthy (build ' + options.nextSuccessful.number + ')', options.nextSuccessful, options.personalBuild));
            }
            if (options.nextBroken)
            {
                items.push(this._createItem('next broken (build ' + options.nextBroken.number + ')', options.nextBroken, options.personalBuild));
            }
            if (options.previousSuccessful)
            {
                items.push(this._createItem('previous healthy (build ' + options.previousSuccessful.number + ')', options.previousSuccessful, options.personalBuild));
            }
            if (options.previousBroken)
            {
                items.push(this._createItem('previous broken (build ' + options.previousBroken.number + ')', options.previousBroken, options.personalBuild));
            }
            if (options.latest)
            {
                items.push(this._createItem('latest (build ' + options.latest.number + ')', options.latest, options.personalBuild));
            }

            jQuery.extend(options, {
                items: items,
                model: {
                    content: 'build ' + build.number
                }
            });

            MenuNavbarItem.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaBuildNavbarItem"
        },

        _createItem: function(text, build)
        {
            return {
                text: text,
                url: this._getUrl(build)
            }
        },

        _getUrl: function(build)
        {
            if (this.isPersonal)
            {
                return '/dashboard/my/' + build.number + '/' + this.selectedTab;
            }
            else
            {
                return '/browse/projects/' + encodeURIComponent(build.name) + '/builds/' + build.number + '/' + this.selectedTab;
            }
        }
    });

    Zutubi.reporting.UserNavbar = Navbar.extend({
        init: function(element, options)
        {
            var extraItems = [];
            if (options.buildId)
            {
                extraItems.push({
                    type: "kendoZaBuildNavbarItem",
                    options: jQuery.extend({
                        personalBuild: true
                    }, options, options.data)
                });
            }

            jQuery.extend(options, {
                extraItems: extraItems
            });

            Navbar.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaUserNavbar"
        }
    });

    Zutubi.reporting.ProjectNavbar = Navbar.extend({
        init: function(element, options)
        {
            var extraItems = [{
                options: {
                    model: {
                        content: kendo.htmlEncode(options.projectName),
                        url: options.projectUrl
                    }
                }
            }];

            if (options.buildId)
            {
                extraItems.push({
                    type: "kendoZaBuildNavbarItem",
                    options: jQuery.extend({}, options, options.data)
                });
            }

            jQuery.extend(options, {
                extraItems: extraItems
            });

            Navbar.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaProjectNavbar"
        }
    });

    Zutubi.reporting.AgentNavbar = Navbar.extend({
        init: function(element, options)
        {
            jQuery.extend(options, {
                extraItems: [{
                    options: {
                        model: {
                            content: kendo.htmlEncode(options.agentName),
                            url: options.agentUrl
                        }
                    }
                }]
            });

            Navbar.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaAgentNavbar"
        }
    });

    ui.plugin(Zutubi.reporting.BuildNavbarItem);
    ui.plugin(Zutubi.reporting.UserNavbar);
    ui.plugin(Zutubi.reporting.ProjectNavbar);
    ui.plugin(Zutubi.reporting.AgentNavbar);
}(jQuery));
