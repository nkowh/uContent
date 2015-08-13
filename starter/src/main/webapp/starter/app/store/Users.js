Ext.define('starter.store.Users', {
    extend: 'Ext.data.Store',
    alias : 'store.users',
    model: 'starter.model.User',
    pageSize: 10,
    autoSync : true,
    proxy: {
        type: 'rest',
        startParam : '$skip',
        pageParam : '',
        limitParam : '$top',
        url: '/dm/Users',
        reader: {
            type: 'json',
            rootProperty: 'value',
            totalProperty: 'total'
        }
    },
    //listeners: {
    //    datachanged : {
    //        element: 'body', //bind to the underlying body property on the panel
    //        fn: function(){ alert('操作成功！'); }
    //    }
    //},

    autoLoad: true
});