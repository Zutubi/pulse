// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoZaNavbar",
        CLICK = "click" + ns,
        SELECT = "select";

    // A basic item that can be clicked to navigate (to either a URL, or via a click callback).
    //
    // options:
    //   - model: argument for the template (required unless create() is overridden in a subclass)
    //     - click: if the model has a click property, this will be invoked when the item is clicked
    //   - template: override the default template (the default creates a link with a url and content)
    Zutubi.core.NavbarItem = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.template = kendo.template(that.options.template);
            that.create();
        },

        options: {
            name: "ZaNavbarItem",
            template: '<a href="#= url #">#= content #</a>'
        },

        create: function()
        {
            var model = this.options.model;

            this.innerElement = $(this.template(model));
            this.element.append(this.innerElement);
            if (typeof model.click !== "undefined")
            {
                this.innerElement.on(CLICK, jQuery.proxy(model.click, this));
            }
        },

        destroy: function()
        {
            this.innerElement.off(ns);
            Widget.fn.destroy.call(this);
        },

        hide: function()
        {
            this.element.hide();
        },

        show: function()
        {
            this.element.show();
        },

        applyDelta: function(delta)
        {
            // Default does nothing.
        }
    });

    // An item that presents a dropdown menu of textual items.  Items can be either plain links or raise select events
    // on click.  The content of the item can be set in the original model and/or updated to the text of the selected
    // item.
    //
    // options:
    //   - items: array of item text (will be escaped when creating the menu)
    //   - urls: optional array of urls corresponding to items (if not provided a select event is raised on item click)
    //   - model: model passed to template, may define content (HTML) to populate the item
    //   - updateOnSelect: if true, the content of the item is updated to the text of the selected item on select
    //   - itemTemplate: template used to create menu items (when no urls are specified)
    //   - urlItemTemplate: template used to create menu items (when urls are specified)
    Zutubi.core.MenuNavbarItem = Zutubi.core.NavbarItem.extend({
        options: {
            name: "ZaMenuNavbarItem",
            template: '<div class="k-split-container"><a class="k-split-button">#= content #</a><a class="k-split-button-arrow"><span class="fa fa-caret-down"></span></a></div>',
            model: {
                content: ''
            },
            updateOnSelect: true,
            itemTemplate: '<li><span class="k-selector-popup-item">#: text #</span></li>',
            urlItemTemplate: '<li><a class="k-selector-popup-item" href="#= url #">#: text #</a></li>'
        },

        events: [
            SELECT
        ],

        create: function()
        {
            var that = this;

            that.itemTemplate = kendo.template(that.options.itemTemplate);
            that.urlItemTemplate = kendo.template(that.options.urlItemTemplate);

            Zutubi.core.NavbarItem.fn.create.call(that);

            that.mainButton = that.innerElement.find(".k-split-button");
            that.mainButton.on(CLICK, jQuery.proxy(that._clicked, that));
            that.arrowButton = that.innerElement.find(".k-split-button-arrow");
            that.arrowButton.on(CLICK, jQuery.proxy(that._clicked, that));

            that.popup = that.createPopup();
        },

        destroy: function()
        {
            this.mainButton.off(ns);
            this.arrowButton.off(ns);
            if (this.popupEl)
            {
                this.popupEl.off(ns);
            }
            Zutubi.core.NavbarItem.fn.destroy.call(this);
        },

        createPopup: function()
        {
            var that = this,
                i,
                items = that.options.items,
                urls = that.options.urls,
                template,
                url,
                anchor,
                side,
                popup;

            that.popupEl = $("<ul class='k-selector-popup'></ul>");
            for (i = 0; i < items.length; i++)
            {
                url = urls ? urls[i] : '';
                template = url ? that.urlItemTemplate : that.itemTemplate;
                that.popupEl.append(template({
                    text: items[i],
                    url: url
                }));
            }

            anchor = that.element.closest(".k-navitem");
            side = anchor.hasClass("k-navright") ? "right" : "left";
            popup = that.popupEl.kendoPopup({
                anchor: anchor,
                origin: "bottom " + side,
                position: "top " + side
            }).data("kendoPopup");

            if (!urls)
            {
                that.popupEl.on(CLICK, jQuery.proxy(that._itemClicked, that));
            }

            return popup;
        },

        select: function(text)
        {
            this.selected = text;
            if (this.options.updateOnSelect)
            {
                this.mainButton.text(text);
            }
        },

        _clicked: function(e)
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
        },

        _itemClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                item = $(target).closest("li");

            e.preventDefault();

            if (item)
            {
                that.popup.close();
                that.select(item.text());
                that.trigger(SELECT, {scope: that.selected});
            }
        }
    });

    // An item that displays a button with icon and text that raises a click event when pressed.
    //
    // options:
    //   - model: required model passed to the template, should define spriteCssClass and title for the default template
    //   - template: can be set to override the default template
    Zutubi.core.ButtonNavbarItem = Zutubi.core.NavbarItem.extend({
        options: {
            name: "ZaButtonNavbarItem",
            template: '<button class="k-primary"><span class="#= spriteCssClass #"></span> <span class="k-button-navitem-title">#: title #</span></button>'
        },

        events: [
            "click"
        ],

        create: function()
        {
            var that = this;

            Zutubi.core.NavbarItem.fn.create.call(that);

            that.button = that.innerElement.kendoButton().data("kendoButton");
            that.button.bind("click", function(e) { that.trigger("click", e); });
        },

        setTitle: function(title)
        {
            this.element.find(".k-button-navitem-title").html(kendo.htmlEncode(title));
        }
    });

    // A bar for top-level navigation of the UI, which sits right at the top of the screen and has some fixed items at
    // both left and right ends, plus additional items between based on context.
    //
    // options:
    //   - userName: name of the logged in user (unset if the user is a guest)
    //   - userCanLogout: set to true if the current user may logout
    //   - itemTemplate: template used to make elements on which items are instantiated (passed cls)
    //   - extraItems: optional array of extra item configurations, each of which may contain:
    //       - type: the item widget name (e.g. ZaMenuNavbarItem)
    //       - cls: cls to pass to itemTemplate when making the element for the item
    //       - options: passed to the item initialiser
    Zutubi.core.Navbar = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            Widget.fn.init.call(this, element, options);

            that.create();

            if (options.section)
            {
                that.setSection(options.section);
            }
        },

        options: {
            name: "ZaNavbar",
            itemTemplate: '<span class="k-navitem #: cls #"></span>'
        },

        create: function()
        {
            var that = this,
                sectionItems = [],
                sectionUrls = [],
                userItems = [],
                userUrls = [];

            that.itemTemplate = kendo.template(that.options.itemTemplate);

            if (that.options.userName)
            {
                sectionItems.push("dashboard", "my builds");
                sectionUrls.push("/dashboard/", "/dashboard/my/");

                userItems.push(that.options.userName);
                userUrls.push("");
                userItems.push("preferences");
                userUrls.push("/dashboard/preferences/");
                if (that.options.userCanLogout)
                {
                    userItems.push("logout");
                    userUrls.push("/logout")
                }
            }
            else
            {
                userItems.push("Guest");
                userUrls.push("");
                userItems.push("login");
                userUrls.push("/login!input.action");
            }

            sectionItems.push("projects", "server", "agents", "administration");
            sectionUrls.push("/browse/", "/server/", "/agents/", "/admin/");

            that.list = $('<div class="k-navlist"></div>');
            that.element.append(that.list);

            that.homeItem = that._addSimpleItem({
                url: window.baseUrl + '/',
                content: '<span class="fa fa-heartbeat"></span>'
            });


            that.sectionItem = that._addItemElement().kendoZaMenuNavbarItem({
                items: sectionItems,
                urls: sectionUrls
            }).data("kendoZaMenuNavbarItem");

            that._createExtraItems();

            that.userItem = that._addItemElement("k-navright").kendoZaMenuNavbarItem({
                model: {
                    content: '<span class="fa fa-user"></span>'
                },
                items: userItems,
                urls: userUrls
            }).data("kendoZaMenuNavbarItem");

            that.helpItem = that._addSimpleItem({
                url: "#",
                click: function(e)
                {
                    var popup;

                    e.preventDefault();

                    popup = window.open(window.baseUrl + '/popups/reference.action', '_pulsereference', 'status=yes,resizable=yes,top=100,left=100,width=900,height=600,scrollbars=yes');
                    popup.focus();
                },
                content: '<span class="fa fa-question-circle"></span>'
            });
        },

        _addItemElement: function(cls)
        {
            return $(this.itemTemplate({cls: cls || ''})).appendTo(this.list);
        },

        _addSimpleItem: function(model, cls)
        {
            var el = this._addItemElement(cls);
            el.kendoZaNavbarItem({model : model}).data("kendoZaNavbarItem");
        },

        _createExtraItems: function()
        {
            var that = this,
                items = this.options.extraItems,
                i;

            this.extraItems = [];
            if (items)
            {
                for (i = 0; i < items.length; i++)
                {
                    that.addExtraItem(items[i]);
                }
            }
        },

        addExtraItem: function(item)
        {
            var el,
                type,
                result;

            el = $(this.itemTemplate({cls: item.cls || ''}));
            el.insertBefore(this.list.find(".k-navright"));
            type = item.type || "kendoZaNavbarItem";
            result = el[type](item.options || {}).data(type);
            this.extraItems.push(result);

            return result;
        },

        setSection: function(section)
        {
            this.sectionItem.select(section);
        },

        applyDelta: function(delta)
        {
            var i;
            for (i = 0; i < this.extraItems.length; i++)
            {
                this.extraItems[i].applyDelta(delta);
            }
        }
    });

    ui.plugin(Zutubi.core.NavbarItem);
    ui.plugin(Zutubi.core.MenuNavbarItem);
    ui.plugin(Zutubi.core.ButtonNavbarItem);
    ui.plugin(Zutubi.core.Navbar);
}(jQuery));
