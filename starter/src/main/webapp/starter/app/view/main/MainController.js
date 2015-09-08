/**
 * This class is the controller for the main view for the application. It is specified as
 * the "controller" of the Main view class.
 *
 * TODO - Replace this content of this view to suite the needs of your application.
 */
Ext.define('starter.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',

    onItemSelected: function (sender, record) {
        Ext.Msg.confirm('Confirm', 'Are you sure?', 'onConfirm', this);
    },

    onConfirm: function (choice) {
        if (choice === 'yes') {
            //
        }
    },
    checkIsAdmin : function(){
        var me = this;
        Ext.Ajax.request({
            method: 'GET',
            url: '/svc/currentUsers/isAdmin',
            success: function(response, opts) {
                var isAdmin = Ext.decode(response.responseText);
                //if(isAdmin){
                    me.getView().add( {
                        title: '用户',
                        iconCls: 'fa-user',
                        items: [{
                            xtype: 'users'
                        }]
                    }, {
                        title: '组',
                        iconCls: 'fa-users',
                        items: [{
                            xtype: 'groups'
                        }]
                    }, {
                        title: '监控',
                        iconCls: 'fa-cog',
                        items: [{
                            xtype: 'monitorOs'
                        }]
                    }, {
                        title: '日志',
                        iconCls: 'fa fa-bars',
                        items: [{
                            xtype: 'logs'
                        }]
                    },{
                        title: '类型',
                        iconCls: 'fa-cubes',
                        items: [{
                            xtype: 'types'
                        }]
                    }, {
                        title: '配置',
                        iconCls: 'fa-cog',
                        items: []
                    });
                //}

            }

            //failure: function(response, opts) {
            //    var obj = Ext.decode(response.responseText);
            //    alert(obj);
            //}

        });
    }
});
