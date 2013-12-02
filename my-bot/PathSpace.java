import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class PathSpace {

	private Tile myHill;
	private Ants ants;

	public PathSpace(Tile myHill, Ants ants) {
		this.myHill = myHill;
		this.ants = ants;
	}

	public HashMap<Tile, Tile> process() {
		HashSet<Tile> alreadySeen = new HashSet<>();
		Queue<Tile> frontier = new LinkedList<>();
		HashMap<Tile, Tile> parenthood = new HashMap<>();
		frontier.add(myHill);
		while (!frontier.isEmpty()) {
			Tile now = frontier.poll();
			if (!ants.isOccupied(now)) {
				return resultingPath(now, parenthood);
			}
			alreadySeen.add(now);
			for (Tile t : ants.possibleNextStates(now)) {
				if (!alreadySeen.contains(t)) {
					parenthood.put(t, now);
					frontier.add(t);
				}
			}
		}
		return null;
	}

	public HashMap<Tile, Tile> resultingPath(Tile now,
			HashMap<Tile, Tile> parenthood) {
		HashMap<Tile, Tile> res = new HashMap<>();
		Tile current = now;
		while (current != null) {
			Tile par = parenthood.get(current);
			if (par != null) {
				res.put(par, current);
			}
			current = par;
		}
		return res;
	}
}
