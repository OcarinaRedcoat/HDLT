package pt.tecnico.sec.hdlt.client.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.Position;
import pt.tecnico.sec.hdlt.client.user.User;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {

    private static FileUtils INSTANCE = null;

    private String gridFileLocation;

    public FileUtils() {
        gridFileLocation = null;
    }

    public static FileUtils getInstance(){
        if (INSTANCE == null)
            INSTANCE = new FileUtils();

        return INSTANCE;
    }

    public void setGridFileLocation(String gridFileLocation) throws IOException {
        FileReader fr = new FileReader(gridFileLocation);
        fr.close();
        this.gridFileLocation = gridFileLocation;
    }

    public ArrayList<User> parseGridUsers() throws IOException, ParseException {
        if(this.gridFileLocation == null){
            throw new IOException(); //TODO dizer que não foi definido a localização
        }

        FileReader fr = new FileReader(this.gridFileLocation);

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

    public User parseGridUser(int userId) throws IOException, ParseException, IndexOutOfBoundsException {
        if(this.gridFileLocation == null){
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

}
