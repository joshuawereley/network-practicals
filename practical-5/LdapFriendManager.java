import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class LdapFriendManager {
    private static final int LDAP_PORT = 389;
    private static int messageId = 1;
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static final String BASE_DN = "ou=Friends,dc=mycompany,dc=com";

    public static void main(String[] args) {
        try {
            connectToServer();
            if (authenticate()) {
                showMenu();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private static void connectToServer() throws IOException {
        socket = new Socket("localhost", LDAP_PORT);
        socket.setSoTimeout(5000);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private static boolean authenticate() throws IOException {
        byte[] bindRequest = createBindRequest();
        out.write(bindRequest);
        byte[] response = readFullResponse(in);
        return parseBindResponse(response);
    }

    private static void showMenu() throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== LDAP Friend Manager ===");
            System.out.println("1. Add Friend");
            System.out.println("2. Search Friend");
            System.out.println("3. List All Friends");
            System.out.println("4. Delete Friend");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addFriend(scanner);
                    break;
                case 2:
                    searchFriend(scanner);
                    break;
                case 3:
                    listAllFriends();
                    break;
                case 4:
                    deleteFriend(scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void addFriend(Scanner scanner) throws IOException {
        System.out.println("\n=== Add New Friend ===");
        System.out.print("Enter friend's common name (cn): ");
        String cn = scanner.nextLine();
        System.out.print("Enter friend's surname (sn): ");
        String sn = scanner.nextLine();
        System.out.print("Enter telephone number: ");
        String telephone = scanner.nextLine();

        byte[] addRequest = createAddRequest(cn, sn, telephone);
        out.write(addRequest);
        byte[] response = readFullResponse(in);
        parseAddResponse(response);
    }

    private static void searchFriend(Scanner scanner) throws IOException {
        System.out.println("\n=== Search Friend ===");
        System.out.print("Enter friend's name to search: ");
        String name = scanner.nextLine();

        byte[] searchRequest = createSearchRequest("(cn=" + name + ")");
        out.write(searchRequest);
        byte[] response = readFullResponse(in);
        parseSearchResponse(response);
    }

    private static void listAllFriends() throws IOException {
        System.out.println("\n=== List All Friends ===");
        byte[] searchRequest = createSearchRequest("(objectClass=*)");
        out.write(searchRequest);
        byte[] response = readFullResponse(in);
        parseSearchResponse(response);
    }

    private static void deleteFriend(Scanner scanner) throws IOException {
        System.out.println("\n=== Delete Friend ===");
        System.out.print("Enter friend's common name (cn) to delete: ");
        String cn = scanner.nextLine();

        byte[] deleteRequest = createDeleteRequest(cn);
        out.write(deleteRequest);
        byte[] response = readFullResponse(in);
        parseDeleteResponse(response);
    }

    private static byte[] createBindRequest() {
        byte[] version = {0x02, 0x01, 0x03};
        String bindDN = "cn=admin,dc=mycompany,dc=com";
        String password = "adminpassword";

        byte[] dn = encodeOctetString(bindDN.getBytes());
        byte[] pwd = encodeSimpleAuth(password.getBytes());

        byte[] bindElements = new byte[version.length + dn.length + pwd.length];
        System.arraycopy(version, 0, bindElements, 0, version.length);
        System.arraycopy(dn, 0, bindElements, version.length, dn.length);
        System.arraycopy(pwd, 0, bindElements, version.length + dn.length, pwd.length);

        byte[] bindRequest = encodeSequence((byte)0x60, bindElements);
        byte[] msgId = encodeInteger(messageId++);
        byte[] ldapMessage = new byte[msgId.length + bindRequest.length];
        System.arraycopy(msgId, 0, ldapMessage, 0, msgId.length);
        System.arraycopy(bindRequest, 0, ldapMessage, msgId.length, bindRequest.length);
        
        return encodeSequence((byte)0x30, ldapMessage);
    }

    private static byte[] createAddRequest(String cn, String sn, String telephone) {
        String dn = "cn=" + cn + "," + BASE_DN;
        
        byte[][] attributes = {
            encodeAttribute("objectClass", "inetOrgPerson"),
            encodeAttribute("cn", cn),
            encodeAttribute("sn", sn),
            encodeAttribute("telephoneNumber", telephone)
        };
        
        ByteArrayOutputStream attrStream = new ByteArrayOutputStream();
        for (byte[] attr : attributes) {
            attrStream.write(attr, 0, attr.length);
        }
        byte[] attributeList = encodeSequence((byte)0x30, attrStream.toByteArray());
        
        byte[] dnBytes = encodeOctetString(dn.getBytes());
        byte[] addElements = new byte[dnBytes.length + attributeList.length];
        System.arraycopy(dnBytes, 0, addElements, 0, dnBytes.length);
        System.arraycopy(attributeList, 0, addElements, dnBytes.length, attributeList.length);
        
        byte[] addRequest = encodeSequence((byte)0x68, addElements);
        byte[] msgId = encodeInteger(messageId++);
        byte[] ldapMessage = new byte[msgId.length + addRequest.length];
        System.arraycopy(msgId, 0, ldapMessage, 0, msgId.length);
        System.arraycopy(addRequest, 0, ldapMessage, msgId.length, addRequest.length);
        
        return encodeSequence((byte)0x30, ldapMessage);
    }

    private static byte[] createSearchRequest(String filter) {
        byte[] baseDN = encodeOctetString(BASE_DN.getBytes());
        byte[] scope = {0x0a, 0x01, 0x02};
        byte[] deref = {0x0a, 0x01, 0x00};
        byte[] sizeLimit = {0x02, 0x01, 0x00};
        byte[] timeLimit = {0x02, 0x01, 0x00};
        byte[] typesOnly = {0x01, 0x01, 0x00};
        
        String attr = filter.substring(filter.indexOf('(')+1, filter.indexOf('='));
        String value = filter.substring(filter.indexOf('=')+1, filter.indexOf(')'));
        byte[] filterBytes = encodeEqualityFilter(attr, value);
        
        byte[][] attrs = {
            encodeOctetString("cn".getBytes()),
            encodeOctetString("telephoneNumber".getBytes())
        };
        ByteArrayOutputStream attrStream = new ByteArrayOutputStream();
        for (byte[] attrBytes : attrs) {
            attrStream.write(attrBytes, 0, attrBytes.length);
        }
        byte[] attributes = encodeSequence((byte)0x30, attrStream.toByteArray());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(baseDN);
            baos.write(scope);
            baos.write(deref);
            baos.write(sizeLimit);
            baos.write(timeLimit);
            baos.write(typesOnly);
            baos.write(filterBytes);
            baos.write(attributes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        byte[] searchRequest = encodeSequence((byte)0x63, baos.toByteArray());
        byte[] msgId = encodeInteger(messageId++);
        byte[] ldapMessage = new byte[msgId.length + searchRequest.length];
        System.arraycopy(msgId, 0, ldapMessage, 0, msgId.length);
        System.arraycopy(searchRequest, 0, ldapMessage, msgId.length, searchRequest.length);
        
        return encodeSequence((byte)0x30, ldapMessage);
    }

    private static byte[] createDeleteRequest(String cn) {
        String dn = "cn=" + cn + "," + BASE_DN;
        byte[] dnBytes = encodeOctetString(dn.getBytes());
        
        byte[] delRequest = encodeSequence((byte)0x4a, dnBytes);
        byte[] msgId = encodeInteger(messageId++);
        byte[] ldapMessage = new byte[msgId.length + delRequest.length];
        System.arraycopy(msgId, 0, ldapMessage, 0, msgId.length);
        System.arraycopy(delRequest, 0, ldapMessage, msgId.length, delRequest.length);
        
        return encodeSequence((byte)0x30, ldapMessage);
    }

    private static boolean parseBindResponse(byte[] response) {
        try {
            int pos = 2;
            pos += 2;
            if (response[pos++] != 0x61) return false;
            pos += 1;
            int resultCode = response[pos + 2] & 0xff;
            if (resultCode == 0) {
                System.out.println("Authenticated successfully");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error parsing bind response");
        }
        return false;
    }

    private static void parseAddResponse(byte[] response) {
        try {
            int pos = 2;
            pos += 2;
            if (response[pos++] != 0x69) {
                System.out.println("Not an add response");
                return;
            }
            pos += 1;
            int resultCode = response[pos + 2] & 0xff;
            if (resultCode == 0) {
                System.out.println("Friend added successfully");
            } else {
                System.out.println("Failed to add friend (code: " + resultCode + ")");
            }
        } catch (Exception e) {
            System.out.println("Error parsing add response");
        }
    }

    private static void parseSearchResponse(byte[] response) {
        try {
            int pos = 2;
            pos += 2;
            int op = response[pos++] & 0xff;
            
            if (op == 0x65) {
                pos += 1;
                int dnLength = response[pos++] & 0xff;
                String dn = new String(response, pos, dnLength);
                pos += dnLength;
                
                pos += 2;
                pos += 2;
                int attrLength = response[pos++] & 0xff;
                String attrType = new String(response, pos, attrLength);
                pos += attrLength;
                
                pos += 2;
                int valLength = response[pos++] & 0xff;
                String value = new String(response, pos, valLength);
                
                System.out.println("\nFound friend:");
                System.out.println("Name: " + dn.substring(3, dn.indexOf(',')));
                System.out.println("Phone: " + value);
                
                response = readFullResponse(in);
                parseSearchResponse(response);
            } 
            else if (op == 0x66) {
                pos += 2;
                int resultCode = response[pos++] & 0xff;
                if (resultCode == 0) {
                    System.out.println("No matching entries found");
                } else {
                    System.out.println("Search failed with code: " + resultCode);
                }
            }
            else {
                System.out.println("Unknown operation: 0x" + Integer.toHexString(op));
            }
        } catch (Exception e) {
            System.out.println("Error parsing search response: " + e.getMessage());
        }
    }

    private static void parseDeleteResponse(byte[] response) {
        try {
            int pos = 2;
            pos += 2;
            if (response[pos++] != 0x6b) {
                System.out.println("Not a delete response");
                return;
            }
            pos += 1;
            int resultCode = response[pos + 2] & 0xff;
            if (resultCode == 0) {
                System.out.println("Friend deleted successfully");
            } else {
                System.out.println("Failed to delete friend (code: " + resultCode + ")");
            }
        } catch (Exception e) {
            System.out.println("Error parsing delete response");
        }
    }

    private static byte[] readFullResponse(DataInputStream in) throws IOException {
        int tag = in.readByte() & 0xff;
        if (tag != 0x30) throw new IOException("Invalid LDAP message");
        int length = readBerLength(in);
        byte[] response = new byte[length];
        in.readFully(response);
        
        byte[] fullResponse = new byte[2 + length];
        fullResponse[0] = (byte)tag;
        fullResponse[1] = (byte)length;
        System.arraycopy(response, 0, fullResponse, 2, length);
        return fullResponse;
    }

    private static int readBerLength(DataInputStream in) throws IOException {
        int lengthByte = in.readByte() & 0xff;
        if ((lengthByte & 0x80) == 0) return lengthByte;
        int numBytes = lengthByte & 0x7f;
        int length = 0;
        for (int i = 0; i < numBytes; i++) {
            length = (length << 8) | (in.readByte() & 0xff);
        }
        return length;
    }

    private static byte[] encodeSequence(byte tag, byte[] content) {
        byte[] sequence = new byte[2 + content.length];
        sequence[0] = tag;
        sequence[1] = (byte)content.length;
        System.arraycopy(content, 0, sequence, 2, content.length);
        return sequence;
    }

    private static byte[] encodeOctetString(byte[] content) {
        byte[] octetString = new byte[2 + content.length];
        octetString[0] = 0x04;
        octetString[1] = (byte)content.length;
        System.arraycopy(content, 0, octetString, 2, content.length);
        return octetString;
    }

    private static byte[] encodeSimpleAuth(byte[] password) {
        byte[] auth = new byte[2 + password.length];
        auth[0] = (byte)0x80;
        auth[1] = (byte)password.length;
        System.arraycopy(password, 0, auth, 2, password.length);
        return auth;
    }

    private static byte[] encodeInteger(int value) {
        byte[] intBytes = ByteBuffer.allocate(4).putInt(value).array();
        int start = 0;
        while (start < 3 && intBytes[start] == 0) start++;
        byte[] integer = new byte[2 + (4 - start)];
        integer[0] = 0x02;
        integer[1] = (byte)(4 - start);
        System.arraycopy(intBytes, start, integer, 2, 4 - start);
        return integer;
    }

    private static byte[] encodeAttribute(String attr, String value) {
        byte[] attrBytes = encodeOctetString(attr.getBytes());
        byte[] valueBytes = encodeOctetString(value.getBytes());
        byte[] attrValue = new byte[attrBytes.length + valueBytes.length];
        System.arraycopy(attrBytes, 0, attrValue, 0, attrBytes.length);
        System.arraycopy(valueBytes, 0, attrValue, attrBytes.length, valueBytes.length);
        return encodeSequence((byte)0x30, attrValue);
    }

    private static byte[] encodeEqualityFilter(String attr, String value) {
        byte[] attrBytes = encodeOctetString(attr.getBytes());
        byte[] valueBytes = encodeOctetString(value.getBytes());
        byte[] elements = new byte[attrBytes.length + valueBytes.length];
        System.arraycopy(attrBytes, 0, elements, 0, attrBytes.length);
        System.arraycopy(valueBytes, 0, elements, attrBytes.length, valueBytes.length);
        return encodeSequence((byte)0xa3, elements);
    }

    private static void disconnect() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}