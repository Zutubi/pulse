// dependency: zutubi/namespace.js

if (window.Zutubi.admin === undefined)
{
    window.Zutubi.admin = (function($)
    {
        var app = {},
            baseUrl = window.baseUrl;

        function _createNotificationWidget()
        {
            var notificationElement = $("#notification");
            return notificationElement.kendoNotification({
                autoHideAfter: 7000,
                allowHideAfter: 1000,
                button: true,
                hideOnClick: false,
                position: {
                    top: 50
                },
                stacking: "down"
            }).data("kendoNotification");
        }

        function _createRouter()
        {
            var router = new kendo.Router({
                root: baseUrl + "/admina",
                pushState: true,
                routeMissing: function(e)
                {
                    app.notificationWidget.error("Unknown admin path '" + e.url + "', redirecting.");
                    router.navigate("/");
                }
            });

            router.route("/", function()
            {
                router.navigate("/hierarchy/projects/");
            });

            router.route("/hierarchy/projects(/)(:name)(/)", function(name)
            {
                app.navbar.selectScope("projects");
                Zutubi.admin.showScope("projects", name);
            });

            router.route("/config/projects/*path", function(path)
            {
                app.navbar.selectScope("projects");
                Zutubi.admin.showConfig("projects/" + Zutubi.admin.normalisedPath(path), true);
            });

            router.route("/hierarchy/agents(/)(:name)(/)", function(name)
            {
                app.navbar.selectScope("agents");
                Zutubi.admin.showScope("agents", name);
            });

            router.route("/config/agents/*path", function(path)
            {
                app.navbar.selectScope("agents");
                if (path)
                {
                    Zutubi.admin.showConfig("agents/" + Zutubi.admin.normalisedPath(path), true);
                }
            });

            router.route("/config/settings/*path", function(path)
            {
                app.navbar.selectScope("settings");
                Zutubi.admin.showConfig("settings/" + Zutubi.admin.normalisedPath(path), false);
            });

            router.route("/config/users/*path", function(path)
            {
                app.navbar.selectScope("users");
                Zutubi.admin.showConfig("users/" + Zutubi.admin.normalisedPath(path), false);
            });

            router.route("/config/groups/*path", function(path)
            {
                app.navbar.selectScope("groups");
                Zutubi.admin.showConfig("groups/" + Zutubi.admin.normalisedPath(path), false);
            });

            router.route("/plugins(/:id)", function(id)
            {
                app.navbar.selectScope("plugins");
                console.log("plugin: " + id);
            });

            return router;
        }

        function _createNavbar()
        {
            var navbar = $("#navbar").kendoZaNavbar().data("kendoZaNavbar");
            navbar.bind("scope-selected", function(e)
            {
                if (e.scope === "projects" || e.scope === "agents")
                {
                    app.router.navigate("/hierarchy/" + e.scope + "/");
                }
                else if (e.scope === "plugins")
                {
                    app.router.navigate("/plugins/");
                }
                else
                {
                    app.router.navigate("/config/" + e.scope + "/");
                }
            });

            return navbar;
        }

        function _coerceInt(properties, name)
        {
            var value, newValue;
            if (properties.hasOwnProperty(name))
            {
                value = properties[name];
                if (value === "")
                {
                    newValue = null;
                }
                else
                {
                    newValue = Number(value);
                }

                properties[name] = newValue;
            }
        }

        return {
            app: app,

            ACTION_ICONS: {
                "add": "plus-circle",
                "addComment": "comment",
                "changePassword": "key",
                "clean": "eraser",
                "clearResponsibility": "user-times",
                "clone": "clone",
                "convertToCustom": "code",
                "convertToVersioned": "file-code-o",
                "delete": "trash",
                "disable": "toggle-off",
                "enable": "toggle-on",
                "fire": "bolt",
                "hide": "trash-o",
                "initialise": "refresh",
                "kill": "exclamation-circle",
                "pause": "pause",
                "pin": "thumb-tack",
                "ping": "bullseye",
                "pullUp": "angle-double-up",
                "pushDown": "angle-double-down",
                "rebuild": "bolt",
                "reload": "repeat",
                "rename": "pencil",
                "restore": "plus-square-o",
                "resume": "play",
                "setPassword": "key",
                "takeResponsibility": "wrench",
                "trigger": "bolt",
                "unpin": "minus",
                "view": "arrow-circle-right",
                "write": "pencil-square-o"
            },

            LINK_ICONS: {
                "config": "pencil",
                "dependencies": "sitemap",
                "home": "home",
                "homepage": "external-link",
                "history": "clock-o",
                "info": "info-circle",
                "log": "file-text",
                "messages": "files-o",
                "reports": "bar-chart",
                "rss": "rss",
                "statistics": "pie-chart",
                "status": "heartbeat"
            },

            init: function()
            {
                app.notificationWidget = _createNotificationWidget();
                app.router = _createRouter();
                app.navbar = _createNavbar();
            },

            start: function()
            {
                app.router.start();
            },

            normalisedPath: function(path)
            {
                if (!path)
                {
                    return "";
                }

                if (path.length > 0 && path[0] === "/")
                {
                    path = path.substring(1);
                }
                if (path.length > 0 && path[path.length - 1] === "/")
                {
                    path = path.substring(0, path.length - 1);
                }

                return path;
            },

            subPath: function(path, begin, end)
            {
                var elements = path.split("/");

                if (typeof end === "undefined")
                {
                    end = elements.length;
                }

                elements = elements.slice(begin, end);
                return elements.join("/");
            },

            parentPath: function(path)
            {
                var i = path.lastIndexOf("/");
                if (i >= 0)
                {
                    return path.substring(0, i);
                }

                return null;
            },

            baseName: function(path)
            {
                var i = path.lastIndexOf("/");
                if (i >= 0)
                {
                    return path.substring(i + 1);
                }

                return path;
            },

            reportSuccess: function(message)
            {
                app.notificationWidget.success(message);
            },

            reportError: function(message)
            {
                app.notificationWidget.error(message);
            },

            replaceConfigPath: function (newPath)
            {
                app.router.replace("/config/" + newPath, true);
            },

            showScope: function(scope, name)
            {
                if (app.configPanel)
                {
                    app.configPanel.destroy();
                    delete app.configPanel;
                }

                if (!app.scopePanel)
                {
                    app.scopePanel = new Zutubi.admin.ScopePanel("#config-view");
                    app.scopePanel.bind("select", function(e)
                    {
                        var url = "/hierarchy/" + e.scope;
                        if (e.name.length > 0)
                        {
                            url += "/" + e.name;
                        }

                        app.router.navigate(url, true);
                    });
                }

                app.scopePanel.setScope(scope, name);
            },

            showConfig: function (path, templated)
            {
                var rootIndex = templated ? 2 : 1,
                    rootPath = Zutubi.admin.subPath(path, 0, rootIndex),
                    configPath = Zutubi.admin.subPath(path, rootIndex);

                if (app.scopePanel)
                {
                    app.scopePanel.destroy();
                    delete app.scopePanel;
                }

                if (!app.configPanel)
                {
                    app.configPanel = new Zutubi.admin.ConfigPanel("#config-view");
                    app.configPanel.bind("pathselect", function(e)
                    {
                        app.router.navigate("/config/" + e.path, true);
                    });
                }

                app.configPanel.setPaths(rootPath, configPath);
            },

            coerceProperties: function(properties, propertyTypes)
            {
                var i,
                    propertyType;

                if (propertyTypes)
                {
                    for (i = 0; i < propertyTypes.length; i++)
                    {
                        propertyType = propertyTypes[i];
                        if (propertyType.shortType === "int")
                        {
                            _coerceInt(properties, propertyType.name);
                        }
                    }
                }
            }
        };
    }(jQuery));
}
