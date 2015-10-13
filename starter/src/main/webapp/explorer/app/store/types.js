Ext.define('explorer.store.Types', {
    extend: 'Ext.data.Store',
    alias: 'store.types',
    autoLoad: true,
    proxy: {
        type: 'rest',
        headers: {'Content-Type': "application/json;charset=utf-8" },
        url: '/svc/types',
        startParam : '',
        pageParam : '',
        limitParam : '',
        reader: {
            type: 'json',
            root :'documentTypes'
        }
    },
    model:'explorer.model.Type'
});