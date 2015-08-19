Ext.define('starter.system.TypeModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.type',
    stores: {
        types: {type: 'types'},
        properties : {type : 'properties'}
    },
    data: {
        pageSize : 10,
        listTitle: '类型管理',
        createTitle : '创建类型',
        modifyTitle : '修改类型',
        propertyTitle : 'Business Field List'
    }

});
