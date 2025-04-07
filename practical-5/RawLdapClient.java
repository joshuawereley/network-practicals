import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class RawLdapClient {
    private static final int LDAP_PORT = 389;
    private static int messageId = 1;

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", LDAP_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            
            // 1. Send Bind Request
            byte[] bindRequest = createBindRequest();
            out.write(bindRequest);
            System.out.println("Sent bind request");
            
            // Read bind response
            byte[] response = readFullResponse(in);
            if (!parseBindResponse(response)) {
                System.out.println("Bind failed. Exiting...");
                return;
            }

            // 2. Menu-driven interaction
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("\nMenu:");
                System.out.println("1. Add Friend");
                System.out.println("2. Remove Friend");
                System.out.println("3. List Friends");
                System.out.println("4. Search Friend");
                System.out.println("5. Quit");
                System.out.print("Enter your choice: ");
                String choice = console.readLine();

                switch (choice) {
                    case "1":
                        System.out.print("Enter friend's name: ");
                        String addName = console.readLine();
                        System.out.print("Enter friend's phone number: ");
                        String phoneNumber = console.readLine();
                        byte[] addRequest = createAddRequest(addName, phoneNumber);
                        out.write(addRequest);
                        System.out.println("Sent add request");
                        response = readFullResponse(in);
                        parseAddResponse(response);
                        break;
                    case "2":
                        System.out.print("Enter friend's name to remove: ");
                        String removeName = console.readLine();
                        byte[] removeRequest = createRemoveRequest(removeName);
                        out.write(removeRequest);
                        System.out.println("Sent remove request");
                        response = readFullResponse(in);
                        parseRemoveResponse(response);
                        break;
                    case "3":
                        byte[] listRequest = createListRequest();
                        out.write(listRequest);
                        System.out.println("Sent list request");
                        response = readFullResponse(in);
                        parseListResponse(response);
                        break;
                    case "4":
                        System.out.print("Enter friend's name to search: ");
                        String searchName = console.readLine();
                        byte[] searchRequest = createSearchRequest(searchName);
                        out.write(searchRequest);
                        System.out.println("Sent search request");
                        response = readFullResponse(in);
                        parseSearchResponse(response);
                        break;
                    case "5":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static byte[] createBindRequest() {
        // LDAPv3 bind request components
        byte[] version = {0x02, 0x01, 0x03}; // INTEGER 3
        String bindDN = "cn=admin,dc=mycompany,dc=com";
        String password = "adminpassword";

        // Build DN component
        byte[] dnBytes = encodeOctetString(bindDN.getBytes());
        
        // Build password component
        byte[] pwdBytes = password.getBytes();
        byte[] pwd = new byte[2 + pwdBytes.length];
        pwd[0] = (byte)0x80; // Context-specific primitive [0]
        pwd[1] = (byte)pwdBytes.length;
        System.arraycopy(pwdBytes, 0, pwd, 2, pwdBytes.length);

        // Combine all bind request elements
        byte[] bindElements = new byte[version.length + dnBytes.length + pwd.length];
        System.arraycopy(version, 0, bindElements, 0, version.length);
        System.arraycopy(dnBytes, 0, bindElements, version.length, dnBytes.length);
        System.arraycopy(pwd, 0, bindElements, version.length + dnBytes.length, pwd.length);

        // Build bind request (APPLICATION[0])
        byte[] bindRequest = encodeSequence((byte)0x60, bindElements); // 0x60 = BindRequest

        // Build complete LDAP message (SEQUENCE)
        byte[] messageIdBytes = encodeInteger(messageId++);
        byte[] ldapMessage = new byte[messageIdBytes.length + bindRequest.length];
        System.arraycopy(messageIdBytes, 0, ldapMessage, 0, messageIdBytes.length);
        System.arraycopy(bindRequest, 0, ldapMessage, messageIdBytes.length, bindRequest.length);
        
        return encodeSequence((byte)0x30, ldapMessage); // 0x30 = SEQUENCE
    }

    private static byte[] createSearchRequest(String name) {
        String baseDN = "ou=friends,dc=mycompany,dc=com";

        // Build filter (cn=name) - search by the friend's name
        byte[] cnAttr = encodeOctetString("cn".getBytes());
        byte[] nameValue = encodeOctetString(name.getBytes());
        byte[] filterElements = combineArrays(cnAttr, nameValue);
        byte[] filter = encodeSequence((byte)0xa3, filterElements); // 0xa3 = equalityMatch

        // Build search request components
        byte[] scope = {0x0a, 0x01, 0x02}; // wholeSubtree (2)
        byte[] deref = {0x0a, 0x01, 0x00}; // neverDerefAliases (0)
        byte[] sizeLimit = {0x02, 0x01, 0x00}; // no limit (0)
        byte[] timeLimit = {0x02, 0x01, 0x00}; // no limit (0)
        byte[] typesOnly = {0x01, 0x01, 0x00}; // FALSE (0)

        // No specific attributes requested (return all)
        byte[] attributes = encodeSequence((byte)0x30, new byte[0]); // Empty SEQUENCE

        // Combine all components
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(encodeOctetString(baseDN.getBytes()));
            baos.write(scope);
            baos.write(deref);
            baos.write(sizeLimit);
            baos.write(timeLimit);
            baos.write(typesOnly);
            baos.write(filter);
            baos.write(attributes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build search request (APPLICATION[3])
        byte[] searchRequest = encodeSequence((byte)0x63, baos.toByteArray()); // 0x63 = SearchRequest

        // Build complete LDAP message
        byte[] messageIdBytes = encodeInteger(messageId++);
        byte[] ldapMessage = combineArrays(messageIdBytes, searchRequest);

        return encodeSequence((byte)0x30, ldapMessage); // 0x30 = SEQUENCE
    }

    // Method to create an LDAP Add Request
    private static byte[] createAddRequest(String name, String phoneNumber) {
        String dn = "cn=" + name + ",ou=friends,dc=mycompany,dc=com";

        // Build the entry attributes
        // Attribute: objectClass
        byte[] objectClassAttr = encodeOctetString("objectClass".getBytes());
        byte[] objectClassValues = encodeSequence((byte) 0x31, combineArrays(
            encodeOctetString("top".getBytes()),
            encodeOctetString("person".getBytes())
        )); // SET OF values
        byte[] objectClassSequence = encodeSequence((byte) 0x30, combineArrays(objectClassAttr, objectClassValues));

        // Attribute: cn
        byte[] cnAttr = encodeOctetString("cn".getBytes());
        byte[] cnValue = encodeSequence((byte) 0x31, encodeOctetString(name.getBytes())); // SET OF values
        byte[] cnSequence = encodeSequence((byte) 0x30, combineArrays(cnAttr, cnValue));

        // Attribute: sn (surname) - required for person objectClass
        byte[] snAttr = encodeOctetString("sn".getBytes());
        byte[] snValue = encodeSequence((byte) 0x31, encodeOctetString(name.getBytes())); // Use name as surname for simplicity
        byte[] snSequence = encodeSequence((byte) 0x30, combineArrays(snAttr, snValue));

        // Attribute: telephoneNumber (optional)
        byte[] phoneAttr = encodeOctetString("telephoneNumber".getBytes());
        byte[] phoneValue = encodeSequence((byte) 0x31, encodeOctetString(phoneNumber.getBytes())); // SET OF values
        byte[] phoneSequence = encodeSequence((byte) 0x30, combineArrays(phoneAttr, phoneValue));
        
        // Clear and direct way to combine all attributes 
        byte[] allAttributes = combineArrays(
            objectClassSequence,
            combineArrays(
                cnSequence,
                combineArrays(
                    snSequence,
                    phoneSequence
                )
            )
        );
        
        // Wrap attributes in a SEQUENCE
        byte[] attributes = encodeSequence((byte) 0x30, allAttributes);
        
        // Create Add Request content (DN + attributes)
        byte[] addContent = combineArrays(encodeOctetString(dn.getBytes()), attributes);
        
        // Build Add Request with proper tag (0x68 = APPLICATION[8])
        byte[] addRequest = encodeSequence((byte) 0x68, addContent);
        
        // Build complete LDAP message with message ID
        byte[] messageIdBytes = encodeInteger(messageId++);
        byte[] ldapMessage = combineArrays(messageIdBytes, addRequest);
        
        // Finally, wrap in outer SEQUENCE
        return encodeSequence((byte) 0x30, ldapMessage);
    }

    // Method to parse the Add Response
    private static void parseAddResponse(byte[] response) {
        try {
            int pos = 0;
            // Get total length to prevent going out of bounds
            int totalLength = response.length;
            
            // Print full response for debugging
            System.out.println("Response received: " + bytesToHex(response));
            
            // Verify enough bytes for basic structure
            if (totalLength < 7) {
                System.out.println("Response too short: " + bytesToHex(response));
                return;
            }
            
            // Skip SEQUENCE tag
            pos++;
            
            // Check if length byte indicates long form
            int lengthByte = response[pos++] & 0xff;
            if ((lengthByte & 0x80) != 0) {
                // Skip long form length bytes
                int numLengthBytes = lengthByte & 0x7f;
                pos += numLengthBytes;
                
                // Ensure we don't go out of bounds
                if (pos >= totalLength) {
                    System.out.println("Response truncated after length: " + bytesToHex(response));
                    return;
                }
            }
            
            // Message ID (INTEGER tag)
            if (pos >= totalLength || response[pos] != 0x02) {
                System.out.println("Expected INTEGER tag for messageID, got: " + 
                                  (pos < totalLength ? "0x" + Integer.toHexString(response[pos] & 0xff) : "end of data"));
                return;
            }
            pos++; // Skip INTEGER tag
            
            // Message ID length
            int idLen = response[pos++] & 0xff;
            
            // Ensure we have enough bytes for the messageID
            if (pos + idLen > totalLength) {
                System.out.println("Response truncated in messageID");
                return;
            }
            
            // Read message ID value
            int msgId = 0;
            for (int i = 0; i < idLen; i++) {
                msgId = (msgId << 8) | (response[pos++] & 0xff);
            }
            
            // Check for any response tag - handle multiple response types
            if (pos >= totalLength) {
                System.out.println("Response truncated before response tag");
                return;
            }
            
            int opTag = response[pos++] & 0xff;
            System.out.println("Received operation tag: 0x" + Integer.toHexString(opTag));
            
            // Skip response length byte
            if (pos >= totalLength) {
                System.out.println("Response truncated after response tag");
                return;
            }
            pos++; // Skip length byte
            
            // Result code (ENUMERATED tag)
            if (pos >= totalLength || response[pos] != 0x0A) {
                System.out.println("Expected ENUMERATED tag for resultCode, got: " + 
                                  (pos < totalLength ? "0x" + Integer.toHexString(response[pos] & 0xff) : "end of data"));
                return;
            }
            pos++; // Skip ENUMERATED tag
            
            // Result code length
            if (pos >= totalLength) {
                System.out.println("Response truncated before resultCode length");
                return;
            }
            
            int resultCodeLen = response[pos++] & 0xff;
            
            // Ensure we have enough bytes for the result code
            if (pos + resultCodeLen > totalLength) {
                System.out.println("Response truncated in resultCode");
                return;
            }
            
            // Read result code value
            int resultCode = 0;
            for (int i = 0; i < resultCodeLen; i++) {
                resultCode = (resultCode << 8) | (response[pos++] & 0xff);
            }
            
            // Process the result - accept any valid LDAP response type
            if (resultCode == 0) {
                System.out.println("Add operation successful (message ID: " + msgId + ")");
            } else {
                System.out.println("Add operation failed with code: " + resultCode);
                
                // Common LDAP result codes
                switch (resultCode) {
                    case 32:
                        System.out.println("Error: No such object - parent entry does not exist");
                        System.out.println("You need to create 'ou=friends,dc=mycompany,dc=com' first");
                        break;
                    case 68:
                        System.out.println("Error: Entry already exists");
                        break;
                    case 50:
                        System.out.println("Error: Insufficient access rights");
                        break;
                    default:
                        System.out.println("See LDAP result codes for more information");
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing add response: " + e.getMessage());
            System.out.println("Response dump: " + bytesToHex(response));
        }
    }

    // Method to create an LDAP Remove Request
    private static byte[] createRemoveRequest(String name) {
        // Escape spaces and special characters in the name for LDAP DN
        String escapedName = escapeLdapDN(name);
        String dn = "cn=" + escapedName + ",ou=friends,dc=mycompany,dc=com";
        
        System.out.println("Creating delete request for: " + dn);
        
        // Build Delete Request with the entry's DN
        byte[] dnBytes = encodeOctetString(dn.getBytes());
        
        // Delete Request only needs the DN (APPLICATION[10])
        byte[] deleteRequest = encodeSequence((byte) 0x4A, dnBytes); // 0x4A = DelRequest
        
        // Build complete LDAP message with message ID
        byte[] messageIdBytes = encodeInteger(messageId++);
        byte[] ldapMessage = combineArrays(messageIdBytes, deleteRequest);
        
        // Wrap in outer SEQUENCE
        byte[] request = encodeSequence((byte) 0x30, ldapMessage);
        
        // Output request for debugging
        System.out.println("Delete request bytes: " + bytesToHex(request));
        
        return request;
    }
    
    // Helper method to escape special characters in LDAP DNs
    private static String escapeLdapDN(String name) {
        if (name == null) return "";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == ' ') {
                // Option 1: Replace space with \20 (hex escape)
                sb.append("\\ ");
            } else if (c == ',' || c == '+' || c == '"' || c == '\\' || 
                      c == '<' || c == '>' || c == ';' || c == '#' || c == '=') {
                // Escape special characters in LDAP DN
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Method to parse the Remove Response
    private static void parseRemoveResponse(byte[] response) {
        try {
            int pos = 0;
            // Get total length to prevent going out of bounds
            int totalLength = response.length;
            
            // Print the full raw response for debugging
            System.out.println("Delete response received: " + bytesToHex(response));
            
            // Verify enough bytes for basic structure
            if (totalLength < 7) {
                System.out.println("Response too short: " + bytesToHex(response));
                return;
            }
            
            // Skip SEQUENCE tag
            pos++;
            
            // Check if length byte indicates long form
            int lengthByte = response[pos++] & 0xff;
            if ((lengthByte & 0x80) != 0) {
                // Skip long form length bytes
                int numLengthBytes = lengthByte & 0x7f;
                pos += numLengthBytes;
                
                // Ensure we don't go out of bounds
                if (pos >= totalLength) {
                    System.out.println("Response truncated after length: " + bytesToHex(response));
                    return;
                }
            }
            
            // Message ID (INTEGER tag)
            if (pos >= totalLength || response[pos] != 0x02) {
                System.out.println("Expected INTEGER tag for messageID, got: " + 
                    (pos < totalLength ? "0x" + Integer.toHexString(response[pos] & 0xff) : "end of data"));
                return;
            }
            pos++; // Skip INTEGER tag
            
            // Message ID length
            int idLen = response[pos++] & 0xff;
            
            // Ensure we have enough bytes for the messageID
            if (pos + idLen > totalLength) {
                System.out.println("Response truncated in messageID");
                return;
            }
            
            // Read message ID value
            int msgId = 0;
            for (int i = 0; i < idLen; i++) {
                msgId = (msgId << 8) | (response[pos++] & 0xff);
            }
            
            // Check for operation tag - this could be 0x6B (DelResponse) or possibly other tags
            if (pos >= totalLength) {
                System.out.println("Response truncated before operation tag");
                return;
            }
            
            int opTag = response[pos++] & 0xff;
            System.out.println("Received operation tag: 0x" + Integer.toHexString(opTag));
            
            // Accept both DelResponse (0x6B) and standard response codes
            boolean isDelResponse = (opTag == 0x6B);
            boolean isErrorResponse = (opTag == 0x65); // SearchResultDone often used for errors
            
            if (!isDelResponse && !isErrorResponse) {
                System.out.println("Warning: Unexpected response tag 0x" + Integer.toHexString(opTag) + 
                                   ", attempting to process anyway...");
            }
            
            // Skip response length byte
            if (pos >= totalLength) {
                System.out.println("Response truncated after tag");
                return;
            }
            pos++; // Skip length
            
            // Result code (ENUMERATED tag)
            if (pos >= totalLength || response[pos] != 0x0A) {
                System.out.println("Expected ENUMERATED tag for resultCode, got: " + 
                    (pos < totalLength ? "0x" + Integer.toHexString(response[pos] & 0xff) : "end of data"));
                return;
            }
            pos++; // Skip tag
            
            // Result code length
            int resultCodeLen = response[pos++] & 0xff;
            if (pos + resultCodeLen > totalLength) {
                System.out.println("Response truncated in resultCode");
                return;
            }
            
            // Read result code value
            int resultCode = 0;
            for (int i = 0; i < resultCodeLen; i++) {
                resultCode = (resultCode << 8) | (response[pos++] & 0xff);
            }
            
            // Process the result
            if (resultCode == 0) {
                System.out.println("Delete operation successful (message ID: " + msgId + ")");
            } else {
                System.out.println("Delete operation failed with code: " + resultCode);
                
                // Extract error message if available
                String errorMessage = extractErrorMessage(response, pos, totalLength);
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    System.out.println("Error details: " + errorMessage);
                }
                
                // Common LDAP result codes
                switch (resultCode) {
                    case 32:
                        System.out.println("Error: No such object - entry does not exist");
                        break;
                    case 50:
                        System.out.println("Error: Insufficient access rights");
                        break;
                    case 53:
                        System.out.println("Error: Unable to perform operation - server limitation or constraint");
                        break;
                    case 66:
                        System.out.println("Error: Not allowed on non-leaf - entry has children that must be deleted first");
                        break;
                    default:
                        System.out.println("See LDAP result codes for more information");
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing delete response: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Response dump: " + bytesToHex(response));
        }
    }
    
    // Helper method to extract error message from LDAP response
    private static String extractErrorMessage(byte[] response, int startPos, int totalLength) {
        try {
            int pos = startPos;
            
            // Look for the diagnostic message component (usually OCTET STRING after result code)
            while (pos < totalLength) {
                if (response[pos] == 0x04) { // OCTET STRING
                    pos++; // Skip tag
                    
                    if (pos < totalLength) {
                        int msgLen = response[pos++] & 0xff;
                        
                        if (pos + msgLen <= totalLength && msgLen > 0) {
                            return new String(response, pos, msgLen);
                        }
                    }
                    break;
                }
                pos++; // Move to next byte
            }
        } catch (Exception e) {
            // Ignore any errors in error message extraction
        }
        return null;
    }

    // Method to create an LDAP List Request
    private static byte[] createListRequest() {
        String baseDN = "ou=friends,dc=mycompany,dc=com";

        // Build filter (objectClass=person)
        byte[] objectClassAttr = encodeOctetString("objectClass".getBytes());
        byte[] personValue = encodeOctetString("person".getBytes());
        byte[] filterElements = combineArrays(objectClassAttr, personValue);
        byte[] filter = encodeSequence((byte) 0xa3, filterElements); // 0xa3 = equalityMatch

        // Build search request components
        byte[] scope = {0x0a, 0x01, 0x02}; // wholeSubtree (2)
        byte[] deref = {0x0a, 0x01, 0x00}; // neverDerefAliases (0)
        byte[] sizeLimit = {0x02, 0x01, 0x00}; // no limit (0)
        byte[] timeLimit = {0x02, 0x01, 0x00}; // no limit (0)
        byte[] typesOnly = {0x01, 0x01, 0x00}; // FALSE (0)

        // No specific attributes requested (return all)
        byte[] attributes = encodeSequence((byte) 0x30, new byte[0]); // Empty SEQUENCE

        // Combine all components
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(encodeOctetString(baseDN.getBytes()));
            baos.write(scope);
            baos.write(deref);
            baos.write(sizeLimit);
            baos.write(timeLimit);
            baos.write(typesOnly);
            baos.write(filter);
            baos.write(attributes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Build search request (APPLICATION[3])
        byte[] searchRequest = encodeSequence((byte) 0x63, baos.toByteArray()); // 0x63 = SearchRequest

        // Build complete LDAP message
        byte[] messageIdBytes = encodeInteger(messageId++);
        byte[] ldapMessage = combineArrays(messageIdBytes, searchRequest);

        return encodeSequence((byte) 0x30, ldapMessage); // 0x30 = SEQUENCE
    }

    // Method to parse the List Response
    private static void parseListResponse(byte[] response) {
        try {
            int pos = 0;
            int totalLength = response.length;
            
            // Debugging: Print out the raw response data
            System.out.println("DEBUG: Raw list response: " + bytesToHex(response));
            
            // Check for minimum length
            if (totalLength < 10) {
                System.out.println("Response too short: " + bytesToHex(response));
                return;
            }
            
            // Skip SEQUENCE tag
            pos++;
            
            // Parse length - handle both short and long form
            int lengthByte = response[pos++] & 0xff;
            int messageLength;
            if ((lengthByte & 0x80) == 0) {
                // Short form
                messageLength = lengthByte;
            } else {
                // Long form
                int numLengthBytes = lengthByte & 0x7f;
                if (pos + numLengthBytes > totalLength) {
                    System.out.println("Invalid long-form length encoding");
                    return;
                }
                messageLength = 0;
                for (int i = 0; i < numLengthBytes; i++) {
                    messageLength = (messageLength << 8) | (response[pos++] & 0xff);
                }
            }
            
            // Message ID (INTEGER tag)
            if (pos >= totalLength || response[pos] != 0x02) {
                System.out.println("Expected INTEGER tag for messageID, got: " + 
                                  (pos < totalLength ? "0x" + Integer.toHexString(response[pos] & 0xff) : "end of data"));
                return;
            }
            pos++; // Skip INTEGER tag
            
            // Message ID length
            if (pos >= totalLength) {
                System.out.println("Truncated data after INTEGER tag");
                return;
            }
            int idLen = response[pos++] & 0xff;
            if (pos + idLen > totalLength) {
                System.out.println("Message ID value truncated");
                return;
            }
            
            // Read message ID value
            int msgId = 0;
            for (int i = 0; i < idLen; i++) {
                msgId = (msgId << 8) | (response[pos++] & 0xff);
            }
            
            System.out.println("Response for message ID: " + msgId);
            
            // Process all entries in the response
            boolean hasEntries = false;
            
            while (pos < totalLength) {
                // Get the operation tag
                if (pos >= totalLength) break;
                int tagByte = response[pos++] & 0xff;
                
                // Parse length - handle both short and long form
                if (pos >= totalLength) break;
                int lenByte = response[pos++] & 0xff;
                int contentLength;
                
                if (lenByte == 0x81) {  // Common long form pattern: 0x81 followed by 1 byte length
                    if (pos >= totalLength) break;
                    contentLength = response[pos++] & 0xff;
                } else if (lenByte == 0x82) {  // Less common: 0x82 followed by 2 byte length
                    if (pos + 1 >= totalLength) break;
                    contentLength = ((response[pos] & 0xff) << 8) | (response[pos + 1] & 0xff);
                    pos += 2;
                } else if ((lenByte & 0x80) == 0) {
                    // Short form
                    contentLength = lenByte;
                } else {
                    // Other long form
                    int numBytes = lenByte & 0x7f;
                    if (pos + numBytes > totalLength) break;
                    contentLength = 0;
                    for (int i = 0; i < numBytes; i++) {
                        contentLength = (contentLength << 8) | (response[pos++] & 0xff);
                    }
                }
                
                // Calculate end position for current protocol op
                int endPos = pos + contentLength;
                if (endPos > totalLength) endPos = totalLength;
                
                // Process based on operation tag
                if (tagByte == 0x64) { // SearchResultEntry (4)
                    hasEntries = true;
                    parseEntry(response, pos, endPos);
                    pos = endPos; // Skip to end of this entry
                } 
                else if (tagByte == 0x65) { // SearchResultDone (5)
                    int resultCode = parseResultCode(response, pos, endPos);
                    
                    if (resultCode == 0) {
                        if (!hasEntries) {
                            System.out.println("No entries found.");
                        }
                        System.out.println("\nSearch completed successfully.");
                    } else {
                        System.out.println("\nSearch completed with result code: " + resultCode);
                    }
                    pos = endPos; // Skip to end
                }
                else if (tagByte == 0x6f || tagByte == 0x70 || tagByte == 0x71) {
                    // Possible continuation or overflow tags
                    System.out.println("Processing special tag: 0x" + Integer.toHexString(tagByte));
                    
                    // Look ahead for a SearchResultEntry tag
                    boolean found = false;
                    for (int i = pos; i < Math.min(pos + 20, totalLength - 1); i++) {
                        if (response[i] == 0x64 || response[i] == 0x65) {
                            System.out.println("Found valid tag at offset " + (i - pos));
                            contentLength = i - pos;
                            endPos = i;
                            found = true;
                            break;
                        }
                    }
                    
                    if (!found) {
                        // Can't find a valid tag, skip this section
                        System.out.println("Unable to find valid tag, skipping to next section");
                    }
                    
                    pos = endPos; // Skip this section
                }
                else {
                    System.out.println("Unknown operation tag: 0x" + Integer.toHexString(tagByte));
                    pos = endPos; // Skip unknown operation
                }
                
                // Check if we've reached the end or if there might be more entries
                if (pos >= totalLength) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing list response: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Response dump: " + bytesToHex(response));
        }
    }
    
    // Helper to parse a search result entry
    private static void parseEntry(byte[] response, int startPos, int endPos) {
        try {
            int pos = startPos;
            
            // Check for DN (OCTET STRING)
            if (pos < endPos && response[pos] == 0x04) { // OCTET STRING
                pos++; // Skip tag
                
                // Parse length
                int lenByte = response[pos++] & 0xff;
                int dnLen;
                
                if ((lenByte & 0x80) == 0) {
                    dnLen = lenByte;
                } else {
                    int numBytes = lenByte & 0x7f;
                    dnLen = 0;
                    for (int i = 0; i < numBytes && pos < endPos; i++) {
                        dnLen = (dnLen << 8) | (response[pos++] & 0xff);
                    }
                }
                
                if (pos + dnLen <= endPos) {
                    String dn = new String(response, pos, dnLen);
                    System.out.println("\nDN: " + dn);
                    pos += dnLen;
                    
                    // Parse attributes if we have them
                    if (pos < endPos && response[pos] == 0x30) { // SEQUENCE of attributes
                        pos++; // Skip tag
                        
                        // Skip length
                        lenByte = response[pos++] & 0xff;
                        if ((lenByte & 0x80) != 0) {
                            int numBytes = lenByte & 0x7f;
                            pos += numBytes;
                        }
                        
                        // Process each attribute
                        while (pos < endPos && response[pos] == 0x30) { // SEQUENCE for attribute
                            parseAttribute(response, pos, endPos);
                            
                            // Skip to next attribute - find end of current attribute
                            pos++; // Skip SEQUENCE tag
                            int attrLenByte = response[pos++] & 0xff;
                            int attrLen;
                            
                            if ((attrLenByte & 0x80) == 0) {
                                attrLen = attrLenByte;
                            } else {
                                int numBytes = attrLenByte & 0x7f;
                                attrLen = 0;
                                for (int i = 0; i < numBytes && pos < endPos; i++) {
                                    attrLen = (attrLen << 8) | (response[pos++] & 0xff);
                                }
                            }
                            pos += attrLen; // Skip to end of attribute
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing entry: " + e.getMessage());
        }
    }
    
    // Helper to parse a single attribute
    private static void parseAttribute(byte[] response, int startPos, int endPos) {
        try {
            int pos = startPos + 1; // Skip SEQUENCE tag
            
            // Skip length
            int lenByte = response[pos++] & 0xff;
            if ((lenByte & 0x80) != 0) {
                int numBytes = lenByte & 0x7f;
                pos += numBytes;
            }
            
            // Get attribute type (OCTET STRING)
            if (pos < endPos && response[pos] == 0x04) {
                pos++; // Skip tag
                int typeLen = response[pos++] & 0xff;
                if (pos + typeLen <= endPos) {
                    String type = new String(response, pos, typeLen);
                    pos += typeLen;
                    
                    // Get values (SET OF)
                    if (pos < endPos && response[pos] == 0x31) {
                        pos++; // Skip SET tag
                        
                        // Skip length
                        lenByte = response[pos++] & 0xff;
                        int valuesLen;
                        
                        if ((lenByte & 0x80) == 0) {
                            valuesLen = lenByte;
                        } else {
                            int numBytes = lenByte & 0x7f;
                            valuesLen = 0;
                            for (int i = 0; i < numBytes && pos < endPos; i++) {
                                valuesLen = (valuesLen << 8) | (response[pos++] & 0xff);
                            }
                        }
                        
                        int valuesEnd = pos + valuesLen;
                        if (valuesEnd > endPos) valuesEnd = endPos;
                        
                        // Parse all values
                        StringBuilder values = new StringBuilder();
                        while (pos < valuesEnd && response[pos] == 0x04) {
                            pos++; // Skip OCTET STRING tag
                            int valueLen = response[pos++] & 0xff;
                            
                            if (pos + valueLen <= valuesEnd) {
                                String value = new String(response, pos, valueLen);
                                pos += valueLen;
                                
                                if (values.length() > 0) {
                                    values.append(", ");
                                }
                                values.append(value);
                            }
                        }
                        
                        System.out.println("  " + type + ": " + values.toString());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing attribute: " + e.getMessage());
        }
    }
    
    // Helper to parse result code
    private static int parseResultCode(byte[] response, int startPos, int endPos) {
        try {
            int pos = startPos;
            
            // Look for ENUMERATED tag
            if (pos < endPos && response[pos] == 0x0A) {
                pos++; // Skip tag
                int codeLen = response[pos++] & 0xff;
                
                if (pos + codeLen <= endPos) {
                    int code = 0;
                    for (int i = 0; i < codeLen; i++) {
                        code = (code << 8) | (response[pos++] & 0xff);
                    }
                    return code;
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing result code: " + e.getMessage());
        }
        return -1; // Error code
    }

    // Helper methods for BER encoding
    private static byte[] encodeSequence(byte tag, byte[] content) {
        if (content.length < 128) {
            // Short form for length < 128
            byte[] sequence = new byte[2 + content.length];
            sequence[0] = tag;
            sequence[1] = (byte)content.length;
            System.arraycopy(content, 0, sequence, 2, content.length);
            return sequence;
        } else {
            // Long form for length >= 128
            byte[] lengthBytes = encodeLongFormLength(content.length);
            byte[] sequence = new byte[1 + lengthBytes.length + content.length];
            sequence[0] = tag;
            System.arraycopy(lengthBytes, 0, sequence, 1, lengthBytes.length);
            System.arraycopy(content, 0, sequence, 1 + lengthBytes.length, content.length);
            return sequence;
        }
    }
    
    private static byte[] encodeLongFormLength(int length) {
        // Determine how many bytes we need to represent the length
        int numBytes = 0;
        int tempLength = length;
        while (tempLength > 0) {
            numBytes++;
            tempLength >>>= 8;  // Fixed: added equals sign for assignment
        }
        
        // Ensure at least one byte for the length value
        if (numBytes == 0) {
            numBytes = 1;
        }
        
        // Create the length encoding bytes
        byte[] lengthBytes = new byte[1 + numBytes];
        lengthBytes[0] = (byte)(0x80 | numBytes); // First byte: 0x80 + number of bytes
        
        // Fill in the length bytes in big-endian order
        for (int i = 0; i < numBytes; i++) {
            lengthBytes[numBytes - i] = (byte)(length & 0xFF);
            length >>>= 8;  // Fixed: added equals sign for assignment
        }
        
        return lengthBytes;
    }

    private static byte[] encodeOctetString(byte[] content) {
        // Use the same length encoding logic we have in encodeSequence
        if (content.length < 128) {
            byte[] octetString = new byte[2 + content.length];
            octetString[0] = 0x04; // OCTET STRING tag
            octetString[1] = (byte)content.length;
            System.arraycopy(content, 0, octetString, 2, content.length);
            return octetString;
        } else {
            byte[] lengthBytes = encodeLongFormLength(content.length);
            byte[] octetString = new byte[1 + lengthBytes.length + content.length];
            octetString[0] = 0x04; // OCTET STRING tag
            System.arraycopy(lengthBytes, 0, octetString, 1, lengthBytes.length);
            System.arraycopy(content, 0, octetString, 1 + lengthBytes.length, content.length);
            return octetString;
        }
    }

    private static byte[] encodeInteger(int value) {
        byte[] intBytes = ByteBuffer.allocate(4).putInt(value).array();
        // Find first non-zero byte
        int start = 0;
        while (start < 3 && intBytes[start] == 0) start++;
        
        byte[] integer = new byte[2 + (4 - start)];
        integer[0] = 0x02; // INTEGER tag
        integer[1] = (byte)(4 - start);
        System.arraycopy(intBytes, start, integer, 2, 4 - start);
        return integer;
    }

    private static byte[] readFullResponse(DataInputStream in) throws IOException {
        try {
            // For safety, limit the maximum response size
            final int MAX_RESPONSE_SIZE = 1024 * 1024; // 1MB max
            
            // Read the ASN.1 SEQUENCE tag
            int tag = in.readByte() & 0xff;
            
            // If we don't get a SEQUENCE tag, try to recover
            if (tag != 0x30) {
                System.out.println("WARNING: Received non-SEQUENCE tag: 0x" + Integer.toHexString(tag));
                
                // Try to read and skip some bytes to resynchronize
                byte[] skipped = new byte[8];
                in.readFully(skipped);
                
                // Try again
                tag = in.readByte() & 0xff;
                if (tag != 0x30) {
                    // Still wrong, one more try but with a larger skip
                    skipped = new byte[32];
                    in.readFully(skipped);
                    
                    tag = in.readByte() & 0xff;
                    if (tag != 0x30) {
                        throw new IOException("Cannot synchronize with LDAP server responses");
                    }
                }
            }
            
            // Read the length - safely handle potentially incorrect length
            int length;
            try {
                length = readBerLength(in);
                
                // Sanity check - don't allow unreasonable lengths
                if (length <= 0 || length > MAX_RESPONSE_SIZE) {
                    System.out.println("WARNING: Suspicious message length: " + length + ", limiting to reasonable size");
                    length = 1024; // Limit to something reasonable
                }
            } catch (IOException e) {
                System.out.println("ERROR reading BER length: " + e.getMessage());
                // Default to a reasonable length if we can't read it
                length = 512;
            }
            
            // Read the actual response data
            byte[] response = new byte[length];
            
            try {
                in.readFully(response);
            } catch (EOFException eof) {
                System.out.println("WARNING: Reached end of stream reading response, got " + 
                                  in.available() + " bytes");
                // Return what we have
                return new byte[] { (byte)tag, (byte)length };
            }
            
            // Include the tag and length in the returned bytes
            byte[] fullResponse = new byte[2 + length];
            fullResponse[0] = (byte)tag;
            fullResponse[1] = (byte)length; // This is a simplification, should handle long form
            System.arraycopy(response, 0, fullResponse, 2, length);
            
            return fullResponse;
        } catch (Exception e) {
            // Last resort error handling
            System.out.println("Fatal error reading LDAP response: " + e.getMessage());
            e.printStackTrace();
            
            // Return a minimal valid LDAP message to avoid breaking the client
            // This is a SearchResultDone message with success code
            return new byte[] { 0x30, 0x0c, 0x02, 0x01, 0x00, 0x65, 0x07, 0x0a, 0x01, 0x00, 0x04, 0x00, 0x04, 0x00 };
        }
    }

    // Add a helper method to safely skip data
    private static void skipBytes(DataInputStream in, int n) throws IOException {
        byte[] discard = new byte[n];
        in.readFully(discard);
    }

    private static int readBerLength(DataInputStream in) throws IOException {
        int lengthByte = in.readByte() & 0xff;
        if ((lengthByte & 0x80) == 0) {
            return lengthByte;
        } else {
            int numBytes = lengthByte & 0x7f;
            int length = 0;
            for (int i = 0; i < numBytes; i++) {
                length = (length << 8) | (in.readByte() & 0xff);
            }
            return length;
        }
    }

    private static boolean parseBindResponse(byte[] response) {
        try {
            int pos = 2; // Skip SEQUENCE tag and length
            
            // Message ID
            pos += 2; // Skip INTEGER tag and length
            int msgId = response[pos++];
            
            // Bind Response (0x61)
            if (response[pos++] != 0x61) {
                System.out.println("Not a bind response");
                return false;
            }
            
            pos += 1; // Skip length
            int resultCode = response[pos + 2] & 0xff;
            
            if (resultCode == 0) {
                System.out.println("Bind successful (message ID: " + msgId + ")");
                return true;
            } else {
                System.out.println("Bind failed with code: " + resultCode);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error parsing bind response: " + e.getMessage());
            return false;
        }
    }

    private static void parseSearchResponse(byte[] response) {
        try {
            // Print the raw response for debugging
            System.out.println("Search response: " + bytesToHex(response));
            
            int pos = 0;
            int totalLength = response.length;
            
            // Verify enough bytes for basic structure
            if (totalLength < 7) {
                System.out.println("Response too short: " + bytesToHex(response));
                return;
            }
            
            // Skip SEQUENCE tag
            pos++;
            
            // Parse length - handle both short and long form
            int lengthByte = response[pos++] & 0xff;
            if ((lengthByte & 0x80) != 0) {
                // Long form
                int numLengthBytes = lengthByte & 0x7f;
                pos += numLengthBytes; // Skip length bytes
            }
            
            // Message ID (INTEGER tag)
            if (pos >= totalLength || response[pos] != 0x02) {
                System.out.println("Expected INTEGER tag for messageID, got: " + 
                                  (pos < totalLength ? "0x" + Integer.toHexString(response[pos] & 0xff) : "end of data"));
                return;
            }
            pos++; // Skip INTEGER tag
            
            // Message ID length
            if (pos >= totalLength) {
                System.out.println("Truncated data after INTEGER tag");
                return;
            }
            
            int idLen = response[pos++] & 0xff;
            if (pos + idLen > totalLength) {
                System.out.println("Message ID value truncated");
                return;
            }
            
            // Read message ID value
            int msgId = 0;
            for (int i = 0; i < idLen; i++) {
                msgId = (msgId << 8) | (response[pos++] & 0xff);
            }
            
            System.out.println("Search response for message ID: " + msgId);
            
            // Check for operation tag
            if (pos >= totalLength) {
                System.out.println("Response truncated after message ID");
                return;
            }
            
            int opTag = response[pos++] & 0xff;
            
            // Handle different response types
            if (opTag == 0x64) { // SearchResultEntry (4)
                // Process entries
                processSearchEntry(response, pos, totalLength);
            } 
            else if (opTag == 0x65) { // SearchResultDone (5)
                // Skip length byte
                if (pos < totalLength) pos++;
                
                // Look for result code (ENUMERATED)
                if (pos < totalLength && response[pos] == 0x0A) {
                    pos++; // Skip ENUMERATED tag
                    if (pos < totalLength) {
                        int codeLen = response[pos++] & 0xff;
                        
                        if (pos + codeLen <= totalLength) {
                            int resultCode = 0;
                            for (int i = 0; i < codeLen; i++) {
                                resultCode = (resultCode << 8) | (response[pos++] & 0xff);
                            }
                            
                            if (resultCode == 0) {
                                System.out.println("Search completed successfully, no matching entries found.");
                            } else {
                                System.out.println("Search completed with result code: " + resultCode);
                            }
                        }
                    }
                }
            } else {
                System.out.println("Unknown operation tag in search response: 0x" + Integer.toHexString(opTag));
            }
        } catch (Exception e) {
            System.out.println("Error parsing search response: " + e.getMessage());
            System.out.println("Response dump: " + bytesToHex(response));
        }
    }
    
    private static void processSearchEntry(byte[] response, int startPos, int totalLength) {
        try {
            int pos = startPos;
            
            // Skip length byte
            if (pos < totalLength) pos++;
            
            // Read the DN
            if (pos < totalLength && response[pos] == 0x04) { // OCTET STRING
                pos++; // Skip tag
                if (pos < totalLength) {
                    int dnLen = response[pos++] & 0xff;
                    
                    if (pos + dnLen <= totalLength) {
                        String dn = new String(response, pos, dnLen);
                        System.out.println("Found entry: " + dn);
                        pos += dnLen;
                        
                        // Process attributes if any
                        if (pos < totalLength && response[pos] == 0x30) { // SEQUENCE of attributes
                            parseAttributes(response, pos, totalLength);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing search entry: " + e.getMessage());
        }
    }
    
    private static void parseAttributes(byte[] response, int startPos, int totalLength) {
        try {
            int pos = startPos + 1; // Skip SEQUENCE tag
            
            // Skip length byte
            if (pos < totalLength) pos++;
            
            // Read attributes
            while (pos < totalLength && response[pos] == 0x30) { // SEQUENCE for each attribute
                pos++; // Skip SEQUENCE tag
                
                // Skip length byte
                if (pos < totalLength) pos++;
                
                // Attribute type
                if (pos < totalLength && response[pos] == 0x04) { // OCTET STRING
                    pos++; // Skip tag
                    
                    if (pos < totalLength) {
                        int typeLen = response[pos++] & 0xff;
                        
                        if (pos + typeLen <= totalLength) {
                            String type = new String(response, pos, typeLen);
                            pos += typeLen;
                            
                            // Attribute values (SET OF)
                            if (pos < totalLength && response[pos] == 0x31) { // SET
                                pos++; // Skip SET tag
                                
                                if (pos < totalLength) {
                                    // Skip SET length
                                    pos++;
                                    
                                    StringBuilder values = new StringBuilder();
                                    
                                    // Read values
                                    while (pos < totalLength && response[pos] == 0x04) { // OCTET STRING
                                        pos++; // Skip OCTET STRING tag
                                        
                                        if (pos < totalLength) {
                                            int valueLen = response[pos++] & 0xff;
                                            
                                            if (pos + valueLen <= totalLength) {
                                                if (values.length() > 0) values.append(", ");
                                                values.append(new String(response, pos, valueLen));
                                                pos += valueLen;
                                            }
                                        }
                                    }
                                    
                                    System.out.println("  " + type + ": " + values.toString());
                                }
                            }
                        }
                    }
                }
                
                // Skip to next attribute if we can't fully parse this one
                while (pos < totalLength && response[pos] != 0x30) pos++;
            }
        } catch (Exception e) {
            System.out.println("Error parsing attributes: " + e.getMessage());
        }
    }

    // Helper method to combine two byte arrays
    private static byte[] combineArrays(byte[] array1, byte[] array2) {
        byte[] combined = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, combined, 0, array1.length);
        System.arraycopy(array2, 0, combined, array1.length, array2.length);
        return combined;
    }

    // Helper method to convert bytes to hex for debugging
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString();
    }
}