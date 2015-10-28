Ext.define('admin.view.main.GroupController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.group',

    openCreateWin: function (sender, record) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建组',
            items: [{
                xtype: 'createGroup',
                store: this.getView().getStore()
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '修改组',
            items: [{
                xtype: 'modifyGroup',
                record: record
            }]
        }).show();
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        var users = record.get('users');
        var usersObj = this.getView().down('itemselector');
        // usersObj.setValue(['AU9j1LcP4oXt9xabfnOL']);
        //Ext.Array.each(users, function (user, index, countriesItSelf) {
        //    var userIds = usersObj.setValue(user.userId);
        //});
        this.getView().getForm().loadRecord(record);
    },
    deleteGroup: function (e) {
        var me = this;
        var grid = e.up('grid');
        var record = this.getView().getSelectionModel().getSelection();
        var store = record[0].store;
        if (record && record.length > 0) {
            Ext.Msg.confirm("Title", "Are you sure to delete this Group？", function (r) {
                if(r=='yes'){
                    store.remove(record[0]);
                    me.sync(store);
                }
            });
        } else {
            Ext.Msg.alert('message', 'Please select one item at least.');
        }
    },
    createSave: function (e) {
        var me = this;
        var form = this.getView().getForm();
        if (!form.isValid())return;
        var store = this.getView().store;
        var group = store.add(form.getValues())[0];
        group.phantom = true;
        me.sync(store);
    },
    modifySave: function (e) {
        var me = this;
        var record = me.getView().record;
        var form = me.getView().getForm();
        if (!form.isValid())return;
        var group = form.getValues();
        record.set('groupName', group.groupName);
        record.set('users', group.users);
        me.sync(record.store)
    },

    sync: function (store) {
        var view = this.getView();
        var window = view.up('window');
        view.mask('loading...');
        store.sync({
            callback: function (batch, options) {
                view.unmask();
                if (window)window.close();
                Ext.toast({
                    html: 'Successful operation',
                    title: 'message',
                    width: 200,
                    align: 't'
                });
            }
        });
    }

});
