Ext.define('starter.system.GroupModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.group',
    stores: {
        users: {type: 'users'},
        groups: {type: 'groups'}
    },
    data: {
        pageSize : 10,
        listTitle: '组管理',
        createTitle : '创建组',
        modifyTitle : '修改组'
    }

});
