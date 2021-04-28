package pt.tecnico.sec.hdlt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

public class FileUtils {

    public static User parseGridUser(String gridFileLocation, int userId) throws IOException, ParseException, IndexOutOfBoundsException {
        FileReader fr = new FileReader(gridFileLocation);
        Object obj = new JSONParser().parse(fr);
        JSONArray grid = (JSONArray) obj;

        //-1 because the users start at 1 not at 0 in the grid file
        JSONObject userJson = (JSONObject) grid.get(userId-1);

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
        fr.close();

        user.setPositions(positions);
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

    public static PublicKey getUserPublicKey(int userId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        return readPublicKey("../keys/client_pub_" + userId + ".der");
    }

    public static PublicKey getServerPublicKey(int serverId)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {

        return readPublicKey("../keys/server_pub_" + serverId + ".der");
    }

    public static PublicKey getHAPublicKey() throws NoSuchAlgorithmException, IOException,
            InvalidKeySpecException{
        return readPublicKey("../keys/ha_pub_1.der");
    }

    public static KeyStore loadKeyStore(File keystoreFile, String password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        if (keystoreFile == null) {
            throw new IllegalArgumentException("Keystore url may not be null");
        }

        URL keystoreUrl = keystoreFile.toURI().toURL();
        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        InputStream is = null;
        try {
            is = keystoreUrl.openStream();
            keystore.load(is, null == password ? null : password.toCharArray());
        } finally {
            if (null != is) {
                is.close();
            }
        }

        return keystore;
    }
}
