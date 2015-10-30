Ext.define('chemistry.store.Views', {
    extend: 'Ext.data.Store',
    alias : 'store.views',
    model: 'chemistry.model.View',
    pageSize: 10,
    autoSync : true,
    remoteSort : true,
    proxy: {
        type: 'rest',
        headers: {'Content-Type': "application/json;charset=utf-8" },
        url: '/svc/views',
        startParam : '',
        pageParam : '',
        limitParam : '',
        reader: {
            type: 'json',
            root :'views'
        }
    },
    autoLoad: true
});