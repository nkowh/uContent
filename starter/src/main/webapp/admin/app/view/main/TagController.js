Ext.define('admin.view.main.TagController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.tag',
    openCreateWin: function () {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '新建标签',
            items: [{
                xtype: 'createTag',
                store: this.getView().getStore()
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title: '修改标签',
            items: [{
                xtype: 'modifyTag',
                record: record
            }]
        }).show();
        return;
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        this.getView().getForm().loadRecord(record);
    },
    deleteTag: function (e) {
        var me = this;
        var record = this.getView().getSelectionModel().getSelection();
        if (!record || record.length == 0) {
            Ext.Msg.alert('message', 'Please select one item at least.');
            return;
        }

        Ext.Msg.confirm("Title", "Are you sure to delete this User ?", function (r) {
            if(r==='yes'){
                var store = record[0].store;
                store.remove(record[0]);
                me.sync(store);
            }
        });
    },

    createSave: function (e) {
        var me = this;
        var form = this.getView().getForm();
        if (!form.isValid())return;
        var store = me.getView().store;
        var user = store.add(form.getValues())[0];
        user.phantom = true;
        me.sync(store);

    },

    modifySave: function (e) {
        var me = this;
        var form = e.up('form').getForm();
        if (!form.isValid())return;
        var record = me.getView().record;
        record.set(form.getValues());
        me.sync(record.store);
    },

    sync: function (store) {
        var me = this;
        var view = me.getView();
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
