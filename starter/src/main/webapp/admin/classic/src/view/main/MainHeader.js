Ext.define('admin.view.main.MainHeader', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-header',
    controller: 'main',
    layout: 'column',

    //initComponent: function () {
    //    this.callParent();
    //},
    items: [
        {
            columnWidth: 0.95,
            bind: {
                html: '<p>{headerTitle}</p>'
            }
        },
        {
            columnWidth: 0.05,
            xtype : 'toolbar',
            items : [
                {
                    text   : '退出',
                    handler  : 'onTitleClick'
                }
            ]
        }
    ]
});
