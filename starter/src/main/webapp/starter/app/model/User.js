Ext.define('starter.model.User', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'CreateBy',  type: 'string'},
        {name: 'CreatedOn',   type: 'date'},
        {name: 'Password', type: 'string'},
        {name: 'Id', type: 'string'},
        {name: 'Name', type: 'string'},
        {name: 'LastUpdatedBy', type: 'string'},
        {name: 'LastUpdatedOn', type: 'date'}
    ],
    idProperty: 'Id'
});