Ext.define('admin.model.Tag', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'tagContext',  type: 'string'},
        {name: 'description',   type: 'string'}
    ],
    idProperty: '_id'
});