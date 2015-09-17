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
                    this.app.notificationWidget.error("Unknown admin path '" + e.url + "', redirecting.");
                    router.navigate("/");
                }
            });

            router.route("/", function()
            {
                router.navigate("/hierarchy/projects/");
            });

            router.route("/hierarchy/projects/(:name)", function(path, name)
            {
                app.navbar.selectScope("projects");
                Zutubi.admin.showScope("projects", name);
            });

            router.route("/config/projects/*path", function(path)
            {
                app.navbar.selectScope("projects");
                Zutubi.admin.showConfig("projects/" + path, true);
            });

            router.route("/hierarchy/agents/(:name)", function(path, name)
            {
                app.navbar.selectScope("agents");
                Zutubi.admin.showScope("agents", name);
            });

            router.route("/config/agents/*path", function(path)
            {
                app.navbar.selectScope("agents");
                if (path)
                {
                    Zutubi.admin.showConfig("agents/" + path, true);
                }
            });

            router.route("/config/settings/*path", function(path)
            {
                app.navbar.selectScope("settings");
                Zutubi.admin.showConfig("settings/" + (path || ""), false);
            });

            router.route("/config/users/*path", function(path)
            {
                app.navbar.selectScope("users");
                Zutubi.admin.showConfig("users/" + (path || ""), false);
            });

            router.route("/config/groups/*path", function(path)
            {
                app.navbar.selectScope("groups");
                Zutubi.admin.showConfig("groups/" + (path || ""), false);
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

        return {
            app: app,
            
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

            showScope: function(scope)
            {
                if (app.configPanel)
                {
                    app.configPanel.destroy();
                    delete app.configPanel;
                }

                if (!app.scopePanel)
                {
                    app.scopePanel = new Zutubi.admin.ScopePanel("#config-view");
                }

                app.scopePanel.setScope(scope);
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
                    app.configPanel.bind("pathselect", function (e) {
                        app.router.navigate("/config/" + e.path, true);
                    });
                }

                app.configPanel.setPaths(rootPath, configPath);
            }
        };
    }(jQuery));
}
