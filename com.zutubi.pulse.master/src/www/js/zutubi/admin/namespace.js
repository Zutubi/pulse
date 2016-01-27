// dependency: zutubi/namespace.js
// dependency: zutubi/config/package.js

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

        function _showScope(scope, name)
        {
            if (app.configPanel)
            {
                app.configPanel.destroy();
                delete app.configPanel;
            }

            if (app.pluginsPanel)
            {
                app.pluginsPanel.destroy();
                delete app.pluginsPanel;
            }

            if (!app.scopePanel)
            {
                app.scopePanel = new Zutubi.admin.ScopePanel("#config-view");
                app.scopePanel.bind("select", function(e)
                {
                    var url = "/hierarchy/" + e.scope;
                    if (e.name.length > 0)
                    {
                        url += "/" + encodeURIComponent(e.name);
                    }

                    app.router.navigate(url, true);
                });
            }

            app.scopePanel.setScope(scope, name);
        }

        function _showConfig(path, templated)
        {
            var rootIndex = templated ? 2 : 1,
                rootPath = Zutubi.config.subPath(path, 0, rootIndex),
                configPath = Zutubi.config.subPath(path, rootIndex);

            if (app.scopePanel)
            {
                app.scopePanel.destroy();
                delete app.scopePanel;
            }

            if (app.pluginsPanel)
            {
                app.pluginsPanel.destroy();
                delete app.pluginsPanel;
            }

            if (!app.configPanel)
            {
                app.configPanel = new Zutubi.admin.ConfigPanel("#config-view");
                app.configPanel.bind("delta", function(e)
                {
                    app.navbar.applyDelta(e.delta);
                });
                app.configPanel.bind("pathselect", function(e)
                {
                    app.router.navigate("/config/" + Zutubi.config.encodePath(e.path), true);
                });
            }

            app.configPanel.setPaths(rootPath, configPath);
        }

        function _showPlugins(id)
        {
            if (app.scopePanel)
            {
                app.scopePanel.destroy();
                delete app.scopePanel;
            }

            if (app.configPanel)
            {
                app.configPanel.destroy();
                delete app.configPanel;
            }

            if (!app.pluginsPanel)
            {
                app.pluginsPanel = new Zutubi.admin.PluginsPanel("#config-view", app.isAdmin);
            }

            app.pluginsPanel.setId(id);
            app.pluginsPanel.bind("pluginSelected", function(e)
            {
                var url = "/plugins/" + Zutubi.config.encodePath(e.id);
                if (e.initialDefault)
                {
                    app.router.replace(url);

                }
                else
                {
                    app.router.navigate(url, true);
                }
            });
        }

        function _createRouter()
        {
            var router = new kendo.Router({
                root: baseUrl + "/admin",
                pushState: true,
                routeMissing: function(e)
                {
                    app.notificationWidget.error("Unknown admin path '" + e.url + "', redirecting.");
                    router.navigate("/");
                }
            });

            router.route("/", function()
            {
                router.navigate("/hierarchy/projects");
            });

            router.route("/hierarchy/projects(/)(:name)(/)", function(name)
            {
                app.navbar.selectScope("projects");
                _showScope("projects", name);
            });

            router.route("/config/projects(/)*path", function(path)
            {
                var normalisedPath = Zutubi.config.normalisedPath(path);
                if (normalisedPath)
                {
                    app.navbar.selectScope("projects", normalisedPath);
                    _showConfig("projects/" + normalisedPath, true);
                }
                else
                {
                    app.router.replace("/hierarchy/projects");
                }
            });

            router.route("/hierarchy/agents(/)(:name)(/)", function(name)
            {
                app.navbar.selectScope("agents");
                _showScope("agents", name);
            });

            router.route("/config/agents(/)*path", function(path)
            {
                var normalisedPath = Zutubi.config.normalisedPath(path);
                if (normalisedPath)
                {
                    app.navbar.selectScope("agents", normalisedPath);
                    _showConfig("agents/" + normalisedPath, true);
                }
                else
                {
                    app.router.replace("/hierarchy/agents");
                }
            });

            router.route("/config/settings(/)*path", function(path)
            {
                app.navbar.selectScope("settings");
                _showConfig("settings/" + Zutubi.config.normalisedPath(path), false);
            });

            router.route("/config/users(/)*path", function(path)
            {
                app.navbar.selectScope("users");
                _showConfig("users/" + Zutubi.config.normalisedPath(path), false);
            });

            router.route("/config/groups(/)*path", function(path)
            {
                app.navbar.selectScope("groups");
                _showConfig("groups/" + Zutubi.config.normalisedPath(path), false);
            });

            router.route("/plugins(/)(:id)", function(id)
            {
                app.navbar.selectScope("plugins");
                _showPlugins(id);
            });

            return router;
        }

        function _showAddWizard(scope)
        {
            var path, item, label, window;

            if (app.scopePanel)
            {
                path = scope;
                item = app.scopePanel.getItem();
                if (item)
                {
                    path += "/" + item;
                }
            }
            else if (app.configPanel)
            {
                path = app.configPanel.getRootPath();
            }

            label = Zutubi.config.subPath(path, 0, 1);
            label = label.substring(0, label.length - 1);

            window = new Zutubi.admin.WizardWindow({
                path: path,
                label: label,
                success: function(delta)
                {
                    app.navbar.applyDelta(delta);
                    app.addedPath = delta.addedPaths[0];
                    app.router.navigate("/hierarchy/" + Zutubi.config.encodePath(app.addedPath), false);
                }
            });

            window.show();
        }

        function _createNavbar(options)
        {
            var navbar = $("#navbar").kendoZaNavbar(options).data("kendoZaNavbar");
            navbar.bind("scope-selected", function(e)
            {
                var path;
                if (e.scope === "projects" || e.scope === "agents")
                {
                    path = e.scope;
                    if (app.configPanel && app.configPanel.getRootPath().indexOf(path) === 0)
                    {
                        path = app.configPanel.getRootPath();
                    }

                    Zutubi.admin.openHierarchyPath(path);
                }
                else if (e.scope === "plugins")
                {
                    app.router.navigate("/plugins");
                }
                else
                {
                    app.router.navigate("/config/" + e.scope);
                }
            });

            navbar.bind("item-selected", function(e)
            {
                var rootPath = app.configPanel.getRootPath(),
                    configPath = app.configPanel.getConfigPath();

                rootPath = Zutubi.config.subPath(rootPath, 0, 1) + "/" + e.name;
                app.router.navigate(Zutubi.config.encodePath("/config/" + rootPath + "/" + configPath), true);
                // Lazy setPaths will take care of choosing the longest valid config path.
                app.configPanel.setPaths(rootPath, configPath, true);
            });

            navbar.bind("add", function(e)
            {
                if (e.scope === "plugins")
                {
                    app.pluginsPanel.showInstallWindow();
                }
                else
                {
                    _showAddWizard(e.scope);
                }
            });

            return navbar;
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
                "introduceParent": "caret-square-o-up",
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
                "smartClone": "magic",
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

            init: function(isAdmin, projectCreateAllowed, agentCreateAllowed)
            {
                app.isAdmin = isAdmin;
                app.notificationWidget = _createNotificationWidget();
                Zutubi.core.registerFeedbackHandler(app.notificationWidget);
                app.router = _createRouter();
                app.navbar = _createNavbar({
                    isAdmin: isAdmin,
                    projectCreateAllowed: projectCreateAllowed,
                    agentCreateAllowed: agentCreateAllowed
                });
            },

            start: function()
            {
                app.router.start();
            },

            openHierarchyPath: function(newPath)
            {
                app.router.navigate("/hierarchy/" + Zutubi.config.encodePath(newPath), false);
            },

            openConfigPath: function(newPath)
            {
                app.router.navigate("/config/" + Zutubi.config.encodePath(newPath), false);
            },

            replaceConfigPath: function(newPath)
            {
                app.router.replace("/config/" + Zutubi.config.encodePath(newPath), true);
            },

            hasCollapsedCollection: function(data)
            {
                // Simple (<10 field) composite with a single nested collection will be collapsed.
                return data.kind === "composite" &&
                    data.nested && data.nested.length === 1 && data.nested[0].kind === "collection" &&
                    (!data.type.simpleProperties || data.type.simpleProperties.length < 10);
            },

            labelCompare: function(a1, a2)
            {
                return a1.label.localeCompare(a2.label);
            },

            lastAddedPath: function()
            {
                // Note this only captures added projects/agents.
                return app.addedPath;
            },

            clearLastAddedPath: function()
            {
                delete app.addedPath;
            }
        };
    }(jQuery));
}
