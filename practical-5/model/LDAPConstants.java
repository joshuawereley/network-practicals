package model;

public class LDAPConstants {
    public static final int LDAP_PORT = 389;
    public static final String SERVER = "localhost";
    public static final String ADMIN_DN = "cn=admin,dc=example,dc=com";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String BASE_DN = "ou=Friends,dc=example,dc=com";
    public static final byte LDAP_SUCCESS = 0x00;

    public static String getLDAPErrorDescription(byte errorCode) {
        switch (errorCode) {
            case 0x00:
                return "Success";
            case 0x01:
                return "Operations error";
            case 0x02:
                return "Protocol error";
            case 0x03:
                return "Time limit exceeded";
            case 0x04:
                return "Size limit exceeded";
            case 0x05:
                return "Compare false";
            case 0x06:
                return "Compare true";
            case 0x07:
                return "Auth method not supported";
            case 0x08:
                return "Strong auth required";
            case 0x09:
                return "Partial results";
            case 0x0A:
                return "Referral";
            case 0x0B:
                return "Admin limit exceeded";
            case 0x0C:
                return "Unavailable critical extension";
            case 0x0D:
                return "Confidentiality required";
            case 0x0E:
                return "Sasl bind in progress";
            case 0x10:
                return "No such attribute";
            case 0x11:
                return "Undefined attribute type";
            case 0x12:
                return "Inappropriate matching";
            case 0x13:
                return "Constraint violation";
            case 0x14:
                return "Attribute or value exists";
            case 0x15:
                return "Invalid attribute syntax";
            case 0x20:
                return "No such object";
            case 0x21:
                return "Alias problem";
            case 0x22:
                return "Invalid DN syntax";
            case 0x23:
                return "Alias dereferencing problem";
            case 0x30:
                return "Inappropriate authentication";
            case 0x31:
                return "Invalid credentials";
            case 0x32:
                return "Insufficient access rights";
            case 0x33:
                return "Busy";
            case 0x34:
                return "Unavailable";
            case 0x35:
                return "Unwilling to perform";
            case 0x36:
                return "Loop detect";
            case 0x40:
                return "Naming violation";
            case 0x41:
                return "Object class violation";
            case 0x42:
                return "Not allowed on non-leaf";
            case 0x43:
                return "Not allowed on RDN";
            case 0x44:
                return "Entry already exists";
            case 0x45:
                return "Object class mods prohibited";
            case 0x46:
                return "Affects multiple DSAs";
            case 0x50:
                return "Other";
            default:
                return "Unknown error (" + errorCode + ")";
        }
    }
}