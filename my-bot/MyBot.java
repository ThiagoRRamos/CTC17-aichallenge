import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }
    
    private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
    private Set<Tile> enemyHills = new HashSet<Tile>();
    private DiffusionMap foodMap;
    private DiffusionMap enemyHillMap;
     private DiffusionMap myHillMap;
    private Set<Tile> unseenTiles;


    private boolean doMoveDirection(Tile antLoc, Aim direction) {
        Ants ants = getAnts();
        // Track all moves, prevent collisions
        Tile newLoc = ants.getTile(antLoc, direction);
        if (ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc)) {
            ants.issueOrder(antLoc, direction);
            orders.put(newLoc, antLoc);
            return true;
        } else {
            return false;
        }
    }

    private boolean doMoveLocation(Tile antLoc, Tile destLoc) {
        Ants ants = getAnts();
        // Track targets to prevent 2 ants to the same location
        List<Aim> directions = ants.getDirections(antLoc, destLoc);
        for (Aim direction : directions) {
            if (doMoveDirection(antLoc, direction)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doTurn() {
        System.err.println("A");
        Ants ants = getAnts();
        orders.clear();
        if(foodMap == null){
            foodMap = new DiffusionMap(ants);
            enemyHillMap = new DiffusionMap(ants);
            myHillMap = new DiffusionMap(ants);
        }
        foodMap.clear();
        enemyHillMap.clear();
        myHillMap.clear();
        Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();

        if (unseenTiles == null) {
            unseenTiles = new HashSet<Tile>();
            for (int row = 0; row < ants.getRows(); row++) {
                for (int col = 0; col < ants.getCols(); col++) {
                    unseenTiles.add(new Tile(row, col));
                }
            }
        }
        // remove any tiles that can be seen, run each turn
        for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext(); ) {
            Tile next = locIter.next();
            if (ants.isVisible(next)) {
                locIter.remove();
            }
        }
        for (Tile myHill : ants.getMyHills()) {
            orders.put(myHill, null);
        }

        // find close food;
        TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        int numberOfAnts = sortedAnts.size();

        for (Tile foodLoc : ants.getFoodTiles()) {
            foodMap.addElement(foodLoc, 200, 2, 1);
        }

         // add new hills to set
        for (Tile enemyHill : ants.getEnemyHills()) {
            if (!enemyHills.contains(enemyHill)) {
                enemyHills.add(enemyHill);
            }
        }

        for (Tile enemyAnt : ants.getEnemyAnts()) {
            enemyHillMap.addElement(enemyAnt, -5, 2, -1);
        }
        // attack hills
        Set<Tile> eliminatedHills = new HashSet<Tile>();
        for (Tile hillLoc : enemyHills) {
            if(ants.isVisible(hillLoc) && !ants.getEnemyHills().contains(hillLoc)){
                eliminatedHills.add(hillLoc);
            }else{
                System.err.println("Not eliminated hill - "+hillLoc);
                enemyHillMap.addElement(hillLoc, 100000, 2, 1);
            }  
        }
        for(Tile elHill : eliminatedHills){
            System.err.println("Hill eliminated - "+elHill);
            enemyHills.remove(elHill);
        }

        for (Tile myHill : ants.getMyHills()) {
            myHillMap.addElement(myHill, 100, 2, 1);
            if (ants.getMyAnts().contains(myHill) && !orders.containsValue(myHill)) {
                for (Aim direction : Aim.values()) {
                    if (doMoveDirection(myHill, direction)) {
                        break;
                    }
                }
            }
        }
        int count = 0;
        for (Tile antLoc : sortedAnts) {
            if (!orders.containsValue(antLoc)){
                if(count > numberOfAnts/2)
                    break;
                Aim direction = foodMap.getBestDirection(antLoc);
                if(direction!=null){
                    doMoveDirection(antLoc,direction);
                    count++;
                    System.err.println("Formiga indo atras de comida");
                }
            }
        }
        count = 0;
        for (Tile antLoc : sortedAnts) {
            if (!orders.containsValue(antLoc)){
                if(count > numberOfAnts/3)
                    break;
                Aim direction = enemyHillMap.getBestDirection(antLoc);
                if(direction!=null){
                    doMoveDirection(antLoc,direction);
                    count++;
                    System.err.println("Formiga indo atras de hill inimigo");
                }
            }
        }
        count = 0;
        for (Tile antLoc : sortedAnts) {
            if (!orders.containsValue(antLoc)) {
                List<Route> unseenRoutes = new ArrayList<Route>();
                for (Tile unseenLoc : unseenTiles) {
                    int distance = ants.getDistance(antLoc, unseenLoc);
                    Route route = new Route(antLoc, unseenLoc, distance);
                    unseenRoutes.add(route);
                }
                Collections.sort(unseenRoutes);
                for (Route route : unseenRoutes) {
                    if (doMoveLocation(route.getStart(), route.getEnd())) {
                        System.err.println("Formiga explorando");
                        break;
                    }
                }
            }
        }
        count = 0;
        for (Tile antLoc : sortedAnts) {
            if (!orders.containsValue(antLoc)){
                if(count > numberOfAnts/3)
                    break;
                count++;
                Aim direction = myHillMap.getBestDirection(antLoc);
                if(direction!=null){
                    System.err.println("Formiga indo atras de hill proprio");
                    doMoveDirection(antLoc,direction);
                }
            }
        }
        System.err.println(enemyHillMap);
    }
}
