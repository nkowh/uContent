Ext.define('explorer.view.main.DocumentController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.document',
    detail : function(){
        var me = this;
        var records = this.getView().getSelectionModel().getSelection();
        if (records && records.length > 0) {
            if(records.length > 1 ){
                Ext.Msg.alert('message', 'Can only choose one item.');
                return;
            }
            var record = records[0];
            Ext.create('Ext.window.Window', {
                layout: 'fit',
                title: '修改文档',
                width : 1000,
                height : 600,
                items: [{
                    xtype: 'modifydocument',
                    docData : record
                }]
            }).show();
        } else {
            Ext.Msg.alert('message', 'Please select one item at least.');
            return;
        }
    },
    deleteDoc : function(){
        var me = this;
        var records = this.getView().getSelectionModel().getSelection();
        if (records && records.length > 0) {
            var result = [];
            Ext.Msg.confirm("Title", "Are you sure to delete this Document？", function (r) {
                if(r=='no') return ;
                Ext.Array.each(records, function(record, index, countriesItSelf) {
                    result.push({"type":record.get("_type"),"id":record.get("_id")});
                });
                Ext.Ajax.request({
                    method: 'DELETE',
                    url: '/svc',
                    headers: {'Content-Type': 'application/json;charset=utf-8'},
                    params: Ext.JSON.encode(result),
                    callback: function (options, success, response) {
                        if (!success) {
                            var error = Ext.decode(response.responseText);
                            Ext.toast({
                                html: 'Delete Error!<br />'+error.status+':'+error.reason,
                                title: 'message',
                                width: 200,
                                align: 't'
                            });

                            return;
                        }
                        Ext.toast({
                            html: 'Delete successful',
                            title: 'message',
                            width: 200,
                            align: 't'
                        });
                        me.loadData();
                    }
                });
            });

        } else {
            Ext.Msg.alert('message', 'Please select one item at least.');
            return;
        }
    },

    loadData : function(e, eOpts ){
        var me = this;
        var query = this.getView().docQuery;
        var params = {};
        if(this.getView().limit&&this.getView().limit!=''){

            params ={
                highlight:true,
                query:query,
                limit: this.getView().limit
            };
        }else{
            params ={
                highlight:false,
                query:query,
                types: this.getView().qType
            };
        }
        var store =  Ext.create('explorer.store.Documents');
        store.getProxy().extraParams =params;
        e.bindStore(store);
        e.down('pagingtoolbar').bindStore(store);
        store.load();

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
