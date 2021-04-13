package pt.tecnico.sec.hdlt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

public class FileUtils {

    public static User parseGridUser(String gridFileLocation, int userId) throws IOException, ParseException, IndexOutOfBoundsException {
        //TODO: falta ver se o ficheiro é mesmo json e mandar exceptions se isso ocurrer acho eu
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

    public static PublicKey readPublicKey(String publicKeyPath) throws NoSuchAlgorithmException, IOException,
            InvalidKeySpecException {

        System.out.println("Reading public key from file: " + publicKeyPath);
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        return keyFacPub.generatePublic(pubSpec);
    }

    public static PrivateKey readPrivateKey(String privateKeyPath) throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException {

        System.out.println("Reading private key from file: " + privateKeyPath);
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        return keyFacPriv.generatePrivate(privSpec);
    }

    public static PrivateKey getUserPrivateKey(int userId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        return readPrivateKey("../keys/priv_client_" + userId + ".der");
    }

    public static PublicKey getUserPublicKey(int userId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        return readPublicKey("../keys/pub_client_" + userId + ".der");
    }

    public static PublicKey getServerPublicKey(int serverId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        return readPublicKey("keys/pub_server_" + serverId + ".der");
    }

}
