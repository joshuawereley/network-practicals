package service;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import model.LDAPConstants;
import model.LDAPResponseParser;
import util.BERUtils;

public class LDAPClientService {
    private LDAPRequestBuilder requestBuilder = new LDAPRequestBuilder();

    public void searchFriend(Scanner scanner) {
        System.out.print("\nEnter friend's name to search: ");
        String friendName = scanner.nextLine();

        try (Socket socket = new Socket(LDAPConstants.SERVER, LDAPConstants.LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            os.write(requestBuilder.createBindRequest());
            byte[] bindResponse = readResponse(is);
            if (!LDAPResponseParser.checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            os.write(requestBuilder.createSearchRequest(friendName));
            byte[] searchResponse = readResponse(is);
            String phoneNumber = LDAPResponseParser.parseSearchResponse(searchResponse);

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

    public void addFriend(Scanner scanner) {
        System.out.println("\nAdd New Friend");
        System.out.print("Enter friend's full name: ");
        String name = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine();
        System.out.print("Enter email address: ");
        String email = scanner.nextLine();

        try (Socket socket = new Socket(LDAPConstants.SERVER, LDAPConstants.LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            os.write(requestBuilder.createBindRequest());
            byte[] bindResponse = readResponse(is);
            if (!LDAPResponseParser.checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            os.write(requestBuilder.createAddRequest(name, phone, email));
            byte[] addResponse = readResponse(is);

            if (LDAPResponseParser.checkOperationResponse(addResponse)) {
                System.out.println("Friend added successfully!");
            } else {
                System.out.println("Failed to add friend");
            }
        } catch (Exception e) {
            System.err.println("Error adding friend: " + e.getMessage());
        }
    }

    public void modifyPhoneNumber(Scanner scanner) {
        System.out.println("\nModify Phone Number");
        System.out.print("Enter friend's name to modify: ");
        String name = scanner.nextLine();
        System.out.print("Enter new phone number: ");
        String newPhone = scanner.nextLine();

        try (Socket socket = new Socket(LDAPConstants.SERVER, LDAPConstants.LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            os.write(requestBuilder.createBindRequest());
            byte[] bindResponse = readResponse(is);
            if (!LDAPResponseParser.checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            os.write(requestBuilder.createModifyRequest(name, newPhone));
            byte[] modifyResponse = readResponse(is);

            if (LDAPResponseParser.checkOperationResponse(modifyResponse)) {
                System.out.println("Phone number updated successfully!");
            } else {
                System.out.println("Failed to update phone number");
            }
        } catch (Exception e) {
            System.err.println("Error modifying phone number: " + e.getMessage());
        }
    }

    public void deleteFriend(Scanner scanner) {
        System.out.println("\nDelete Friend");
        System.out.print("Enter friend's name to delete: ");
        String name = scanner.nextLine();

        try (Socket socket = new Socket(LDAPConstants.SERVER, LDAPConstants.LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            os.write(requestBuilder.createBindRequest());
            byte[] bindResponse = readResponse(is);
            if (!LDAPResponseParser.checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            os.write(requestBuilder.createDeleteRequest(name));
            byte[] deleteResponse = readResponse(is);

            if (LDAPResponseParser.checkOperationResponse(deleteResponse)) {
                System.out.println("Friend deleted successfully!");
            } else {
                System.out.println("Failed to delete friend");
            }
        } catch (Exception e) {
            System.err.println("Error deleting friend: " + e.getMessage());
        }
    }

    public void listAllFriends() {
        try (Socket socket = new Socket(LDAPConstants.SERVER, LDAPConstants.LDAP_PORT);
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream()) {

            os.write(requestBuilder.createBindRequest());
            byte[] bindResponse = readResponse(is);
            if (!LDAPResponseParser.checkBindResponse(bindResponse)) {
                System.err.println("Authentication failed");
                return;
            }

            os.write(requestBuilder.createSearchRequest("*"));
            byte[] searchResponse = readResponse(is);

            System.out.println("\nList of All Friends:");
            LDAPResponseParser.parseAndDisplayAllFriends(searchResponse);
        } catch (Exception e) {
            System.err.println("Error listing friends: " + e.getMessage());
        }
    }

    private byte[] readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        byte[] lengthBytes = new byte[2];
        int actuallyRead = is.read(lengthBytes, 0, 2);
        if (actuallyRead != 2) {
            throw new IOException("Failed to read response header");
        }
        baos.write(lengthBytes);

        int length = BERUtils.getLength(lengthBytes, 1);
        int remaining = length - 2;

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
}