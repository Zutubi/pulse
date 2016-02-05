// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        NavbarItem = Zutubi.core.NavbarItem,
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
                items.push(this._createItem('next healthy', options.nextSuccessful, options.personalBuild));
            }
            if (options.nextBroken)
            {
                items.push(this._createItem('next broken', options.nextBroken, options.personalBuild));
            }
            if (options.previousSuccessful)
            {
                items.push(this._createItem('previous healthy', options.previousSuccessful, options.personalBuild));
            }
            if (options.previousBroken)
            {
                items.push(this._createItem('previous broken', options.previousBroken, options.personalBuild));
            }
            if (options.latest)
            {
                items.push(this._createItem('latest', options.latest, options.personalBuild));
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
            name: "ZaBuildNavbarItem",
            urlItemTemplate: '<li><a class="#= cls #" href="#= url #"><span class="fa fa-circle #= statusCls #"></span> #: text #</a></li>'
        },

        _createItem: function(text, build)
        {
            return {
                cls: 'k-selector-popup-item k-build-menu-item',
                statusCls: 'k-status-' + build.status,
                text: text + ' (build ' + build.number + ')',
                url: this._getUrl(build)
            };
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

    Zutubi.reporting.BuildContextNavbarItem = NavbarItem.extend({
        init: function(element, options)
        {
            this.isPersonal = options.personalBuild;
            this.selectedTab = options.selectedTab || "summary";

            NavbarItem.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaBuildContextNavbarItem",
            model: {},
            template: '<span class="k-build-context"></span>',
            itemTemplate: '<a class="#= cls #" href="#= url #"><span class="fa fa-square"><span></a>'
        },

        create: function()
        {
            var builds = this.options.builds,
                buildId = this.options.buildId,
                i,
                build,
                el,
                tip;

            this.itemTemplate = kendo.template(this.options.itemTemplate);

            NavbarItem.fn.create.call(this);

            for (i = 0; i < builds.length; i++)
            {
                build = builds[i];
                el = $(this.itemTemplate({
                    url: this._getUrl(build),
                    cls: 'k-status-' + build.status + (build.id === buildId ? ' k-build-context-current' : '')
                }));
                this.innerElement.append(el);

                if (build.id < buildId)
                {
                    tip = "step backwards to build " + build.number;
                }
                else if (build.id > buildId)
                {
                    tip = "step forwards to build " + build.number;
                }
                else
                {
                    tip = "this build";
                }

                el.kendoTooltip({content: tip});
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
            var extraItems = [],
                buildOptions;

            if (options.buildId)
            {
                buildOptions = jQuery.extend({
                    personalBuild: true
                }, options, options.data);

                extraItems.push({
                    type: "kendoZaBuildNavbarItem",
                    options: buildOptions
                }, {
                    type: "kendoZaBuildContextNavbarItem",
                    options: buildOptions
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
            var buildOptions,
                extraItems = [{
                    type: 'kendoZaSeparatorNavbarItem'
                }, {
                options: {
                    model: {
                        content: kendo.htmlEncode(options.projectName),
                        url: options.projectUrl
                    }
                }
            }];

            if (options.buildId)
            {
                buildOptions = jQuery.extend({
                    personalBuild: false
                }, options, options.data);

                extraItems.push({
                    type: 'kendoZaSeparatorNavbarItem'
                }, {
                    type: "kendoZaBuildNavbarItem",
                    options: buildOptions
                }, {
                    type: "kendoZaBuildContextNavbarItem",
                    options: buildOptions
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
                    type: 'kendoZaSeparatorNavbarItem'
                }, {
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
    ui.plugin(Zutubi.reporting.BuildContextNavbarItem);
    ui.plugin(Zutubi.reporting.UserNavbar);
    ui.plugin(Zutubi.reporting.ProjectNavbar);
    ui.plugin(Zutubi.reporting.AgentNavbar);
}(jQuery));
