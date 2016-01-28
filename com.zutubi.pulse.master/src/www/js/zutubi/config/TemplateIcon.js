// dependency: ./namespace.js

(function($)
{
    var ui = kendo.ui,
        Widget = ui.Widget,
        ns = ".kendoZaTemplateIcon",
        CLICK = "click" + ns,
        MOUSEENTER = "mouseenter" + ns,
        MOUSELEAVE = "mouseleave" + ns,
        SELECT = "select",
        DATA_ITEM = "item" + ns,
        CLASS_ACTIVE = "k-template-active";

    Zutubi.config.TemplateIcon = Widget.extend({
        init: function(element, options)
        {
            var that = this;

            that.anchorEl = $('<a class="k-template-icon"><span class="k-sprite"></span></a>');
            if (options.spriteCssClass)
            {
                that.anchorEl.find(".k-sprite").addClass(options.spriteCssClass);
            }

            $(element).append(that.anchorEl);
            that.anchorEl.on(MOUSEENTER, jQuery.proxy(that._onMouseEnter, that));
            that.anchorEl.on(MOUSELEAVE, jQuery.proxy(that._onMouseLeave, that));
            that.timeoutId = -1;

            Widget.fn.init.call(this, element, options);
        },

        options: {
            name: "ZaTemplateIcon"
        },

        events: [
            SELECT
        ],

        destroy: function()
        {
            this.anchorEl.off(ns);
            if (this.popupEl)
            {
                this.popupEl.off(ns);
            }

            Widget.fn.destroy.call(this);
        },

        _clearTimeout: function()
        {
            if (this.timeoutId !== -1)
            {
                clearTimeout(this.timeoutId);
                this.timeoutId = -1;
            }
        },

        _ensurePopup: function()
        {
            var that = this,
                items = that.options.items,
                i,
                itemEl;

            if (!that.popup)
            {
                that.popupEl = $('<ul class="k-template-popup k-selector-popup"></ul>');
                for (i = 0; i < items.length; i++)
                {
                    itemEl = $('<li>' + kendo.htmlEncode(items[i].text) + '</li>');
                    itemEl.data(DATA_ITEM, items[i]);
                    that.popupEl.append(itemEl);
                }

                that.popup = that.popupEl.kendoPopup({
                    anchor: that.anchorEl,
                    origin: "bottom left",
                    position: "top left",
                    animation: {
                        open: {
                            effects: "slideIn:down"
                        }
                    }
                }).data("kendoPopup");

                that.popopHover = false;
                that.popupEl.on(MOUSEENTER, jQuery.proxy(that._onMouseEnterPopup, that));
                that.popupEl.on(MOUSELEAVE, jQuery.proxy(that._onMouseLeavePopup, that));
                that.popupEl.on(CLICK, jQuery.proxy(that._onPopupClicked, that));
            }
        },

        _onMouseEnter: function()
        {
            this._clearTimeout();
            this._ensurePopup();
            this.popup.open();
            this.anchorEl.addClass(CLASS_ACTIVE);
        },

        _onMouseLeave: function()
        {
            this._clearTimeout();
            this.timeoutId = setTimeout(jQuery.proxy(this._delayedMouseLeave, this), 300);
        },

        _delayedMouseLeave: function()
        {
            this.timeoutId = -1;
            if (!this.popopHover)
            {
                this.anchorEl.removeClass(CLASS_ACTIVE);
                this.popup.close();
            }
        },

        _onMouseEnterPopup: function()
        {
            this._clearTimeout();
            this.popopHover = true;
        },

        _onMouseLeavePopup: function()
        {
            this._clearTimeout();
            this.popopHover = false;
            this.anchorEl.removeClass(CLASS_ACTIVE);
            this.popup.close();
        },

        _onPopupClicked: function(e)
        {
            var that = this,
                target = kendo.eventTarget(e),
                itemEl = $(target).closest("li"),
                data;

            e.preventDefault();

            if (itemEl)
            {
                data = itemEl.data(DATA_ITEM);
                if (data)
                {
                    this.anchorEl.removeClass(CLASS_ACTIVE);
                    that.popup.close();
                    that.trigger(SELECT, {item: data});
                }
            }
        }
    });

    ui.plugin(Zutubi.config.TemplateIcon);
}(jQuery));
