package model;

import util.BERUtils;

public class LDAPResponseParser {
    public static boolean checkBindResponse(byte[] response) {
        try {
            for (int i = 0; i < response.length - 3; i++) {
                if (response[i] == 0x0A && response[i + 1] == 0x01) {
                    byte resultCode = response[i + 2];
                    if (resultCode == LDAPConstants.LDAP_SUCCESS) {
                        return true;
                    } else {
                        System.err.println("LDAP error: " + LDAPConstants.getLDAPErrorDescription(resultCode));
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

    public static String parseSearchResponse(byte[] response) {
        try {
            for (int i = 0; i < response.length - 4; i++) {
                if (response[i] == 0x04 && i + 12 < response.length) {
                    String attrName = new String(response, i + 2, response[i + 1], "UTF-8");
                    if (attrName.equals("telephoneNumber")) {
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

    public static boolean checkOperationResponse(byte[] response) {
        try {
            int pos = 0;
            if (response[pos++] != 0x30)
                return false;
            pos += BERUtils.getLengthLength(response[pos]) + BERUtils.getLength(response, pos);

            if (response[pos] == 0x0A) {
                pos++;
                if (response[pos++] == 0x01) {
                    return response[pos] == 0x00;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void parseAndDisplayAllFriends(byte[] response) {
        try {
            int pos = 0;
            if (response[pos++] != 0x30)
                return;
            pos += BERUtils.getLengthLength(response[pos]) + BERUtils.getLength(response, pos);

            while (pos < response.length) {
                if (response[pos] == 0x04) {
                    pos++;
                    int entryLength = response[pos++];
                    int entryEnd = pos + entryLength;
                    pos += 2 + response[pos + 1];

                    while (pos < entryEnd) {
                        if (response[pos] == 0x30) {
                            pos++;
                            int attrLength = response[pos++];
                            int attrEnd = pos + attrLength;

                            if (response[pos] == 0x04) {
                                pos++;
                                int typeLength = response[pos++];
                                String attrType = new String(response, pos, typeLength, "UTF-8");
                                pos += typeLength;

                                if (attrType.equals("cn") || attrType.equals("telephoneNumber")) {
                                    if (response[pos] == 0x31) {
                                        pos++;
                                        int setLength = response[pos++];
                                        int setEnd = pos + setLength;

                                        while (pos < setEnd) {
                                            if (response[pos] == 0x04) {
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
}