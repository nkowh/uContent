Ext.define('dm.model.Document', {
    extend: 'Ext.data.Model',

    fields: [
        {name: 'Id', type: 'string'},
        {name: 'Name', type: 'string'},
        {name: 'CreateBy', type: 'string'},
        {name: 'LastUpdatedBy', type: 'string'},
        {name: 'CreatedOn', type: 'date'},
        {name: 'LastUpdatedOn', type: 'date'}
    ],
    idProperty: 'Id'
});