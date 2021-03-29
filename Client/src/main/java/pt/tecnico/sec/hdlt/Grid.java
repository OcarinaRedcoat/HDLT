package pt.tecnico.sec.hdlt;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class Grid {

    //private ArrayList<Integer[][]> grid_list;
    private User grid[][];

    //private ArrayList<Integer> grid;

    private int dimension_X;
    private int dimension_Y;

    protected Grid(int dimension_X, int dimension_Y){
        this.grid = new User[dimension_X][dimension_Y];

        for (int i=0; i< dimension_X; i++){
            //System.out.println(i);
            for(int j=0; j< dimension_Y; j++){
                //System.out.println(j);
                grid[i][j]=null;
            }
        }
        //grid_list = new ArrayList<>();


        this.dimension_X = dimension_X;
        this.dimension_Y = dimension_Y;
    }

    protected void parseFiles(String gridFile, String userFile) throws FileNotFoundException {
        FileReader fr = new FileReader(gridFile);
        Scanner inFile = new Scanner(fr);

        HashMap<Integer, User> userMap= userParseFile(userFile);

        int currentEpoch = 0;
        while (inFile.hasNext()) {
            String line = inFile.nextLine();
            String[] splitStr = line.split(", ");

            if (Integer.parseInt(splitStr[1]) != currentEpoch){

            }
            int user_id = Integer.parseInt(splitStr[0].substring(4,splitStr[0].length()));

            if (Integer.parseInt(splitStr[1]) == 0){
                grid[Integer.parseInt(splitStr[2])][Integer.parseInt(splitStr[3])] = userMap.get(user_id);
            }

        }
        inFile.close();
    }

    protected void displayGrid(){
        for (int i=0; i< this.dimension_X; i++){
            for(int j=0; j< this.dimension_Y; j++){
                if (grid[i][j] != null)
                    System.out.print(grid[i][j].getId() + " ");
                else
                    System.out.print("null ");
            }
            System.out.println();
        }
    }

    protected HashMap<Integer, User> userParseFile(String filename) throws FileNotFoundException {
        FileReader fr = new FileReader(filename);
        Scanner inFile = new Scanner(fr);

        HashMap<Integer, User> userMap = new HashMap<Integer, User>();

        while (inFile.hasNext()) {
            String line = inFile.nextLine();
            String[] splitStr = line.split(", ");

            int user_id = Integer.parseInt(splitStr[0].substring(4,splitStr[0].length()));
            String user_host = splitStr[1];
            int user_port = Integer.parseInt(splitStr[2]);

            User new_user = new User(user_id, user_host, user_port);

            userMap.put(user_id, new_user);
        }
        inFile.close();

        return userMap;
    }
}
