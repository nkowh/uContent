package starter.service;

import java.util.Locale;

/**
 * Created by Administrator on 2015/8/13.
 */
public class Constant {

    /**
     * 权限
     */
    public enum Permission{
        READ(1),
        WRITE(2),
        UPDATE(4),
        DELETE(8);

        private int value = 0;

        Permission(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static Permission fromValue(int value) {
            switch (value){
                case 1:
                    return READ;
                case 2:
                    return WRITE;
                case 3:
                    return UPDATE;
                case 4:
                    return DELETE;
                default:
                    throw new RuntimeException("No type match for [" + value + "]");
            }
        }

        public static Permission fromString(String permission) {
            String lowersOpType = permission.toLowerCase(Locale.ROOT);
            switch (lowersOpType) {
                case "read":
                    return Permission.READ;
                case "write":
                    return Permission.WRITE;
                default:
                    throw new RuntimeException("permission [" + permission + "] not allowed, either [read] or [write] are allowed");
            }
        }
    }


    /**
     * ES中mapping定义相关字段名称
     */
    public class FieldName {

        public static final String meta = "_meta";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String CREATEDBY = "createdBy";
        public static final String CREATEDON = "createdOn";
        public static final String LASTUPDATEDBY = "lastupdatedBy";
        public static final String LASTUPDATEDON = "lastupdatedOn";
        public static final String ACL = "_acl";
        public static final String STREAMS = "_streams";
        public static final String STREAMID = "streamId";
        public static final String STREAMNAME = "streamName";
        public static final String CONTENTTYPE = "contentType";
        public static final String LENGTH = "length";
        public static final String REQUIRED = "required";
        public static final String TYPE = "type";
        public static final String DEFAULTVALUE = "defaultValue";
        public static final String ORDER = "order";
        public static final String PATTERN = "pattern";
        public static final String PROMPTMESSAGE = "promptMessage";
        public static final String USER = "user";
        public static final String GROUP = "group";
        public static final String PERMISSION = "permission";
        public static final String ALLOWABLEACTIONS = "_allowableActions";

    }


}
