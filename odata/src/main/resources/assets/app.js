Ext.setGlyphFontFamily('FontAwesome');
Ext.Ajax.cors = true;

Ext.application({
    name: 'dm',

    launch: function () {
        document.title = "uContent"
        Ext.onReady(function () {
            Ext.create('dm.Viewport');
        });
    }


});