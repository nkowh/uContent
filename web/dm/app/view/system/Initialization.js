Ext.define('dm.view.system.Initialization', {
    extend: 'Ext.panel.Panel',

    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [
                {
                    xtype: 'button',
                    scale: 'large',
                    iconAlign: 'top',
                    text: '重建索引',
                    glyph: 0xf1c0,
                    handler: me.reindexing
                }
            ]

        });

        me.callParent();
    },



    reindexing: function () {
        Ext.ux.Ajax
            .put(Ext.util.Cookies.get('service').replace('/dm','/my_index_v1'))
            .then(Ext.ux.Ajax.put(Ext.util.Cookies.get('service').replace('/dm','/my_index_v1')), errorHandler)
            .then(Ext.ux.Ajax.request('data3.json'), errorHandler)
            .then(function (data3) {
            alert("Ah! That's much better ;)");
        }, errorHandler);
    }


});