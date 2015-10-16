Ext.define('admin.store.Views', {
    extend: 'Ext.data.Store',
    alias : 'store.views',
    model: 'admin.model.View',
    pageSize: 10,
    //autoSync : true,
    remoteSort : true,
    proxy: {
        type: 'rest',
        url: '/svc/views',
        headers: {
            //'_method' : 'QUERY',
            //'Content-Type' : 'application/json;charset=UTF-8'
        },
        //paramsAsJson :true,

        actionMethods : {
            update: 'PATCH'
            //read : 'POST'
        },
        reader: {
            type: 'json',
            rootProperty: 'views',
            totalProperty: 'total'
        }
    },
    autoLoad: true
});