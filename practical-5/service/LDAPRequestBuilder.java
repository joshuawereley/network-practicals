package service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import model.LDAPConstants;
import util.BERUtils;

public class LDAPRequestBuilder {
    public byte[] createBindRequest() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] dnBytes = LDAPConstants.ADMIN_DN.getBytes("UTF-8");
            byte[] pwdBytes = LDAPConstants.ADMIN_PASSWORD.getBytes("UTF-8");

            int bindLength = 1 + 1 + 3 + 1 + 1 + 3 + 1 + 1 + dnBytes.length + 1 + 1 + pwdBytes.length;

            baos.write(0x30);
            BERUtils.writeLength(baos, bindLength);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x01);

            baos.write(0x60);
            BERUtils.writeLength(baos, bindLength - 5);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x03);

            baos.write(0x04);
            BERUtils.writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            baos.write(0x80);
            BERUtils.writeLength(baos, pwdBytes.length);
            baos.write(pwdBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] createSearchRequest(String friendName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] baseDnBytes = LDAPConstants.BASE_DN.getBytes("UTF-8");
            byte[] filterBytes = createFilter(friendName);

            int totalLength = 1 + 1 + 3 + 1 + 1 + (1 + 1 + baseDnBytes.length +
                    1 + 1 + 3 + 1 + 1 + 3 + 1 + 1 + 2 + 1 + 1 + 2 +
                    1 + 1 + 1 + 1 + 1 + filterBytes.length);

            baos.write(0x30);
            BERUtils.writeLength(baos, totalLength);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x02);

            baos.write(0x63);
            BERUtils.writeLength(baos, totalLength - 3);

            baos.write(0x04);
            BERUtils.writeLength(baos, baseDnBytes.length);
            baos.write(baseDnBytes);

            baos.write(0x0A);
            baos.write(0x01);
            baos.write(0x02);

            baos.write(0x0A);
            baos.write(0x01);
            baos.write(0x00);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x00);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x00);

            baos.write(0x01);
            baos.write(0x01);
            baos.write(0x00);

            baos.write(filterBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] createAddRequest(String name, String phone, String email) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String dn = "cn=" + name + "," + LDAPConstants.BASE_DN;
            byte[] dnBytes = dn.getBytes("UTF-8");
            byte[] cnBytes = name.getBytes("UTF-8");
            byte[] phoneBytes = phone.getBytes("UTF-8");
            byte[] mailBytes = email.getBytes("UTF-8");
            byte[] objectClassBytes = "inetOrgPerson".getBytes("UTF-8");
            byte[] snBytes = name.split(" ").length > 0 ? name.split(" ")[1].getBytes("UTF-8") : " ".getBytes("UTF-8");

            int attributesLength = 1 + 1 + (1 + 1 + objectClassBytes.length) +
                    1 + 1 + (1 + 1 + cnBytes.length) +
                    1 + 1 + (1 + 1 + snBytes.length) +
                    1 + 1 + (1 + 1 + phoneBytes.length) +
                    1 + 1 + (1 + 1 + mailBytes.length);

            int totalLength = 1 + 1 + 3 + 1 + 1 + (1 + 1 + dnBytes.length + attributesLength);

            baos.write(0x30);
            BERUtils.writeLength(baos, totalLength);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x03);

            baos.write(0x68);
            BERUtils.writeLength(baos, 1 + 1 + dnBytes.length + attributesLength);

            baos.write(0x04);
            BERUtils.writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            baos.write(0x30);
            BERUtils.writeLength(baos, attributesLength);

            addAttribute(baos, "objectClass", new byte[][] { objectClassBytes });
            addAttribute(baos, "cn", new byte[][] { cnBytes });
            addAttribute(baos, "sn", new byte[][] { snBytes });
            addAttribute(baos, "telephoneNumber", new byte[][] { phoneBytes });
            addAttribute(baos, "mail", new byte[][] { mailBytes });

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] createModifyRequest(String name, String newPhone) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String dn = "cn=" + name + "," + LDAPConstants.BASE_DN;
            byte[] dnBytes = dn.getBytes("UTF-8");
            byte[] phoneBytes = newPhone.getBytes("UTF-8");

            int modificationLength = 1 + 1 + (1 + 1 + 1 + 1 + phoneBytes.length);
            int totalLength = 1 + 1 + 3 + 1 + 1 + (1 + 1 + dnBytes.length + 1 + 1 + modificationLength);

            baos.write(0x30);
            BERUtils.writeLength(baos, totalLength);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x04);

            baos.write(0x66);
            BERUtils.writeLength(baos, 1 + 1 + dnBytes.length + 1 + 1 + modificationLength);

            baos.write(0x04);
            BERUtils.writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            baos.write(0x30);
            BERUtils.writeLength(baos, modificationLength);

            baos.write(0x30);
            BERUtils.writeLength(baos, 1 + 1 + 1 + 1 + phoneBytes.length);

            baos.write(0x0A);
            baos.write(0x01);
            baos.write(0x02);

            baos.write(0x30);
            BERUtils.writeLength(baos, 1 + 1 + phoneBytes.length);

            baos.write(0x04);
            baos.write(0x0E);
            baos.write("telephoneNumber".getBytes("UTF-8"));

            baos.write(0x31);
            BERUtils.writeLength(baos, 1 + 1 + phoneBytes.length);

            baos.write(0x04);
            BERUtils.writeLength(baos, phoneBytes.length);
            baos.write(phoneBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] createDeleteRequest(String name) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String dn = "cn=" + name + "," + LDAPConstants.BASE_DN;
            byte[] dnBytes = dn.getBytes("UTF-8");

            int totalLength = 1 + 1 + 3 + 1 + 1 + (1 + 1 + dnBytes.length);

            baos.write(0x30);
            BERUtils.writeLength(baos, totalLength);

            baos.write(0x02);
            baos.write(0x01);
            baos.write(0x05);

            baos.write(0x4A);
            BERUtils.writeLength(baos, 1 + 1 + dnBytes.length);

            baos.write(0x04);
            BERUtils.writeLength(baos, dnBytes.length);
            baos.write(dnBytes);

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] createFilter(String friendName) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (friendName.equals("*")) {
            baos.write(0x87);
            baos.write(0x00);
        } else {
            byte[] cnBytes = "cn".getBytes("UTF-8");
            byte[] nameBytes = friendName.getBytes("UTF-8");

            baos.write(0xA3);
            BERUtils.writeLength(baos, 1 + 1 + cnBytes.length + 1 + 1 + nameBytes.length);

            baos.write(0x04);
            BERUtils.writeLength(baos, cnBytes.length);
            baos.write(cnBytes);

            baos.write(0x04);
            BERUtils.writeLength(baos, nameBytes.length);
            baos.write(nameBytes);
        }

        return baos.toByteArray();
    }

    private void addAttribute(ByteArrayOutputStream baos, String attrName, byte[][] values) throws IOException {
        byte[] attrNameBytes = attrName.getBytes("UTF-8");
        int valuesLength = 0;
        for (byte[] value : values) {
            valuesLength += 1 + 1 + value.length;
        }

        baos.write(0x30);
        BERUtils.writeLength(baos, 1 + 1 + attrNameBytes.length + valuesLength);

        baos.write(0x04);
        BERUtils.writeLength(baos, attrNameBytes.length);
        baos.write(attrNameBytes);

        baos.write(0x31);
        BERUtils.writeLength(baos, valuesLength);

        for (byte[] value : values) {
            baos.write(0x04);
            BERUtils.writeLength(baos, value.length);
            baos.write(value);
        }
    }
}