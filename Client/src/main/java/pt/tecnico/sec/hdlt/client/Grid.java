package pt.tecnico.sec.hdlt.client;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Grid {

    private HashMap<Integer, ArrayList<User>> gridMap;

    protected Grid(){
        this.gridMap = new HashMap<>();
    }

    protected void parseFiles(String gridFile) throws IOException, ParseException {
        FileReader fr = new FileReader(gridFile);

        try {
            Object obj = new JSONParser().parse(fr);
            JSONArray arr = (JSONArray) obj;

            ArrayList<User> arrayList = new ArrayList<>();
            int current_epoch = 0;
            for (Object object: arr) {
                JSONObject aux = (JSONObject) object;

                if (Integer.parseInt(aux.get("epoch").toString()) != current_epoch) {
                    this.gridMap.put(current_epoch, arrayList);
                    current_epoch++;
                    arrayList.clear();
                }

                int user_id = Integer.parseInt(aux.get("userId").toString());;;
                int x_position = Integer.parseInt(aux.get("xPos").toString());;
                int y_position = Integer.parseInt(aux.get("yPos").toString());
                ArrayList<Long> closeBy = new ArrayList<>();
                JSONArray closeByJSON = (JSONArray) aux.get("closeBy");

                for (Object closeById: closeByJSON) {
                    Long userId = (Long) closeById;
                    closeBy.add(userId);
                }

                System.out.println(user_id + " " + x_position + " " + y_position + " " + closeBy.toString());

                User new_user = new User(user_id, "localhost", 10000+user_id, x_position, y_position, closeBy);

               arrayList.add(new_user);
            }
            this.gridMap.put(current_epoch+1, arrayList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

}
