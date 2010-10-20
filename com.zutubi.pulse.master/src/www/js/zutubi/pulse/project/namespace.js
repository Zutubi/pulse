// dependency: zutubi/pulse/namespace.js

window.Zutubi.pulse.project = window.Zutubi.pulse.project || {
    imageLabel: function(type, value) {
        return '<img alt="' + value + '" src="' + window.baseUrl + '/images/' + type + '/' + value.replace(' ', '').toLowerCase() + '.gif"/> ' + value;    
    },
    
    cells: {
        PAUSE_TEMPLATE: new Ext.Template(
            '<a class="unadorned" href="{baseUrl}/projectState.action?projectName={encodedProjectName}&amp;pause={pause}">' +
                '<img style="vertical-align: top" src="{baseUrl}/images/{icon}.gif"/>' +
                '{label}' +
            '</a>'
        ),
        
        health: function(value) {
            return Zutubi.pulse.project.imageLabel('health', value);
        },
        
        status: function(value) {
            return Zutubi.pulse.project.imageLabel('status', value);
        },
        
        projectState: function(value) {
            var result = value.pretty;
            if (value.canPause)
            {
                result += ' ' + PAUSE_TEMPLATE.apply({
                    encodedProjectName: encodeURIComponent(context.projectName),
                    pause: true,
                    label: 'pause',
                    icon: 'control_pause_blue'
                });
            }
            else if (value.canResume)
            {
                result += ' ' + PAUSE_TEMPLATE.apply({
                    encodedProjectName: encodeURIComponent(context.projectName),
                    pause: false,
                    label: 'resume',
                    icon: 'control_play_blue'
                });
            }
            return  
        }
    }
};
