package pt.tecnico.sec.hdlt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class FileUtils {

    public static ArrayList<User> parseGridUsers(String gridFileLocation) throws IOException, ParseException {
        if(gridFileLocation == null){
            throw new IOException(); //TODO dizer que não foi definido a localização
        }

        FileReader fr = new FileReader(gridFileLocation);

        Object obj = new JSONParser().parse(fr);
        JSONArray grid = (JSONArray) obj;

        ArrayList<User> users = new ArrayList<>();

        for (Object userObject: grid) {
            JSONObject userJson = (JSONObject) userObject;

            User user = new User(
                    Integer.parseInt(userJson.get("userId").toString()),
                    userJson.get("ip").toString(),
                    Integer.parseInt(userJson.get("port").toString())
            );

            JSONArray positionsJson = (JSONArray) userJson.get("positions");
            ArrayList<Position> positions = new ArrayList<>();
            for (Object positionObject: positionsJson) {
                JSONObject positionJson = (JSONObject) positionObject;

                Position position = new Position(
                        Long.parseLong(positionJson.get("epoch").toString()),
                        Long.parseLong(positionJson.get("xPos").toString()),
                        Long.parseLong(positionJson.get("yPos").toString())
                );

                JSONArray closeByUsersJson = (JSONArray) positionJson.get("closeBy");
                ArrayList<Long> closeByUsers = new ArrayList<>();
                for (Object closeById: closeByUsersJson) {
                    closeByUsers.add((Long) closeById);
                }

                position.setCloseBy(closeByUsers);
                positions.add(position);
            }
            user.setPositions(positions);
            users.add(user);
        }

        fr.close();

        return users;
    }

    public static User parseGridUser(String gridFileLocation, int userId) throws IOException, ParseException, IndexOutOfBoundsException {
        if(gridFileLocation == null){
            throw new IOException(); //TODO dizer que não foi definido a localização, apenas acontece se o grid nao for properly initialized
        }

        FileReader fr = new FileReader(gridFileLocation);

        Object obj = new JSONParser().parse(fr);
        JSONArray grid = (JSONArray) obj;

        JSONObject userJson = (JSONObject) grid.get(userId);

        User user = new User(
                Integer.parseInt(userJson.get("userId").toString()),
                userJson.get("ip").toString(),
                Integer.parseInt(userJson.get("port").toString())
        );

        JSONArray positionsJson = (JSONArray) userJson.get("positions");
        ArrayList<Position> positions = new ArrayList<>();
        for (Object positionObject: positionsJson) {
            JSONObject positionJson = (JSONObject) positionObject;

            Position position = new Position(
                    Long.parseLong(positionJson.get("epoch").toString()),
                    Long.parseLong(positionJson.get("xPos").toString()),
                    Long.parseLong(positionJson.get("yPos").toString())
            );

            JSONArray closeByUsersJson = (JSONArray) positionJson.get("closeBy");
            ArrayList<Long> closeByUsers = new ArrayList<>();
            for (Object closeById: closeByUsersJson) {
                closeByUsers.add((Long) closeById);
            }

            position.setCloseBy(closeByUsers);
            positions.add(position);
        }
        user.setPositions(positions);

        fr.close();

        return user;
    }

    public static PrivateKey readPrivateKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        String temp = new String(keyBytes);
        String privKeyPEM = temp.replace("-----BEGIN PRIVATE KEY-----", "");
        privKeyPEM = privKeyPEM.replace("-----END PRIVATE KEY-----", "");

        BASE64Decoder b64=new BASE64Decoder();
        byte[] decoded = b64.decodeBuffer(privKeyPEM);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public PublicKey readPublicKey(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int) f.length()];
        dis.readFully(keyBytes);
        dis.close();

        String temp = new String(keyBytes);
        String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n", "");
        publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");


        BASE64Decoder b64=new BASE64Decoder();
        byte[] decoded = b64.decodeBuffer(publicKeyPEM);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

}
