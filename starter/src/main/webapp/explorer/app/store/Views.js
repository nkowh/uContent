Ext.define('explorer.store.Views', {
    extend: 'Ext.data.Store',

    alias : 'store.views',
    model: 'explorer.model.View',
    pageSize: 10,
    autoSync : true,
    remoteSort : true,
    proxy: {
        type: 'rest',
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
