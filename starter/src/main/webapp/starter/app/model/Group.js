Ext.define('starter.model.Group', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'CreateBy',  type: 'string'},
        {name: 'CreatedOn',   type: 'date'},
        {name: 'Id', type: 'string'},
        {name: 'Name', type: 'string'},
        {name: 'LastUpdatedBy', type: 'string'},
        {name: 'LastUpdatedOn', type: 'date'},
        {name: 'Users', type: 'auto'},
        {name: 'Group', type: 'auto'}
    ],
    idProperty: 'Id'
});