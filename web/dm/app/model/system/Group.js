Ext.define('dm.model.system.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {name: '_id', type: 'string'},
        {name: 'display', type: 'string', mapping: '_source.display'},
        {name: 'users', mapping: '_source.users'},
        {name: 'createAt', type: 'date', dateFormat: 'time', mapping: '_source.createAt'}
    ],
    idProperty: '_id',
    proxy: Ext.create('dm.data.proxy.ElasticDocument', {
        type: 'rest',
        url: Ext.util.Cookies.get('service') + '/system/groups',
        reader: {
            type: 'json'
        }
    })
});