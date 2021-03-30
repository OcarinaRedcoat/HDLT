package pt.tecnico.sec.hdlt.client;

/**
 * Hello world!
 *
 */
public class UserApp
{
    public static void main( String[] args ) throws Exception {
        String grid_filename = args[0];

        Grid grid = new Grid(); // change hard coded
        grid.parseFiles(grid_filename);

    }
}
