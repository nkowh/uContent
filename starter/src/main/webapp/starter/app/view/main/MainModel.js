Ext.define('starter.view.main.MainModel', {
    extend: 'Ext.app.ViewModel',

    alias: 'viewmodel.main',

    data: {
        name:  Ext.util.Cookies.get('userId')

    }

});
