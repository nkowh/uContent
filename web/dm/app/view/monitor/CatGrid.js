Ext.define('dm.view.monitor.CatGrid', {
    extend: 'Ext.grid.Panel',
    xtype: 'cat',
    initComponent: function () {
        var me = this;
        Ext.apply(me, {
            listeners: {
                //afterrender: function () {
                //    me.reload();
                //},
                destroy: function () {
                    if (me.task)Ext.TaskManager.stop(me.task);
                }
            }
        })
        me.callParent();

        me.task = Ext.TaskManager.start({
            scope: me,
            run: me.reload,
            interval: 3000
        });
    },

    reload: function () {
        var me = this;
        if (me.up('dashboard-panel').getCollapsed())return;
        Ext.Ajax.request({
            url: Ext.util.Cookies.get('service') + '/_cat/' + me.tag + '?v',
            callback: function (options, success, response) {
                if (!success) {
                    form.toast(response.responseText);
                    return;
                }
                var table = [];
                Ext.each(Ext.util.CSV.decode(response.responseText, ' '), function (line) {
                    table.push(Ext.Array.filter(line, function (item) {
                        return item.length > 0
                    }));
                });
                var headers = table[0];
                var body = Ext.Array.toArray(table, 1, table.length - 1);
                var columns = [];
                Ext.each(headers, function (header) {
                    columns.push({dataIndex: header, text: header, flex: 1});
                });

                me.reconfigure(Ext.create('Ext.data.ArrayStore', {
                    fields: headers, data: body
                }), columns)
            }
        });

    }


});
