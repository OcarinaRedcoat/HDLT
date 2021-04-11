package pt.tecnico.sec.hdlt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
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

}
