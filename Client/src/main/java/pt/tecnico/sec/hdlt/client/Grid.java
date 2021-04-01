package pt.tecnico.sec.hdlt.client;

import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.User;


public class Grid {

    private HashMap<Long, ArrayList<User>> gridMap;

    protected Grid(){
        this.gridMap = new HashMap<>();
    }

    protected void parseFiles(String gridFile) throws IOException, ParseException {
        FileReader fr = new FileReader(gridFile);

        try {
            Object obj = new JSONParser().parse(fr);
            JSONArray arr = (JSONArray) obj;

            ArrayList<User> arrayList = new ArrayList<>();
            long current_epoch = (long) ((JSONObject) arr.get(0)).get("epoch"); //TODO: comeca com o epoch do primeiro elemento pois o ficheiro esta ordenado
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



                User new_user = new User(user_id, "localhost", 10000+user_id, x_position, y_position, closeBy);

               arrayList.add(new_user);
            }
            System.out.println("Grid Initialized.");
            this.gridMap.put(current_epoch+1, arrayList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    /*
    *
    *
    * NAO ESTA PROPRIAMENTE ELEGANTE ESTES PROXIMOS DOIS METODOS...
    *
    *
    * */
    public User getMyUser(int userId, long epoch) throws InvalidParameterException {
        ArrayList<User> currentGrid = gridMap.get(epoch);

        if(currentGrid == null){ //se a grid não tiver o epoch
            throw new InvalidParameterException(); //TODO: fazer throws de jeito e tratar no main
        }

        if(currentGrid.size() < userId){ // se o uid não existir
            throw new InvalidParameterException(); //TODO: fazer throws de jeito e tratar no main
        }

        return currentGrid.get(userId);
    }

    public ArrayList<User> usersClosedBy(ArrayList<Long> userIdList, int epoch){
        ArrayList<User> currentGrid = gridMap.get(epoch);
        ArrayList<User> ret = new ArrayList<>();
        for (int i = 0; i < userIdList.size(); i++){
           User close = currentGrid.get(userIdList.get(i).intValue());
           ret.add(close);
        }
        return ret;
    }

}
