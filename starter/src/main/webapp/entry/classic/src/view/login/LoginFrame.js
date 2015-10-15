Ext.define('entry.view.login.LoginFrame', {
    extend: 'Ext.panel.Panel',
    controller: 'login',
    viewModel: 'login',
    layout: 'center',
    items: [
        {xtype: 'loginForm'}
    ]
});