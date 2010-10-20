// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A box showing information about the responsible user for a project (if any).
 */
Zutubi.pulse.project.ResponsibilityBox = Ext.extend(Ext.BoxComponent, {
    template: new Ext.XTemplate(
        '<div class="note" id="responsible-panel">' +
            '<img alt="fixing" src="{baseUrl}/images/config/actions/takeResponsibility.gif" style="padding: 0 6px 0 6px"/> ' +
            '<span id="responsible-message"></span> ' +
            '<span id="responsible-clear-container">' +
                '[<a href="#" id="responsible-clear" onclick="clearResponsibility(\'{projectId}\'); return false">clear</a>]' +
            '</span>' +
            '<span id="responsible-comment-container">' +
                ' : <img alt="comment" src="{baseUrl}/images/comment.gif" style="padding: 0 6px 0 6px"/> <span id="responsible-comment" class="comment"></span>' +
            '</span>' +
        '</div>'),
    
    onRender: function(container, position) {
        var args = this.getTemplateArgs();
        if (position)
        {
            this.el = this.template.insertBefore(position, args, true);    
        }
        else
        {
            this.el = this.template.append(container, args, true);
        }
        
        this.messageEl = Ext.get('responsible-message');
        this.clearContainerEl = Ext.get('responsible-clear-container');
        this.clearEl = Ext.get('responsible-clear');
        this.commentContainerEl = Ext.get('responsible-comment-container');
        this.commentEl = Ext.get('responsible-comment');
        
        this.update(this.data);
        
        Zutubi.pulse.project.ResponsibilityBox.superclass.onRender.apply(this, arguments);
    },
    
    update: function(data) {
        this.data = data;
        
        if (data)
        {
            this.messageEl.update(Ext.util.Format.htmlEncode(data.owner));
            this.clearContainerEl.setDisplayed(data.canClear);
            if (data.comment)
            {
                this.commentEl.update(Ext.util.Format.htmlEncode(data.comment));
                this.commentContainerEl.setDisplayed(true);
            }
            else
            {
                this.commentEl.update('');
                this.commentContainerEl.setDisplayed(false);
            }
            
            this.el.setDisplayed(true);
        }
        else
        {
            this.el.setDisplayed(false);
        }
    },
    
    getTemplateArgs: function() {
        return {
            baseUrl: window.baseUrl,
            projectId: this.projectId
        }
    }
});

Ext.reg('xzresponsibilitybox', Zutubi.pulse.project.ResponsibilityBox);