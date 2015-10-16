Ext.define('starter.view.main.MainController', {
    extend: 'Ext.app.ViewController',

    alias: 'controller.main',

    onTitleClick: function (cmp, e, eOpts) {
        Ext.Msg.prompt('确认', '是否退出系统?', function (btn, text) {
            if (btn != 'ok')return;
            Ext.util.Cookies.clear('userId');
            Ext.util.Cookies.clear('digest');
        });
        Ext.Msg.show({
            title: '确认',
            message: '是否退出系统?',
            buttons: Ext.Msg.OKCANCEL,
            icon: Ext.Msg.QUESTION,
            fn: function (btn) {
                if (btn != 'ok')return;
                Ext.util.Cookies.clear('userId');
                Ext.util.Cookies.clear('digest');
                window.location.href = '/entry/index.html';
            }
        });
    },
    checkIsAdmin: function () {
        var me = this;
        var groups = Ext.util.Cookies.get('groups');
        if (groups.indexOf('adminGroup') >= 0) {
            me.getView().add({
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
                title: '重建索引',
                iconCls: 'fa-cog',
                items: [{
                    xtype: 'reIndex'
                }]
            }, {
                title: '日志',
                iconCls: 'fa fa-bars',
                items: [{
                    xtype: 'logs'
                }]
            }, {
                title: '类型',
                iconCls: 'fa-cubes',
                items: [{
                    xtype: 'types'
                }]
            }, {
                title: '配置',
                iconCls: 'fa-cog',
                items: [{
                    xtype: 'config'
                }]
            });
        }
    },

    onTabchange: function (tabPanel, newCard, oldCard, eOpts) {
        if (oldCard && oldCard.items.getAt(0))
            oldCard.items.getAt(0).fireEvent('deactivate', {})
        if (newCard && newCard.items.getAt(0))
            newCard.items.getAt(0).fireEvent('activate', {})
    }

});
