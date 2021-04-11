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

    private static byte[] readFile(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static PublicKey readPublicKey(String publicKeyPath) throws Exception {
        System.out.println("Reading public key from file: " + publicKeyPath);
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        return keyFacPub.generatePublic(pubSpec);
    }

    public static PrivateKey readPrivateKey(String privateKeyPath) throws Exception {
        System.out.println("Reading private key from file: " + privateKeyPath);
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        return keyFacPriv.generatePrivate(privSpec);
    }

}
