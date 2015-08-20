Ext.define('starter.system.UserController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.user',

    openCreateWin: function (sender, record) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title:'新建用户',
            items:[{
                xtype: 'createUser'
            }]
        }).show();
    },
    openModifyWin: function (grid, record, tr, rowIndex, e, eOpts) {
        Ext.create('Ext.window.Window', {
            layout: 'fit',
            title:'修改用户',
            items:[{
                xtype: 'modifyUser',
                record : record
            }]
        }).show();
        return ;
    },
    loadModifyData: function (e, eOpts) {
        var record = this.getView().record;
        this.getView().getForm().loadRecord(record);
    },
    deleteUser : function(e){
        var me = this;
        var grid = e.up('grid');
        var record = this.getView().getSelectionModel().getSelection();
        var id = record[0].get('Id');
        if(record&&record.length>0){
            Ext.Msg.confirm("Title","Are you sure to delete this User？",function(r) {
                me.getViewModel().getStore('users').remove(record[0]);
            });

        }else{
            Ext.Msg.alert('message', 'Please select one item at least.');
            return ;
        }
    },
    createSave : function(e){
        var me = this;
        var form = this.getView().getForm();
        if (form.isValid()) {
            var userId = form.down('textfield[name=userId]').getValue();
            Ext.Ajax.request({
                url: '/svc/users/'+userId+'/exist',
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    if( response.responseText!=''){
                        var data =  Ext.decode(response.responseText);
                        if(data.exist){
                            Ext.Msg.alert('message', 'The user with the same userId already exists.');
                        }else{
                            var user = Ext.create('starter.model.User', form.getValues());
                            var store = this.getViewModel().getStore('users');
                            store.add(user);
                            me.getView().up('window').close();
                        }
                    }
                }
            });

        }
    },
    modifySave : function(e){
        var me = this;
        var form = e.up('form').getForm();
        var userValues = form.getValues();
        if (form.isValid()) {
            var userId = form.down('textfield[name=userId]').getValue();
            Ext.Ajax.request({
                url: '/svc/users/'+userId+'/exist',
                callback: function (options, success, response) {
                    if (!success) {
                        return;
                    }
                    if( response.responseText==''){
                        var data =  Ext.decode(response.responseText);
                        if(data.exist){
                            Ext.Msg.alert('message', 'The user with the same userId already exists.');
                        }else {
                            var store = me.getViewModel().getStore('users');
                            var user = form.getRecord();
                            form.updateRecord(user);
                            store.commitChanges();
                            me.getView().up('window').close();
                        }
                    }
                }
            });

        }

    }

});
