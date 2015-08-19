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
}
