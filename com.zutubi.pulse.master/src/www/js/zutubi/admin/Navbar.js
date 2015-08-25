// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        SELECT = "select",
        SCOPE_SELECTED = "scope-selected";

    Zutubi.admin.ScopeSelector = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaScopeSelector"
        },

        events: [
            SELECT
        ],

        _create: function()
        {
            var that = this;

            that.selected = "projects";

            that.outer = $('<div class="k-split-container"></div>');
            that.mainButton = $('<a class="k-split-button">projects</a>');
            that.outer.append(that.mainButton);
            that.arrowButton = $('<a class="k-split-button-arrow"><span class="k-icon k-i-arrow-s"></span></a>');
            that.outer.append(that.arrowButton);

            that.element.append(that.outer);

            that.popupEl = $("<ul class='selector-popup'><li>projects</li><li>agents</li><li>settings</li><li>users</li><li>groups</li><li>plugins</li></ul>");
            that.popup = that.popupEl.kendoPopup({
                anchor: that.element,
                origin: "top left",
                position: "bottom left",
                animation: {
                    open: {
                        effects: "slideIn:up"
                    }
                }
            }).data("kendoPopup");

            that.popupEl.on("click", jQuery.proxy(that._popupClicked, that));
            that.mainButton.on("click", jQuery.proxy(that._mainClicked, that));
            that.arrowButton.on("click", jQuery.proxy(that._arrowClicked, that));
        },

        selectScope: function(scope)
        {
            this.selected = scope;
            this.mainButton.text(scope);
        },

        _popupClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                item = $(target).closest("li");

            e.preventDefault();

            if (item)
            {
                that.popup.close();
                that.selectScope(item.text());
                that.trigger(SELECT, {scope: that.selected});
            }
        },

        _mainClicked: function(e)
        {
            e.preventDefault();
            this.trigger(SELECT, {scope: this.selected});
        },

        _arrowClicked: function(e)
        {
            var that = this;

            e.preventDefault();

            if (that.popup.visible())
            {
                that.popup.close();
            }
            else
            {
                that.popup.open();
            }
        }
    });

    Zutubi.admin.Navbar = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that._create();
        },

        options: {
            name: "ZaNavbar"
        },

        events: [
            SCOPE_SELECTED
        ],

        _create: function()
        {
            var that = this;

            that.list = $('<ul class="navlist"></ul>');
            that.element.append(that.list);
            that.pulseLink = $('<li><a href="' + window.baseUrl + '/admina/">:: pulse admin ::</a></li>');
            that.list.append(that.pulseLink);

            that.scopeSelectorItem = $('<li></li>');
            that.scopeSelector = that.scopeSelectorItem.kendoZaScopeSelector({}).data("kendoZaScopeSelector");
            that.scopeSelector.bind(SELECT, function(e)
            {
                that.trigger(SCOPE_SELECTED, {scope: e.scope});
            });

            that.list.append(that.scopeSelectorItem);
        },

        selectScope: function(scope)
        {
            this.scopeSelector.selectScope(scope);
        }
    });

    ui.plugin(Zutubi.admin.ScopeSelector);
    ui.plugin(Zutubi.admin.Navbar);
}(jQuery));
