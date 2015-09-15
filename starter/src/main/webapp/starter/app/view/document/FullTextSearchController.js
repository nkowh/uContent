Ext.define('starter.view.document.FullTextSearchController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.fulltextsearch',

    search: function () {
        var me = this;
        var keytext = me.getView().down('textfield[name=keytext]').getValue();
        var pageSize = me.getViewModel().get('pageSize');
        Ext.Ajax.request({
            url: '/svc',
            method: 'GET',
            params: {limit: pageSize, query: keytext, highlight: true},
            callback: function (options, success, response) {
                var result = Ext.JSON.decode(response.responseText);
                console.log(result);
                var fulltextStore = me.getView().getStore();
                var data = Ext.Array.map(result.documents, function (doc) {
                    return {
                        _id: doc._id,
                        _streams: doc._streams,
                        _type: doc._type,
                        createdBy: doc.createdBy,
                        createdOn: doc.createdOn,
                        lastUpdatedBy: doc.lastUpdatedBy,
                        lastUpdatedOn: doc.lastUpdatedOn,
                        name: doc.name,
                        score: doc._score,
                        highlight: doc._highlight
                    }
                });
                fulltextStore.loadRawData(data);
            }
        });
    },
    showImage: function (grid, record, item, index, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '图像浏览',
            maximized: true,
            items: [{
                xtype: 'imageexplorer',
                record: record
            }]
        }).show();
    }
});
