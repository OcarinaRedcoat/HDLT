package pt.tecnico.sec.hdlt.client;

/**
 * Hello world!
 *
 */
public class UserApp
{
    public static void main( String[] args ) throws Exception {
        String grid_filename = args[0];

        Grid grid = new Grid(15, 15); // change hard coded
        grid.parseFiles(grid_filename);

        grid.displayGrids();
    }
}
