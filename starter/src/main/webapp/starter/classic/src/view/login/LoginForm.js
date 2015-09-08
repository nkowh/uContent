Ext.define('starter.view.login.LoginForm', {
    extend: 'Ext.form.Panel',
    xtype: 'loginForm',

    title: '登录',
    frame: true,
    width: 320,
    bodyPadding: 10,

    defaultType: 'textfield',

    items: [{
        allowBlank: false,
        fieldLabel: '用户ID',
        name: 'user',
        emptyText: 'user id',
        bind:'{userId}'
    }, {
        allowBlank: false,
        fieldLabel: '登录密码',
        name: 'pass',
        emptyText: 'password',
        inputType: 'password',
        bind:'{password}'
    }],

    buttons: [
        {
            text: 'Login',
            handler: 'login'
        }
    ],

    initComponent: function () {
        this.defaults = {
            anchor: '100%',
            labelWidth: 120
        };

        this.callParent();
    }
});