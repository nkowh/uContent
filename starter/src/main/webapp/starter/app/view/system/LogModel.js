Ext.define('starter.system.LogModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.log',
    stores: {
        logs: {type: 'logs'}
    },
    data: {
        listTitle: '日志管理'
    }

});
