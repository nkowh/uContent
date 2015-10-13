Ext.define('explorer.view.main.MainHeaderController', {
    extend: 'explorer.view.main.DocumentController',

    alias: 'controller.mainheader',
    fullText : function(bt,e){
        var me = this;
        var keytext = bt.up('toolbar').down('textfield').getValue().trim();
        var tabPanel = bt.up('app-header').up('app-main').down('tabpanel');
        var pageSize = this.getViewModel().get('pageSize');
        if(keytext&&keytext!=''){
            var index = tabPanel.items.length;
            tabPanel.add({
                title:this.getViewModel().get('fullTextTitle'),
                xtype:'fulltext',
                query: keytext,
                limit: pageSize,
                index : index
            });
            tabPanel.setActiveTab(index);

            Ext.Ajax.request({
                url: '/svc',
                method: 'GET',
                params: {limit: pageSize, query: keytext, highlight: true},
                callback: function (options, success, response) {
                    var result = Ext.JSON.decode(response.responseText);
                    console.log(result);
                    var fulltextStore = me.getViewModel().getStore('fulltext');
                    //var data = Ext.Array.map(result.documents, function (doc) {
                    //    return {
                    //        _id: doc._id,
                    //        _streams: doc._streams,
                    //        _acl :  doc._acl,
                    //        _type: doc._type,
                    //        createdBy: doc.createdBy,
                    //        createdOn: doc.createdOn,
                    //        lastUpdatedBy: doc.lastUpdatedBy,
                    //        lastUpdatedOn: doc.lastUpdatedOn,
                    //        name: doc.name,
                    //        score: doc._score,
                    //        highlight: doc._highlight
                    //    }
                    //});
                    fulltextStore.loadRawData(result.documents);
                }
            });
        }
    },
    advQuery : function(bt,e){
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '高级查询',
            width : 1000,
            height : 600,
            items: [{
                xtype: 'advancedsearch'
            }]
        }).show();
    },
    indexDoc : function(bt,e){
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建文档',
            width : 1000,
            height : 600,
            items: [{
                xtype: 'indexdocument'
            }]
        }).show();
    }
});
