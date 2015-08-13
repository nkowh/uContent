Ext.define('starter.system.GroupController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.group',

    openCreateWin: function (sender, record) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title:'新建组',
            items:[{
                xtype: 'createGroup'
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title:'修改组',
            items:[{
                xtype: 'modifyGroup',
                record : record
            }]
        }).show();
        return ;
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        this.getView().getForm().loadRecord(record);
    },
    deleteGroup : function(e){
        var me = this;
        var grid = e.up('grid');
        var record = this.getView().getSelectionModel().getSelection();
        var id = record[0].get('Id');
        if(record&&record.length>0){
            Ext.Msg.confirm("Title","Are you sure to delete this Group？",function(r) {
                me.getViewModel().getStore('groups').remove(record[0]);
            });

        }else{
            Ext.Msg.alert('message', 'Please select one item at least.');
            return ;
        }
    },
    createSave : function(e){
        var form = e.up('form').getForm();
        if (form.isValid()) {
            //var url  = '/dm/Users';
            var  uuid = Ext.data.identifier.Uuid.createRandom()();
            e.up('form').down('hiddenfield[name=Id]').setValue(uuid);
            var user = Ext.create('dm.model.Group', form.getValues());
            user.phantom =true;
            var store = this.getViewModel().getStore('groups');
            store.add(user);
            this.getView().up('window').close();
            //store.load({
            //    callback: function(records, operation, success) {
            //        Ext.Msg.alert('info', '加载完毕');
            //    }
            //});
        }
    },
    modifySave : function(e){
        var form = e.up('form').getForm();
        var userValues = form.getValues();
        if (form.isValid()) {
            var store = this.getViewModel().getStore('groups');
            var user =form.getRecord();
            form.updateRecord(user);
            store.commitChanges();
            this.getView().up('window').close();
            //store.load({
            //    callback: function(records, operation, success) {
            //        Ext.Msg.alert('info', '加载完毕');
            //    }
            //});
        }

    }

});
