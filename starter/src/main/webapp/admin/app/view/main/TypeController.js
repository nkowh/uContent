Ext.define('admin.view.main.TypeController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.type',

    openCreateWin: function (sender, record) {
        var me = this;
        Ext.create('admin.view.main.type.Create').show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        var me = this;
        Ext.create('admin.view.main.type.Modify', {record: record}).show();
        return;
    },
    refreshType: function (sender, record) {
        var me = this;
        var store = this.getViewModel().getStore('types');
        store.load();
        return;
    },
    loadModifyData: function () {
        var me = this;
        var record = this.getView().record;
        this.getView().down('form').loadRecord(record);
        var type = record.get('name');
        Ext.Ajax.request({
            url: '/svc/types/' + type,
            callback: function (options, success, response) {
                if (!success) {
                    return;
                }
                if (response.responseText != '') {
                    var properties = Ext.decode(response.responseText);
                    me.getView().down('grid').bindStore(
                        Ext.create('Ext.data.Store', {
                            model: 'admin.model.Property',
                            data: properties.properties,
                            initData: properties.properties
                        }));

                }
            }
        });
    },
    deleteType: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        var name = record[0].get('name');
        if (record && record.length > 0) {
            Ext.Msg.confirm("Title", "Are you sure to delete this Type？", function (r) {
                if(r=='yes'){
                    me.getViewModel().getStore('types').remove(record[0]);
                }
            });

        } else {
            Ext.Msg.alert('message', 'Please select one item at least.');
            return;
        }
    },
    addProperty: function (e) {
        var store = e.up('grid').store;
        var order = store.data.length + 1;
        // Create a model instance
            var r = Ext.create('admin.model.Property', {
                name: '',
                type: 'string',
                pattern: '',
                promptMessage: '',
                index: 'not_analyzed',
                defaultValue: '',
                indexAnalyzer: '',
                searchAnalyzer: '',
                required: false,
                order: order
            });
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建属性',
            items: [{
                xtype: 'createProperty',
                fData : r,
                pStore : store
            }]
        }).show();
    },
    deleteProperty: function (e) {
        var grid = e.up('grid');
        var store = e.up('grid').store;
        var sm = grid.getSelectionModel();
        store.remove(sm.getSelection());
        if (store.getCount() > 0) {
            sm.select(0);
        }
    },
    deleteModifyProperty: function (e) {
        var grid = e.up('grid');
        var store = e.up('grid').store;
        var sm = grid.getSelectionModel();
        var record = sm.getSelection()[0];

        if (record) {
            var isOld = Ext.Array.contains(store.initData, record.data);
            if (isOld) {
                Ext.Msg.alert('message', 'You cannot delete the property.');
                return;
            } else
                store.remove(record);
        }
        if (store.getCount() > 0) {
            sm.select(0);
        }
    },

    createSave: function (e) {
        var me = this;
        var form = e.up('window').down('form');
        var grid = e.up('window').down('grid');
        var store = me.getViewModel().getStore('types');
        if (form.isValid()) {
            var type = form.getValues();
            var gstore = grid.store;
            var record = gstore.getAt(0);
            if (record && !record.isValid() && record.get('name') != '') {
                return;
            }
            var properties = [];
            var size = gstore.getCount();
            for (var i = 0; i < size; i++) {
                var pRecord = gstore.getAt(i);
                if (pRecord.get('type') == 'boolean') {
                    pRecord.set('index', 'not_analyzed');
                }
                if (pRecord.get('index') == 'not_analyzed') {
                    pRecord.set('indexAnalyzer', '');
                    pRecord.set('searchAnalyzer', '');
                }
                properties.push(pRecord.getData());
            }
            properties = Ext.Array.sort(properties, function (a, b) {
                if (a.order < b.order)
                    return -1;
                if (a.order > b.order)
                    return 1;
                if (a.order == b.order)
                    return 0;
            });
            type.properties = properties;
            //type.set('properties',properties);
            Ext.Ajax.request({
                method: 'POST',
                headers: {'Content-Type': 'application/json;charset=utf-8'},
                url: '/svc/types',
                params: Ext.JSON.encode(type),
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    Ext.toast({
                        html: 'Save successful',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                    me.getView().close();
                }
            });
        }

    },

    modifySave: function (e) {
        var me = this;
        var form = e.up('window').down('form');
        var grid = e.up('window').down('grid');
        var store = me.getViewModel().getStore('types');
        if (form.isValid()) {
            var type = form.getValues();
            var gstore = grid.store;
            var record = gstore.getAt(0);
            if (record && !record.isValid() && record.get('name') != '') {
                return;
            }
            var properties = []
            var size = gstore.getCount();
            for (var i = 0; i < size; i++) {
                var pRecord = gstore.getAt(i);
                properties.push(pRecord.getData());
            }
            properties = Ext.Array.sort(properties, function (a, b) {
                if (a.order < b.order)
                    return -1;
                if (a.order > b.order)
                    return 1;
                if (a.order == b.order)
                    return 0;
            });
            type.properties = properties;
            //type.set('properties',properties);
            Ext.Ajax.request({
                method: 'PUT',
                headers: {'Content-Type': 'application/json;charset=utf-8'},
                url: '/svc/types/' + type.name,
                params: Ext.JSON.encode(type),
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    Ext.toast({
                        html: 'Save successful',
                        title: 'message',
                        width: 200,
                        align: 't'
                    });
                    store.load();
                    me.getView().close();
                }
            });
        }

    },
    loadProperty : function(btn,e){
        var from = this.getView();
        var record = from.fData;
        var isUpdate = from.isUpdate;
        from.loadRecord( record );
        if(isUpdate){
            from.getForm().findField('name').disabled = true;
            from.getForm().findField('type').disabled = true;
            from.getForm().findField('index').disabled = true;
            from.getForm().findField('indexAnalyzer').disabled = true;
            from.getForm().findField('searchAnalyzer').disabled = true;
        }
    },
    saveProperty : function(e){
        var form = this.getView().getForm();
        if (form.isValid()) {
           var store =  this.getView().pStore;
            var record = this.getView().fData;
            var name = form.findField('name').getValue();
            var type = form.findField('type').getValue();
            var n = store.findBy(function(record ,id){
               if(record.get('name')==name&&type==record.get('type'))
               return true;
            });
            if(n>-1){
                record.set(form.getValues());
            }else{
                store.insert(0, form.getValues());
            }
            this.getView().up('window').close();
        }
    },
    modifyProperty : function(c, record, item, index, e, eOpts){
        var store = c.up('grid').store;
        var order = store.data.length + 1;
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '修改属性',
            items: [{
                xtype: 'createProperty',
                fData : record,
                pStore : store,
                isUpdate : true
            }]
        }).show();
    }
});
