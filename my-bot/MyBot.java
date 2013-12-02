import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Starter bot implementation.
 */
public class MyBot extends Bot {
	/**
	 * Main method executed by the game engine for starting the bot.
	 * 
	 * @param args
	 *            command line arguments
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public static void main(String[] args) throws IOException {
		new MyBot().readSystemInput();
	}

	private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
	private Set<Tile> enemyHills = new HashSet<Tile>();
	private DiffusionMap foodMap;
	private DiffusionMap enemyHillMap;
	private DiffusionMap myHillMap;
	private DiffusionMap exploreMap;
	private DiffusionMap spacingAnts;
	private Set<Tile> unseenTiles;
	private Queue<Tile> frontier;
	private boolean goFrontier = false;// a
	private HashSet<Tile> processedFrontier;

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

	private boolean doMoveDirectionIDontCare(Tile antLoc, Aim direction) {
		Ants ants = getAnts();
		// Track all moves, prevent collisions, I dont care
		Tile newLoc = ants.getTile(antLoc, direction);
		if (!orders.containsKey(newLoc)) {
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

	private boolean doMoveLocationIDontCare(Tile antLoc, Tile destLoc) {
		Ants ants = getAnts();
		// Track targets to prevent 2 ants to the same location
		List<Aim> directions = ants.getDirections(antLoc, destLoc);
		for (Aim direction : directions) {
			if (doMoveDirectionIDontCare(antLoc, direction)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void doTurn() {
		Ants ants = getAnts();
		orders.clear();
		if (foodMap == null) {
			foodMap = new DiffusionMap(ants);
			enemyHillMap = new DiffusionMap(ants);
			myHillMap = new DiffusionMap(ants);
			exploreMap = new DiffusionMap(ants);
			frontier = new LinkedList<>();
			spacingAnts = new DiffusionMap(ants);
			processedFrontier = new HashSet<>();
			goFrontier = true;
		} else if (goFrontier) {
			for (Tile mh : ants.getMyHills()) {
				frontier.add(mh);
			}
			System.err.println("Initializing frontier");
			System.err.println(frontier);
			goFrontier = false;
		}

		foodMap.clear();
		enemyHillMap.clear();
		myHillMap.clear();
		exploreMap.clear();

		if (unseenTiles == null) {
			unseenTiles = new HashSet<Tile>();
			for (int row = 0; row < ants.getRows(); row++) {
				for (int col = 0; col < ants.getCols(); col++) {
					unseenTiles.add(new Tile(row, col));
				}
			}
		}
		// remove any tiles that can be seen, run each turn
		for (Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext();) {
			Tile next = locIter.next();
			if (ants.isVisible(next)) {
				locIter.remove();
			}
		}
		int count = 0;
		while (count < frontier.size()) {
			Tile t = frontier.poll();
			if (!unseenTiles.contains(t)) {
				count = 0;
				for (Tile neigh : ants.getNeighbors(t)) {
					if (!processedFrontier.contains(neigh)) {
						frontier.add(neigh);
						processedFrontier.add(neigh);
					}
				}
			} else {
				frontier.add(t);
			}
			count++;
		}
		for (Tile t : frontier) {
			exploreMap.addElement(t, 4000, 3, 1);
		}
		for (Tile myHill : ants.getMyHills()) {
			orders.put(myHill, null);
		}

		// find close food;
		List<Tile> sortingAnts = new ArrayList<>(ants.getMyAnts());
		int numberOfAnts = sortingAnts.size();
		int numberofMyHills = ants.getMyHills().size();

		for (Tile ma : ants.getMyAnts()) {
			spacingAnts.addElement(ma, -200, 2, -1);
		}

		for (Tile foodLoc : ants.getFoodTiles()) {
			foodMap.addElement(foodLoc, 2000, 3, 1);
		}

		// add new hills to set
		for (Tile enemyHill : ants.getEnemyHills()) {
			if (!enemyHills.contains(enemyHill)) {
				enemyHills.add(enemyHill);
			}
		}

		for (Tile enemyAnt : ants.getEnemyAnts()) {
			enemyHillMap.addElement(enemyAnt, -20, 2, -1);
		}
		// attack hills
		Set<Tile> eliminatedHills = new HashSet<Tile>();
		for (Tile hillLoc : enemyHills) {
			if (ants.isVisible(hillLoc)
					&& !ants.getEnemyHills().contains(hillLoc)) {
				eliminatedHills.add(hillLoc);
			} else {
				System.err.println("Not eliminated hill - " + hillLoc);
				enemyHillMap.addElement(hillLoc, 100000, 2, 1);
			}
		}
		for (Tile elHill : eliminatedHills) {
			System.err.println("Hill eliminated - " + elHill);
			enemyHills.remove(elHill);
		}

		for (Tile myHill : ants.getMyHills()) {
			myHillMap.addElement(myHill, 100, 2, 1);
			if (ants.getMyAnts().contains(myHill)
					&& !orders.containsValue(myHill)) {
				PathSpace ps = new PathSpace(myHill, ants);
				HashMap<Tile, Tile> res = ps.process();
				for (Tile or : res.keySet()) {
					doMoveLocationIDontCare(or, res.get(or));
				}
			}
		}
		int countGoForFood = 0;
		Collections.sort(sortingAnts, foodMap.getComparator());
		for (Tile antLoc : sortingAnts) {
			if (!orders.containsValue(antLoc)) {
				if (countGoForFood > (numberOfAnts + 1) / 2)
					break;
				Aim direction = foodMap.getBestDirection(antLoc);
				if (direction != null) {
					if (doMoveDirection(antLoc, direction)) {
						countGoForFood++;
					}
				}
			}
		}
		int countGoForEnemyHill = 0;
		for (Tile antLoc : sortingAnts) {
			if (!orders.containsValue(antLoc)) {
				if (countGoForEnemyHill > numberOfAnts / 3)
					break;
				Aim direction = enemyHillMap.getBestDirection(antLoc);
				if (direction != null) {
					if (doMoveDirection(antLoc, direction)) {
						countGoForEnemyHill++;
					}
				}
			}
		}
		int countGoForSpace = 0;
		for (Tile antLoc : sortingAnts) {
			if (countGoForSpace > numberOfAnts / 3)
				break;
			if (!orders.containsValue(antLoc)) {
				Aim direction = spacingAnts.getBestDirection(antLoc);
				if (direction != null) {
					if (doMoveDirection(antLoc, direction)) {
						countGoForSpace++;// a
					}
				}

			}
		}
		int countGoForMyHill = 0;
		for (Tile antLoc : sortingAnts) {
			if (!orders.containsValue(antLoc)) {
				if (countGoForMyHill > numberOfAnts / 6
						|| countGoForMyHill > 10 * numberofMyHills)
					break;
				Aim direction = myHillMap.getBestDirection(antLoc);
				if (direction != null) {
					if (doMoveDirection(antLoc, direction)) {
						countGoForMyHill++;// a
					}
				}
			}
		}
		System.err.println("My hill: " + countGoForMyHill);
		System.err.println("Enemy hill: " + countGoForEnemyHill);
		System.err.println("Food : " + countGoForFood);
		System.err.println("Space: " + countGoForSpace);

	}
}
