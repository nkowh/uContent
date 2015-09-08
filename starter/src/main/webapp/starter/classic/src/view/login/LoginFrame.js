Ext.define('starter.view.login.LoginFrame', {
    extend: 'Ext.panel.Panel',
    requires: [
        'starter.view.login.LoginForm',
        'starter.view.login.LoginController',
        'starter.view.login.LoginModel'
    ],

    controller: 'login',
    viewModel: 'login',
    layout: 'center',
    items: [
        {xtype: 'loginForm'}
    ]
});