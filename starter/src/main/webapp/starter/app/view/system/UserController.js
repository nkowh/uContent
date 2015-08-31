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
        var record = this.getView().getSelectionModel().getSelection();
        var id = record[0].get('Id');
        if(record&&record.length>0){
            Ext.Msg.confirm("Title","Are you sure to delete this User？",function(r) {
                var store = me.getViewModel().getStore('users');
                store.remove(record[0]);
                store.sync({success: function(batch, options) {
                    Ext.Msg.alert('message', 'Success.');
                },
                    failure: function(batch, options) {
                        Ext.Msg.alert('message', options.request.scope.reader.jsonData["message"]);
                    }
                });
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

           var user = Ext.create('starter.model.User', form.getValues());
           //user.phantom = true;
            var store = me.getViewModel().getStore('users');
            //store.suspendAutoSync();
            store.add(user);
            store.sync({success: function(batch, options) {
                me.getView().up('window').close();
                Ext.Msg.alert('message', 'Success.');
            },
               failure: function(batch, options) {
                   Ext.Msg.alert('message', options.request.scope.reader.jsonData["message"]);
               },
                scope : me
            });
        }
    },
    modifySave : function(e){
        var me = this;
        var form = e.up('form').getForm();
        if (form.isValid()) {
            var store = me.getViewModel().getStore('users');
            var user = form.getValues();
            //form.updateRecord(user);
            var record = store.getById(user._id);
            record.set(user);
            //store.commitChanges();
            store.sync();
        }

        }


});
