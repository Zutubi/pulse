// dependency: ./namespace.js
// dependency: ext/package.js

/**
 * A box showing a nested list of features.  Expects data of the form:
 *
 * {
 *     features: FeatureModel[],
 *     stages: [{
 *         name: 'my-stage',
 *         recipeName: 'default',  (may not be defined)
 *         agentName: 'linux 64',  (may not be defined)
 *         isComplete: true,
 *         features: FeatureModel[],
 *         commands: [{
 *             name: 'build',
 *             features: FeatureModel[],
 *             artifacts: [{
 *                 name: 'test-reports',
 *                 files: [{
 *                     path: 'path/to/file.txt',
 *                     featureCount: 130,  (may be more than features included)
 *                     features: FeatureModel[],
 *                 }, ... ]
 *             }, ... ]
 *         }, ... ]
 *     }, ... ]
 * }
 *
 * @cfg {String} id    Id to use for this component.
 * @cfg {String} level The name of the feature level (e.g. 'error').
 */
Zutubi.pulse.project.FeatureList = Ext.extend(Ext.BoxComponent, {
    template: new Ext.XTemplate(
        '<table id="features-${level.prettyString}" class="content-table">' +
            '<tr><th class="two-heading">{level} features</th></tr>' +
            '<tr>' +    
                '<td class="leftmost rightmost">' +
                    '<ul>' +
                    '</ul>' +
                '</td>' +
            '</tr>' +
        '</table>'
    ),

    featureTemplate: new Ext.XTemplate(
        '<li class="{level}">' +
            '{summary:htmlEncode:nl2br}' +
        '</li>'
    ),
    
    onRender: function(container, position) {
        if (position)
        {
            this.el = this.template.insertBefore(position, this, true);    
        }
        else
        {
            this.el = this.template.append(container, this, true);
        }
        
        this.listEl = this.el.child('ul');
        this.renderFeatures();
        
        Zutubi.pulse.project.FeatureList.superclass.onRender.apply(this, arguments);
    },

    update: function(data)
    {
        this.data = data;
        
        if (this.rendered)
        {
            this.el.select('li').remove();
            this.renderFeatures();
        }
    },
    
    renderFeatures: function()
    {
        if (this.data)
        {
            var html = '';
            html += this.simpleFeatures(this.data.features);
            var stages = this.data.stages;
            for (var i = 0, l = stages.length; i < l; i++)
            {
                html += this.stageFeatures(stages[i]);
            }
            
            this.listEl.update(html);
            this.el.setDisplayed(true);
        }
        else
        {
            this.el.setDisplayed(false);
        }
    },
    
    simpleFeatures: function(features)
    {
        var html = '';
        if (features)
        {
            for (var i = 0, l = features.length; i < l; i++)
            {
                html += this.featureTemplate.apply({level: this.level, summary: features[i].summary});
            }
        }
        
        return html;
    },
    
    stageFeatures: function(stage)
    {
        var recipe = stage.recipeName ? Ext.util.Format.htmlEncode(stage.recipeName) : '[default]';
        var agent = stage.agentName ? Ext.util.Format.htmlEncode(stage.agentName) : '[pending]'
        var html = '<li class="header">build stage :: ' + Ext.util.Format.htmlEncode(stage.name) + ' :: ' + recipe + '@' + agent + '<ul>';
        html += this.simpleFeatures(stage.features);
        
        for (var i = 0, l = stage.commands.length; i < l; i++)
        {
            html += this.commandFeatures(stage.commands[i], stage.isComplete);
        }
        
        html += '</ul></li>';
        return html;
    },
    
    commandFeatures: function(command, complete)
    {
        var html = '<li class="header">command :: ' + Ext.util.Format.htmlEncode(command.name) + '<ul>';
        html += this.simpleFeatures(command.features);
        
        for (var i = 0, l = command.artifacts.length; i < l; i++)
        {
            html += this.artifactFeatures(command.artifacts[i], command.artifactsUrl, complete);
        }
        
        html += '</ul></li>';
        return html;
    },
    
    artifactFeatures: function(artifact, artifactsUrl, complete)
    {
        var html = '';
        for (var i = 0, l = artifact.files.length; i < l; i++)
        {
            html += this.fileFeatures(artifact.files[i], artifactsUrl, complete);
        }
        
        return html;
    },
    
    fileFeatures: function(file, artifactsUrl, complete)
    {
        var fileUrl = artifactsUrl + this.pathToUrl(file.path);
        var html = '<li class="header">artifact :: ' + Ext.util.Format.htmlEncode(file.path) + '<ul>';
        for (var i = 0, l = file.features.length; i < l; i++)
        {
            html += '<li class="' + this.level + '">';
            if (feature.summaryLines)
            {
                html += '<span class="context">';
                var lines = faeture.summaryLines;
                for (var lineIndex = 0, lineCount = lines.length; lineIndex < lineLength; lineIndex++)
                {
                    var line = lines[lineIndex];
                    if (lineIndex == feature.lineOffset)
                    {
                        html += '</span><span class="feature">';
                    }
                    
                    html += Ext.util.Format.htmlEncode(line) + '<br/>';
                    
                    if (lineIndex == feature.lineOffset)
                    {
                        html += '</span><span class="context">';
                    }
                }
                
                html += '</span>';
            }
            else
            {
                html += Ext.util.Format.htmlEncode(feature.summary) + '<br/>';
            }
            
            if (complete)
            {
                html += '<a class="unadorned" href="' + fileUrl + '#' + feature.lineNumber;
                html += '<span class="small">jump to</span> <img src="' + window.baseUrl + '/images/go_small.gif"/></a>';
            }
            
            html += '</li>';
        }

        if (file.featuresTrimmed)
        {
            html += '<li>...[trimmed] To see the full list of features';
            html += '<a class="unadorned" href="' + fileUrl + '"><span class="small">jump to</span> ';
            html += '<img src="$base/images/go_small.gif"/></a></li>';
        }
        
        html += '</ul></li>';
        return html;
    }
});

Ext.reg('xzfeaturelist')