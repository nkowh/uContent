Ext.define('explorer.view.main.MainHeader', {
    extend: 'Ext.panel.Panel',
    xtype: 'app-header',
    controller: 'mainheader',
    layout: 'column',

    //initComponent: function () {
    //    this.callParent();
    //},
    items: [
        {
            columnWidth: 0.4,
            bind: {
                html: '<p>{headerTitle}</p>'
            }
        },
        {
            columnWidth: 0.6,
            xtype : 'toolbar',
            items : [
                {
                    xtype    : 'textfield',
                    name     : 'fullText',
                    width : 250,
                    emptyText: 'enter search term'
                },
                {
                    text: '全文搜索',
                    handler: 'fullText'
                }
            ]
        },
        {
            width: 200,
            xtype : 'toolbar',
            items : [
                {
                    text   : '高级搜索',
                    handler: 'advQuery'
                },
                {
                    text   : '新建文档',
                    handler  : 'indexDoc'
                },
                {
                    text   : '退出',
                    handler  : 'logout'
                }
            ]
        }
    ]
});
