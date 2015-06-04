Ext.define('dm.view.document.AdvSearch', {
    extend: 'Ext.panel.Panel',
    title: '高级搜索',
    layout: 'vbox',


    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            items: [Ext.create('dm.view.document.AdvCodition'), Ext.create('dm.grid.DocumentGrid')]
        });
        me.callParent();
    },

    search: function (query) {
        var me = this.up('panel').down('grid');
        var q = {
            size: 50,
            "query": query
        };

        me.search(q);
    }


});