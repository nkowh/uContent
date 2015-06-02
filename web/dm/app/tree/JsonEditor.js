Ext.define('dm.tree.JsonEditor', {
    extend: 'Ext.tree.Panel',

    plugins: {
        ptype: 'cellediting',
        clicksToEdit: 2
    },

    initComponent: function () {
        var me = this;
        me.code = {
            "properties": {
                "fs": {
                    "properties": {
                        "data": {
                            "properties": {
                                "available_in_bytes": {"type": "long"},
                                "dev": {"type": "string"},
                                "disk_io_op": {"type": "long"},
                                "disk_io_size_in_bytes": {"type": "long"},
                                "disk_queue": {"type": "string"},
                                "disk_read_size_in_bytes": {"type": "long"},
                                "disk_reads": {"type": "long"},
                                "disk_write_size_in_bytes": {"type": "long"},
                                "disk_writes": {"type": "long"},
                                "free_in_bytes": {"type": "long"},
                                "mount": {"type": "string"},
                                "path": {"type": "string"},
                                "total_in_bytes": {"type": "long"}
                            }
                        },
                        "timestamp": {"type": "long"},
                        "total": {
                            "properties": {
                                "available_in_bytes": {"type": "long"},
                                "disk_io_op": {"type": "long"},
                                "disk_io_size_in_bytes": {"type": "long"},
                                "disk_queue": {"type": "string"},
                                "disk_read_size_in_bytes": {"type": "long"},
                                "disk_reads": {"type": "long"},
                                "disk_write_size_in_bytes": {"type": "long"},
                                "disk_writes": {"type": "long"},
                                "free_in_bytes": {"type": "long"},
                                "total_in_bytes": {"type": "long"}
                            }
                        }
                    }
                },
                "jvm": {
                    "properties": {
                        "buffer_pools": {
                            "properties": {
                                "direct": {
                                    "properties": {
                                        "count": {"type": "long"},
                                        "total_capacity_in_bytes": {"type": "long"},
                                        "used_in_bytes": {"type": "long"}
                                    }
                                },
                                "mapped": {
                                    "properties": {
                                        "count": {"type": "long"},
                                        "total_capacity_in_bytes": {"type": "long"},
                                        "used_in_bytes": {"type": "long"}
                                    }
                                }
                            }
                        },
                        "gc": {
                            "properties": {
                                "collectors": {
                                    "properties": {
                                        "old": {
                                            "properties": {
                                                "collection_count": {"type": "long"},
                                                "collection_time_in_millis": {"type": "long"}
                                            }
                                        },
                                        "young": {
                                            "properties": {
                                                "collection_count": {"type": "long"},
                                                "collection_time_in_millis": {"type": "long"}
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "mem": {
                            "properties": {
                                "heap_committed_in_bytes": {"type": "long"},
                                "heap_max_in_bytes": {"type": "long"},
                                "heap_used_in_bytes": {"type": "long"},
                                "heap_used_percent": {"type": "long"},
                                "non_heap_committed_in_bytes": {"type": "long"},
                                "non_heap_used_in_bytes": {"type": "long"},
                                "pools": {
                                    "properties": {
                                        "old": {
                                            "properties": {
                                                "max_in_bytes": {"type": "long"},
                                                "peak_max_in_bytes": {"type": "long"},
                                                "peak_used_in_bytes": {"type": "long"},
                                                "used_in_bytes": {"type": "long"}
                                            }
                                        },
                                        "survivor": {
                                            "properties": {
                                                "max_in_bytes": {"type": "long"},
                                                "peak_max_in_bytes": {"type": "long"},
                                                "peak_used_in_bytes": {"type": "long"},
                                                "used_in_bytes": {"type": "long"}
                                            }
                                        },
                                        "young": {
                                            "properties": {
                                                "max_in_bytes": {"type": "long"},
                                                "peak_max_in_bytes": {"type": "long"},
                                                "peak_used_in_bytes": {"type": "long"},
                                                "used_in_bytes": {"type": "long"}
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        "threads": {"properties": {"count": {"type": "long"}, "peak_count": {"type": "long"}}},
                        "timestamp": {"type": "long"},
                        "uptime_in_millis": {"type": "long"}
                    }
                },
                "network": {
                    "properties": {
                        "tcp": {
                            "properties": {
                                "active_opens": {"type": "long"},
                                "attempt_fails": {"type": "long"},
                                "curr_estab": {"type": "long"},
                                "estab_resets": {"type": "long"},
                                "in_errs": {"type": "long"},
                                "in_segs": {"type": "long"},
                                "out_rsts": {"type": "long"},
                                "out_segs": {"type": "long"},
                                "passive_opens": {"type": "long"},
                                "retrans_segs": {"type": "long"}
                            }
                        }
                    }
                },
                "os": {
                    "properties": {
                        "cpu": {
                            "properties": {
                                "idle": {"type": "long"},
                                "stolen": {"type": "long"},
                                "sys": {"type": "long"},
                                "usage": {"type": "long"},
                                "user": {"type": "long"}
                            }
                        },
                        "mem": {
                            "properties": {
                                "actual_free_in_bytes": {"type": "long"},
                                "actual_used_in_bytes": {"type": "long"},
                                "free_in_bytes": {"type": "long"},
                                "free_percent": {"type": "long"},
                                "used_in_bytes": {"type": "long"},
                                "used_percent": {"type": "long"}
                            }
                        },
                        "swap": {"properties": {"free_in_bytes": {"type": "long"}, "used_in_bytes": {"type": "long"}}},
                        "timestamp": {"type": "long"},
                        "uptime_in_millis": {"type": "long"}
                    }
                },
                "process": {
                    "properties": {
                        "cpu": {
                            "properties": {
                                "percent": {"type": "long"},
                                "sys_in_millis": {"type": "long"},
                                "total_in_millis": {"type": "long"},
                                "user_in_millis": {"type": "long"}
                            }
                        },
                        "mem": {
                            "properties": {
                                "resident_in_bytes": {"type": "long"},
                                "share_in_bytes": {"type": "long"},
                                "total_virtual_in_bytes": {"type": "long"}
                            }
                        },
                        "open_file_descriptors": {"type": "long"},
                        "timestamp": {"type": "long"}
                    }
                },
                "timestamp": {"type": "long"}
            }
        };

        Ext.apply(me, {
            columns: [{
                xtype: 'treecolumn',
                text: 'Name',
                flex: 1,
                dataIndex: me.displayField,
                cellTpl: [
                    '<tpl for="lines">',
                    '<img src="{parent.blankUrl}" class="{parent.childCls} {parent.elbowCls}-img ',
                    '{parent.elbowCls}-<tpl if=".">line<tpl else>empty</tpl>" role="presentation"/>',
                    '</tpl>',
                    '<img src="{blankUrl}" class="{childCls} {elbowCls}-img {elbowCls}',
                    '<tpl if="isLast">-end</tpl><tpl if="expandable">-plus {expanderCls}</tpl>" role="presentation"/>',
                    '<tpl if="checked !== null">',
                    '<input type="button" {ariaCellCheckboxAttr}',
                    ' class="{childCls} {checkboxCls}<tpl if="checked"> {checkboxCls}-checked</tpl>"/>',
                    '</tpl>',
                    '<tpl if="icon"><span role="presentation" class="fa-stack">{icon}</span><tpl elseif="iconCls"><span role="presentation"><i class="fa {iconCls} fa-lg fa-fw"></i></span><tpl else>',
                    '<img src="{blankUrl}" role="presentation" class="{childCls} {baseIconCls} ',
                    '{baseIconCls}-<tpl if="leaf">leaf<tpl else>parent</tpl> {iconCls}"',
                    '<tpl if="icon">style="background-image:url({icon})"</tpl>/></tpl>',
                    '<tpl if="href">',
                    '<a href="{href}" role="link" target="{hrefTarget}" class="{textCls} {childCls}">{value}</a>',
                    '<tpl else>',
                    '<span class="{textCls} {childCls}">{value}</span>',
                    '</tpl>'
                ], editor: {
                    xtype: 'textfield',
                    allowBlank: false
                }
            }, {
                text: 'value',
                dataIndex: 'value',
                flex: 1,
                editor: {
                    xtype: 'textfield',
                    allowBlank: false
                }
            }, Ext.create('dm.grid.column.Action', {
                sortable: false,
                scope: me,
                items: [{
                    style: 'font-size:20px;color:Green;',
                    iconCls: 'fa fa-plus',
                    handler: me.addObject
                }, {
                    style: 'margin 10px!important;font-size:20px;color:Green;',
                    iconCls: 'fa fa-chevron-circle-down',
                    handler: me.appendChild
                }, {
                    style: 'font-size:20px;color:red;',
                    iconCls: 'fa fa-times',
                    handler: me.appendChild
                }]
            })]
        });

        if (me.code)this.root = me.buildJsonTree({children: []}, me.code);

        this.callParent();
    },

    addObject: function (view, rowIndex, colIndex, item, e, record) {
        record.getTreeStore().commitChanges();
        var root = record.getTreeStore().getRootNode();
        var node = root.findChild('id', record.get('id'), true);
        var parent = node.parentNode ? node.parentNode : root
        var index = parent.indexOf(node);
        parent.insertChild(index + 1, {text: 'newobj'});
    },

    appendChild: function (view, rowIndex, colIndex, item, e, record) {
        record.getTreeStore().commitChanges();
        var root = record.getTreeStore().getRootNode();
        var node = root.findChild('id', record.get('id'), true);
        node.appendChild({text: 'newobjchild'})

    },

    buildJsonTree: function (node, obj) {
        var me = this;
        Ext.Object.each(obj, function (key, value) {
            if (Ext.isObject(value))
                node.children.push(me.buildJsonTree({text: key, children: []}, obj[key]));
            else if (Ext.isArray(value)) {
                var arr = {text: key, children: []};
                Ext.each(value, function (item, index) {
                    arr.children.push(me.buildJsonTree({text: index, children: []}, item));
                });
                node.children.push(arr);
            }
            else
                node.children.push({
                    text: key,
                    value: value,
                    iconCls: 'fa-leaf',
                    leaf: true
                });
        });
        return node;
    },

    bindCode: function (code) {
        this.setStore(Ext.create('Ext.data.TreeStore', {
            root: this.buildJsonTree({children: []}, code)
        }));
    }


});