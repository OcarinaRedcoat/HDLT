package pt.tecnico.sec.hdlt.client;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

public class Grid {

    private HashMap<Integer, User[][]> gridMap;

    private int number_epochs;

    private int dimension_X;
    private int dimension_Y;

    protected Grid(int dimension_X, int dimension_Y){
        this.dimension_X = dimension_X;
        this.dimension_Y = dimension_Y;
    }

    protected void parseFiles(String gridFile, String userFile) throws FileNotFoundException {
        FileReader fr = new FileReader(gridFile);
        Scanner inFile = new Scanner(fr);

        HashMap<Integer, User> userMap= userParseFile(userFile);

        this.gridMap = new HashMap<>();

        User grid[][];
        grid = new User[dimension_X][dimension_Y];

        for (int i=0; i< dimension_X; i++){
            for(int j=0; j< dimension_Y; j++){
                grid[i][j]=null;
            }
        }
        int currentEpoch = 0;
        while (inFile.hasNext()) {


            String line = inFile.nextLine();
            String[] splitStr = line.split(", ");

            if (Integer.parseInt(splitStr[1]) - 1 == currentEpoch + 1){
                int qwer = Integer.parseInt(splitStr[1]);

                System.out.println("splitStr[1]" + qwer  + " currentEpoch " + currentEpoch);
                gridMap.put(currentEpoch, grid);
                currentEpoch++;
                // Clean grid again
                for (int i=0; i< dimension_X; i++){
                    for(int j=0; j< dimension_Y; j++){
                        grid[i][j]=null;
                    }
                }
            }

            int user_id = Integer.parseInt(splitStr[0].substring(4,splitStr[0].length()));

            if (Integer.parseInt(splitStr[1]) == 0){
                grid[Integer.parseInt(splitStr[2])][Integer.parseInt(splitStr[3])] = userMap.get(user_id);
            }

        }
        gridMap.put(currentEpoch, grid); // insert last epoch
        this.number_epochs = currentEpoch;
        inFile.close();
    }


    protected void displayGrids(){
        System.out.println("Number of epochs " + this.number_epochs );
        for (int i=0; i <= this.number_epochs; i++){
            System.out.println("Current epoch " + i);
            User[][] current_grid = this.gridMap.get(i);
            for (int j=0; j< dimension_X; j++){
                for(int k=0; k< dimension_Y; k++){
                    System.out.println("j " + j + " k " + k);
                    System.out.print(current_grid[j][k].getId() + " ");
                }
                System.out.println();
            }
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
