package pt.tecnico.sec.hdlt.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.entities.Position;
import pt.tecnico.sec.hdlt.entities.User;

import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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

    public static PublicKey readPublicKey(String publicKeyPath) throws CertificateException, IOException {
        InputStream inputStream = new FileInputStream(publicKeyPath);
        X509Certificate certificate =
                (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
        inputStream.close();
        return certificate.getPublicKey();

//        byte[] pubEncoded = readFile(publicKeyPath);
//        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
//        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
//        return keyFacPub.generatePublic(pubSpec);
    }

    public static PublicKey getUserPublicKey(int userId)
            throws CertificateException, IOException {

        return readPublicKey("../keys/client_pub_" + userId + ".cert");
    }

    public static PublicKey getServerPublicKey(int serverId)
            throws CertificateException, IOException {

        return readPublicKey("../keys/server_pub_" + serverId + ".cert");
    }

    public static PublicKey getHAPublicKey()
            throws CertificateException, IOException {
        
        return readPublicKey("../keys/ha_pub_1.cert");
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
