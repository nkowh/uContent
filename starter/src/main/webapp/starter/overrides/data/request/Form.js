Ext.define('starter.overrides.Form', {
    override: 'Ext.data.request.Form',

    getDoc: function () {
        var frame = this.frame.dom;

        return (frame && (frame.contentWindow.document || frame.contentDocument)) ||
            (window.frames[frame.id] || {}).document;
    }
});