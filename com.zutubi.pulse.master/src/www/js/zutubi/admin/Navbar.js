// dependency: ./namespace.js
// dependency: zutubi/config/package.js
// dependency: ./HierarchySelector.js

(function($)
{
    var ui = kendo.ui,
        MenuNavbarItem = Zutubi.core.MenuNavbarItem,
        Navbar = Zutubi.core.Navbar,
        ADD = "add",
        SELECT = "select",
        SCOPE_SELECTED = "scope-selected",
        ITEM_SELECTED = "item-selected";


    Zutubi.admin.ScopeCrumb = MenuNavbarItem.extend({
        init: function(element, options)
        {
            options.items = ["projects", "agents"];
            if (options.isAdmin)
            {
                options.items.push("settings", "users", "groups", "plugins");
            }

            MenuNavbarItem.fn.init.call(this, element, options);

            this.select("projects");
        },

        options: {
            name: "ZaScopeCrumb"
        }
    });

    Zutubi.admin.HierarchyCrumb = MenuNavbarItem.extend({
        options: {
            name: "ZaHierarchyCrumb"
        },

        hasPopup: function()
        {
            return true;
        },

        createPopup: function()
        {
            var that = this,
                popup;

            that.popupEl = $("<div class='k-hierarchy-popup'></div>");
            that.selector = that.popupEl.kendoZaHierarchySelector().data("kendoZaHierarchySelector");
            that.selector.bind("nodeselect", function(e)
            {
                that.select(e.name);
                that.popup.close();
                that.trigger(SELECT, {name: e.name});
            });

            popup = that.popupEl.kendoPopup({
                anchor: that.element.closest(".k-navitem"),
                origin: "bottom left",
                position: "top left"
            }).data("kendoPopup");

            return popup;
        },

        select: function(text)
        {
            MenuNavbarItem.fn.select.call(this, text);
            this.selector.selectItem(text);
        },

        setScope: function(scope)
        {
            this.selector.setScope(scope);
        },

        applyDelta: function(delta)
        {
            var ourPath = this.selector.getScope() + "/" + this.selected,
                newPath;

            this.selector.applyDelta(delta);
            if (delta.renamedPaths)
            {
                newPath = delta.renamedPaths[ourPath];
                if (newPath)
                {
                    this.select(Zutubi.config.subPath(newPath, 1, 2));
                }
            }
        }
    });

    Zutubi.admin.Navbar = Navbar.extend({
        init: function(element, options)
        {
            var that = this;

            that.isAdmin = options.isAdmin;
            that.createAllowed = [];
            if (options.projectCreateAllowed)
            {
                that.createAllowed.push("projects");
            }
            if (options.agentCreateAllowed)
            {
                that.createAllowed.push("agents");
            }

            Navbar.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaAdminNavbar",
            expandSections: false
        },

        events: [
            SCOPE_SELECTED,
            ITEM_SELECTED
        ],

        create: function()
        {
            var that = this;

            Navbar.fn.create.call(this);

            that.scopeCrumb = that.addExtraItem({
                type: "kendoZaScopeCrumb",
                options: {
                    isAdmin: that.isAdmin
                }
            });
            that.scopeCrumb.bind(SELECT, function(e)
            {
                that._updateAddButton();
                that.trigger(SCOPE_SELECTED, {scope: e.scope});
            });

            that.hierarchyCrumb = that.addExtraItem({
                type: "kendoZaHierarchyCrumb"
            });
            that.hierarchyCrumb.bind(SELECT, function(e)
            {
                that.trigger(ITEM_SELECTED, {name: e.name});
            });

            that.addButton = that.addExtraItem({
                type: "kendoZaButtonNavbarItem",
                options: {
                    model: {
                        spriteCssClass: "fa fa-plus-circle",
                        title: ""
                    }
                }
            });
            that.addButton.bind("click", jQuery.proxy(that._addClicked, that));
        },

        selectScope: function(scope, hierarchyPath)
        {
            this.scopeCrumb.select(scope);
            if (typeof hierarchyPath === "undefined")
            {
                this.hierarchyCrumb.hide();
            }
            else
            {
                this.hierarchyCrumb.setScope(scope);
                this.hierarchyCrumb.select(Zutubi.config.subPath(hierarchyPath, 0, 1));
                this.hierarchyCrumb.show();
            }

            this._updateAddButton(scope);
        },

        _updateAddButton: function()
        {
            var that = this,
                scope = that.scopeCrumb.selected,
                title;

            if (that.createAllowed.indexOf(scope) >= 0 || (scope === "plugins" && that.isAdmin))
            {
                if (scope === "plugins")
                {
                    title = 'install plugin';
                }
                else
                {
                    title = 'add new ' + scope.substring(0, scope.length - 1);
                }

                that.addButton.setTitle(title);
                that.addButton.show();
            }
            else
            {
                that.addButton.hide();
            }
        },

        _addClicked: function()
        {
            this.trigger(ADD, {scope: this.scopeCrumb.selected});
        }
    });

    ui.plugin(Zutubi.admin.ScopeCrumb);
    ui.plugin(Zutubi.admin.HierarchyCrumb);
    ui.plugin(Zutubi.admin.Navbar);
}(jQuery));
