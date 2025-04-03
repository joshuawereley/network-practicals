import java.io.*;
import java.net.*;
import java.util.Scanner;

public class LDAPClient {
    private static final int LDAP_PORT = 389;
    private static final String SERVER = "localhost";
    private static final String ADMIN_DN = "cn=admin,dc=example,dc=com";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String BASE_DN = "ou=Friends,dc=example,dc=com";
    private static final byte LDAP_SUCCESS = 0x00;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenu();
            int choice = getMenuChoice(scanner);

            switch (choice) {
                case 1:
                    searchFriend(scanner);
                    break;
                case 2:
                    addFriend(scanner);
                    break;
                case 3:
                    modifyPhoneNumber(scanner);
                    break;
                case 4:
                    deleteFriend(scanner);
                    break;
                case 5:
                    listAllFriends();
                    break;
                case 6:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            // Pause for user input before showing the menu again
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== LDAP Client Menu ===");
        System.out.println("1. Search Friend");
        System.out.println("2. Add Friend");
        System.out.println("3. Modify Phone Number");
        System.out.println("4. Delete Friend");
        System.out.println("5. List All Friends");
        System.out.println("6. Exit");
        System.out.print("Enter your choice (1-6): ");
    }

    private static int getMenuChoice(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number (1-6): ");
            }
        }
    }

    private static void searchFriend(Scanner scanner) {
        System.out.print("\nEnter friend's name to search: ");
        String friendName = scanner.nextLine();

        try (Socket socket = new Socket(SERVER, LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            // Send bind request
            byte[] bindRequest = createBindRequest();
            os.write(bindRequest);

            // Read bind response
            byte[] bindResponse = readResponse(is);
            if (!checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            // Send search request
            byte[] searchRequest = createSearchRequest(friendName);
            os.write(searchRequest);

            // Read search response
            byte[] searchResponse = readResponse(is);

            // Parse and display results
            String phoneNumber = parseSearchResponse(searchResponse);
            if (phoneNumber != null) {
                System.out.println("\nSearch Result:");
                System.out.println("Name: " + friendName);
                System.out.println("Phone: " + phoneNumber);
            } else {
                System.out.println("No friend found with name: " + friendName);
            }

        } catch (Exception e) {
            System.err.println("Error searching friend: " + e.getMessage());
        }
    }

    private static void addFriend(Scanner scanner) {
        System.out.println("\nAdd New Friend");
        System.out.print("Enter friend's full name: ");
        String name = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();
        System.out.print("Enter email address: ");
        String email = scanner.nextLine();

        try (Socket socket = new Socket(SERVER, LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            // Send bind request
            byte[] bindRequest = createBindRequest();
            os.write(bindRequest);

            // Read bind response
            byte[] bindResponse = readResponse(is);
            if (!checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            // Send add request
            byte[] addRequest = createAddRequest(name, phone, email);
            os.write(addRequest);

            // Read add response
            byte[] addResponse = readResponse(is);

            if (checkOperationResponse(addResponse)) {
                System.out.println("Friend added successfully!");
            } else {
                System.out.println("Failed to add friend");
            }

        } catch (Exception e) {
            System.err.println("Error adding friend: " + e.getMessage());
        }
    }

    private static void modifyPhoneNumber(Scanner scanner) {
        System.out.println("\nModify Phone Number");
        System.out.print("Enter friend's name to modify: ");
        String name = scanner.nextLine();
        System.out.print("Enter new phone number: ");
        String newPhone = scanner.nextLine();

        try (Socket socket = new Socket(SERVER, LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            // Send bind request
            byte[] bindRequest = createBindRequest();
            os.write(bindRequest);

            // Read bind response
            byte[] bindResponse = readResponse(is);
            if (!checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            // Send modify request
            byte[] modifyRequest = createModifyRequest(name, newPhone);
            os.write(modifyRequest);

            // Read modify response
            byte[] modifyResponse = readResponse(is);

            if (checkOperationResponse(modifyResponse)) {
                System.out.println("Phone number updated successfully!");
            } else {
                System.out.println("Failed to update phone number");
            }

        } catch (Exception e) {
            System.err.println("Error modifying phone number: " + e.getMessage());
        }
    }

    private static void deleteFriend(Scanner scanner) {
        System.out.println("\nDelete Friend");
        System.out.print("Enter friend's name to delete: ");
        String name = scanner.nextLine();

        try (Socket socket = new Socket(SERVER, LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            // Send bind request
            byte[] bindRequest = createBindRequest();
            os.write(bindRequest);

            // Read bind response
            byte[] bindResponse = readResponse(is);
            if (!checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            // Send delete request
            byte[] deleteRequest = createDeleteRequest(name);
            os.write(deleteRequest);

            // Read delete response
            byte[] deleteResponse = readResponse(is);

            if (checkOperationResponse(deleteResponse)) {
                System.out.println("Friend deleted successfully!");
            } else {
                System.out.println("Failed to delete friend");
            }

        } catch (Exception e) {
            System.err.println("Error deleting friend: " + e.getMessage());
        }
    }

    private static void listAllFriends() {
        try (Socket socket = new Socket(SERVER, LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            // Send bind request
            byte[] bindRequest = createBindRequest();
            os.write(bindRequest);

            // Read bind response
            byte[] bindResponse = readResponse(is);
            if (!checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            // Send search request for all friends
            byte[] searchRequest = createSearchRequest("*");
            os.write(searchRequest);

            // Read search response
            byte[] searchResponse = readResponse(is);

            // Parse and display results
            System.out.println("\nList of All Friends:");
            parseAndDisplayAllFriends(searchResponse);

        } catch (Exception e) {
            System.err.println("Error listing friends: " + e.getMessage());
        }
    }

    private static byte[] createBindRequest() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] dnBytes = ADMIN_DN.getBytes("UTF-8");
            byte[] pwdBytes = ADMIN_PASSWORD.getBytes("UTF-8");

            // LDAP message sequence
            baos.write(0x30); // SEQUENCE
            int bindLength = 1 + 1 + 3 + // messageID
                    1 + 1 + 3 + // version
                    1 + 1 + dnBytes.length + // name
                    1 + 1 + pwdBytes.length; // password

            baos.write(bindLength);

            // Message ID
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x01); // value (message ID 1)

            // Bind Request
            baos.write(0x60); // CHOICE [0] BindRequest
            baos.write(bindLength - 5); // -5 for message ID

            // Version
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x03); // value (LDAP v3)

            // Name (admin DN)
            baos.write(0x04); // OCTET STRING
            baos.write(dnBytes.length);
            baos.write(dnBytes);

            // Password
            baos.write(0x80); // [0] simple authentication
            baos.write(pwdBytes.length);
            baos.write(pwdBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] createSearchRequest(String friendName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] baseDnBytes = BASE_DN.getBytes("UTF-8");
            byte[] filterBytes = createFilter(friendName);

            // Calculate total length
            int totalLength = 1 + 1 + 3 + // message ID
                    1 + 1 + (1 + 1 + baseDnBytes.length + // baseObject
                            1 + 1 + 3 + // scope
                            1 + 1 + 3 + // derefAliases
                            1 + 1 + 2 + // sizeLimit
                            1 + 1 + 2 + // timeLimit
                            1 + 1 + 1 + // typesOnly
                            1 + 1 + filterBytes.length); // filter

            // LDAP message sequence
            baos.write(0x30); // SEQUENCE
            writeLength(baos, totalLength);

            // Message ID
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x02); // value (message ID 2)

            // Search Request
            baos.write(0x63); // CHOICE [3] SearchRequest
            writeLength(baos, totalLength - 3);

            // BaseObject
            baos.write(0x04); // OCTET STRING
            writeLength(baos, baseDnBytes.length);
            baos.write(baseDnBytes);

            // Scope (whole subtree)
            baos.write(0x0A); // ENUMERATED
            baos.write(0x01); // length
            baos.write(0x02); // wholeSubtree (2)

            // DerefAliases (never deref)
            baos.write(0x0A); // ENUMERATED
            baos.write(0x01); // length
            baos.write(0x00); // neverDerefAliases (0)

            // SizeLimit (no limit)
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x00); // 0 (no limit)

            // TimeLimit (no limit)
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x00); // 0 (no limit)

            // TypesOnly (false)
            baos.write(0x01); // BOOLEAN
            baos.write(0x01); // length
            baos.write(0x00); // false

            // Filter
            baos.write(filterBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] createFilter(String friendName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (friendName.equals("*")) {
            // Present filter (always true)
            baos.write(0x87); // [7] present
            baos.write(0x00); // length 0
        } else {
            // Equality match filter
            byte[] cnBytes = "cn".getBytes("UTF-8");
            byte[] nameBytes = friendName.getBytes("UTF-8");

            baos.write(0xA3); // [3] equalityMatch
            writeLength(baos, 1 + 1 + cnBytes.length + 1 + 1 + nameBytes.length);

            // Attribute type
            baos.write(0x04); // OCTET STRING
            writeLength(baos, cnBytes.length);
            baos.write(cnBytes);

            // Attribute value
            baos.write(0x04); // OCTET STRING
            writeLength(baos, nameBytes.length);
            baos.write(nameBytes);
        }

        return baos.toByteArray();
    }

    private static byte[] createAddRequest(String name, String phone, String email) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // DN for new entry
            String dn = "cn=" + name + "," + BASE_DN;
            byte[] dnBytes = dn.getBytes("UTF-8");

            // Attribute sets
            byte[] cnBytes = name.getBytes("UTF-8");
            byte[] phoneBytes = phone.getBytes("UTF-8");
            byte[] mailBytes = email.getBytes("UTF-8");
            byte[] objectClassBytes = "inetOrgPerson".getBytes("UTF-8");
            byte[] snBytes = name.split(" ").length > 0 ? name.split(" ")[1].getBytes("UTF-8") : " ".getBytes("UTF-8");

            // Calculate attribute sequence length
            int attributesLength = 1 + 1 + (1 + 1 + objectClassBytes.length) + // objectClass
                    1 + 1 + (1 + 1 + cnBytes.length) + // cn
                    1 + 1 + (1 + 1 + snBytes.length) + // sn
                    1 + 1 + (1 + 1 + phoneBytes.length) + // telephoneNumber
                    1 + 1 + (1 + 1 + mailBytes.length); // mail

            // Calculate total length
            int totalLength = 1 + 1 + 3 + // message ID
                    1 + 1 + (1 + 1 + dnBytes.length + attributesLength); // add request

            // LDAP message sequence
            baos.write(0x30); // SEQUENCE
            writeLength(baos, totalLength);

            // Message ID
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x03); // value (message ID 3)

            // Add Request
            baos.write(0x68); // CHOICE [8] AddRequest
            writeLength(baos, 1 + 1 + dnBytes.length + attributesLength);

            // Entry DN
            baos.write(0x04); // OCTET STRING
            writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            // Attributes sequence
            baos.write(0x30); // SEQUENCE
            writeLength(baos, attributesLength);

            // objectClass attribute
            addAttribute(baos, "objectClass", new byte[][] { objectClassBytes });

            // cn attribute
            addAttribute(baos, "cn", new byte[][] { cnBytes });

            // sn attribute
            addAttribute(baos, "sn", new byte[][] { snBytes });

            // telephoneNumber attribute
            addAttribute(baos, "telephoneNumber", new byte[][] { phoneBytes });

            // mail attribute
            addAttribute(baos, "mail", new byte[][] { mailBytes });

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addAttribute(ByteArrayOutputStream baos, String attrName, byte[][] values) throws IOException {
        byte[] attrNameBytes = attrName.getBytes("UTF-8");

        // Calculate values length
        int valuesLength = 0;
        for (byte[] value : values) {
            valuesLength += 1 + 1 + value.length;
        }

        // Attribute sequence
        baos.write(0x30); // SEQUENCE
        writeLength(baos, 1 + 1 + attrNameBytes.length + valuesLength);

        // Attribute type
        baos.write(0x04); // OCTET STRING
        writeLength(baos, attrNameBytes.length);
        baos.write(attrNameBytes);

        // Attribute values set
        baos.write(0x31); // SET
        writeLength(baos, valuesLength);

        // Values
        for (byte[] value : values) {
            baos.write(0x04); // OCTET STRING
            writeLength(baos, value.length);
            baos.write(value);
        }
    }

    private static byte[] createModifyRequest(String name, String newPhone) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // DN of entry to modify
            String dn = "cn=" + name + "," + BASE_DN;
            byte[] dnBytes = dn.getBytes("UTF-8");
            byte[] phoneBytes = newPhone.getBytes("UTF-8");

            // Calculate modification sequence length
            int modificationLength = 1 + 1 + (1 + 1 + 1 + 1 + phoneBytes.length);

            // Calculate total length
            int totalLength = 1 + 1 + 3 + // message ID
                    1 + 1 + (1 + 1 + dnBytes.length + 1 + 1 + modificationLength); // modify request

            // LDAP message sequence
            baos.write(0x30); // SEQUENCE
            writeLength(baos, totalLength);

            // Message ID
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x04); // value (message ID 4)

            // Modify Request
            baos.write(0x66); // CHOICE [6] ModifyRequest
            writeLength(baos, 1 + 1 + dnBytes.length + 1 + 1 + modificationLength);

            // Entry DN
            baos.write(0x04); // OCTET STRING
            writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            // Modifications sequence
            baos.write(0x30); // SEQUENCE
            writeLength(baos, modificationLength);

            // Modification operation
            baos.write(0x30); // SEQUENCE
            writeLength(baos, 1 + 1 + 1 + 1 + phoneBytes.length);

            // Operation (replace)
            baos.write(0x0A); // ENUMERATED
            baos.write(0x01); // length
            baos.write(0x02); // replace (2)

            // Modification attribute
            baos.write(0x30); // SEQUENCE
            writeLength(baos, 1 + 1 + phoneBytes.length);

            // Attribute type
            baos.write(0x04); // OCTET STRING
            baos.write(0x0E); // length
            baos.write("telephoneNumber".getBytes("UTF-8"));

            // Attribute values
            baos.write(0x31); // SET
            writeLength(baos, 1 + 1 + phoneBytes.length);

            // Value
            baos.write(0x04); // OCTET STRING
            writeLength(baos, phoneBytes.length);
            baos.write(phoneBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] createDeleteRequest(String name) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // DN of entry to delete
            String dn = "cn=" + name + "," + BASE_DN;
            byte[] dnBytes = dn.getBytes("UTF-8");

            // Calculate total length
            int totalLength = 1 + 1 + 3 + // message ID
                    1 + 1 + (1 + 1 + dnBytes.length); // delete request

            // LDAP message sequence
            baos.write(0x30); // SEQUENCE
            writeLength(baos, totalLength);

            // Message ID
            baos.write(0x02); // INTEGER
            baos.write(0x01); // length
            baos.write(0x05); // value (message ID 5)

            // Delete Request
            baos.write(0x4A); // CHOICE [10] DelRequest
            writeLength(baos, 1 + 1 + dnBytes.length);

            // Entry DN
            baos.write(0x04); // OCTET STRING
            writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        // Read the first few bytes to get the length
        byte[] lengthBytes = new byte[2];
        int actuallyRead = is.read(lengthBytes, 0, 2);
        if (actuallyRead != 2) {
            throw new IOException("Failed to read response header");
        }
        baos.write(lengthBytes);

        // Get the full length of the response
        int length = getLength(lengthBytes, 1);
        int remaining = length - 2;

        // Read the remaining bytes
        while (remaining > 0) {
            bytesRead = is.read(buffer, 0, Math.min(buffer.length, remaining));
            if (bytesRead == -1) {
                throw new IOException("Unexpected end of stream");
            }
            baos.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }

        return baos.toByteArray();
    }

    private static boolean checkBindResponse(byte[] response) {
        try {
            // Look for result code sequence (0x0A0100 for success)
            for (int i = 0; i < response.length - 3; i++) {
                if (response[i] == 0x0A && response[i + 1] == 0x01) {
                    byte resultCode = response[i + 2];
                    if (resultCode == LDAP_SUCCESS) {
                        return true;
                    } else {
                        System.err.println("LDAP error: " + getLDAPErrorDescription(resultCode));
                        return false;
                    }
                }
            }
            System.err.println("Invalid bind response format");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String parseSearchResponse(byte[] response) {
        try {
            // Look for telephoneNumber attribute in the response
            for (int i = 0; i < response.length - 4; i++) {
                // Look for attribute type (04) followed by telephoneNumber
                if (response[i] == 0x04 && i + 12 < response.length) {
                    String attrName = new String(response, i + 2, response[i + 1], "UTF-8");
                    if (attrName.equals("telephoneNumber")) {
                        // Find the value (skip ahead to next 04)
                        for (int j = i + 2 + response[i + 1]; j < response.length - 2; j++) {
                            if (response[j] == 0x04) {
                                int len = response[j + 1] & 0xFF;
                                if (j + 2 + len <= response.length) {
                                    return new String(response, j + 2, len, "UTF-8");
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean checkOperationResponse(byte[] response) {
        try {
            int pos = 0;

            // Check message envelope
            if (response[pos++] != 0x30)
                return false; // SEQUENCE
            pos += getLengthLength(response[pos]) + getLength(response, pos);

            // Check result code
            if (response[pos] == 0x0A) { // ENUMERATED (result code)
                pos++;
                if (response[pos++] == 0x01) { // length
                    return response[pos] == 0x00; // success (0)
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void parseAndDisplayAllFriends(byte[] response) {
        try {
            int pos = 0;

            // Skip message envelope
            if (response[pos++] != 0x30)
                return; // SEQUENCE
            pos += getLengthLength(response[pos]) + getLength(response, pos);

            // Process search result entries
            while (pos < response.length) {
                if (response[pos] == 0x04) { // SearchResultEntry
                    pos++;
                    int entryLength = response[pos++];
                    int entryEnd = pos + entryLength;

                    // Skip DN
                    pos += 2 + response[pos + 1];

                    // Process attributes
                    while (pos < entryEnd) {
                        if (response[pos] == 0x30) { // SEQUENCE
                            pos++;
                            int attrLength = response[pos++];
                            int attrEnd = pos + attrLength;

                            // Get attribute type
                            if (response[pos] == 0x04) {
                                pos++;
                                int typeLength = response[pos++];
                                String attrType = new String(response, pos, typeLength, "UTF-8");
                                pos += typeLength;

                                // Get attribute values
                                if (attrType.equals("cn") || attrType.equals("telephoneNumber")) {
                                    if (response[pos] == 0x31) { // SET
                                        pos++;
                                        int setLength = response[pos++];
                                        int setEnd = pos + setLength;

                                        while (pos < setEnd) {
                                            if (response[pos] == 0x04) { // OCTET STRING
                                                pos++;
                                                int valueLength = response[pos++];
                                                String value = new String(response, pos, valueLength, "UTF-8");
                                                pos += valueLength;

                                                if (attrType.equals("cn")) {
                                                    System.out.print("Name: " + value);
                                                } else {
                                                    System.out.println(", Phone: " + value);
                                                }
                                            } else {
                                                pos++;
                                            }
                                        }
                                    }
                                } else {
                                    pos++;
                                }
                            }
                        } else {
                            pos++;
                        }
                    }
                } else {
                    pos++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeLength(ByteArrayOutputStream baos, int length) {
        if (length < 128) {
            // Short form
            baos.write(length);
        } else {
            // Long form
            int numBytes = 1;
            if (length > 0xFF)
                numBytes++;
            if (length > 0xFFFF)
                numBytes++;
            if (length > 0xFFFFFF)
                numBytes++;

            baos.write(0x80 | numBytes);
            for (int i = numBytes - 1; i >= 0; i--) {
                baos.write((length >> (8 * i)) & 0xFF);
            }
        }
    }

    private static int getLength(byte[] data, int pos) {
        int firstByte = data[pos] & 0xFF;
        if ((firstByte & 0x80) == 0) {
            return firstByte;
        } else {
            int numBytes = firstByte & 0x7F;
            int length = 0;
            for (int i = 1; i <= numBytes; i++) {
                length = (length << 8) | (data[pos + i] & 0xFF);
            }
            return length;
        }
    }

    private static int getLengthLength(byte firstLengthByte) {
        return ((firstLengthByte & 0x80) == 0) ? 1 : (1 + (firstLengthByte & 0x7F));
    }

    private static String getLDAPErrorDescription(byte errorCode) {
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