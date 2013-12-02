import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MinMax {

	private List<Tile> myAnts;
	private List<Tile> theirAnts;
	private Ants ants;
	private static HashMap<Integer,List<Aim>> aims = new HashMap<>();
	
	public MinMax(List<Tile> myAnts, List<Tile> theirAnts, Ants ants){
		this.myAnts = myAnts;
		this.theirAnts = theirAnts;
		this.ants = ants;
	}

	public HashMap<Tile, Aim> bestCalls() {
		int worst = Integer.MIN_VALUE;
		List<Aim> wcal = null;
		System.err.println("Starting");
		for (List<Aim> cal : myPossibleCalls()) {
			System.err.println(cal);
			int best = Integer.MAX_VALUE;
			for (List<Aim> hcal : theirPossibleCalls()) {
				int cur = saldo(cal, hcal);
				if (cur < best) {
					best = cur;
				}
			}
			if (best > worst) {
				worst = best;
				wcal = cal;
			}
		}
		HashMap<Tile, Aim> calls = new HashMap<>();
		for (int i = 0; i < myAnts.size(); i++) {
			calls.put(myAnts.get(i), wcal.get(i));
		}
		return calls;
	}

	private int saldo(List<Aim> cal, List<Aim> hcal) {
		int saldo = 0;
		List<Tile> myNewPositions = new ArrayList<>();
		List<Tile> theirNewPositions = new ArrayList<>();
		for (int i = 0; i < myAnts.size(); i++) {
			myNewPositions.add(ants.getTile(myAnts.get(i), cal.get(i)));
		}
		for (int i = 0; i < theirAnts.size(); i++) {
			theirNewPositions.add(ants.getTile(theirAnts.get(i), hcal.get(i)));
		}
		HashMap<Tile, Integer> distances = new HashMap<>();
		HashMap<Tile, List<Tile>> effects = new HashMap<>();
		for(Tile my : myNewPositions){
			distances.put(my, 0);
			effects.put(my, new ArrayList<Tile>());
		}
		for(Tile th : theirNewPositions){
			distances.put(th, 0);
			effects.put(th, new ArrayList<Tile>());
		}
		for(Tile my : myNewPositions){
			for(Tile th : theirNewPositions){
				if(ants.getDistance(my, th)<ants.getAttackRadius2()){
					distances.put(my, distances.get(my)+1);
					distances.put(th, distances.get(th)+1);
					effects.get(my).add(th);
					effects.get(th).add(my);
				}
			}
		}
		for(Tile my : myNewPositions){
			int nenemies = distances.get(my);
			for(Tile en : effects.get(my)){
				if(distances.get(en)<=nenemies){
					saldo-=1;
					break;
				}
			}
		}
		for(Tile th : theirNewPositions){
			int nenemies = distances.get(th);
			for(Tile en : effects.get(th)){
				if(distances.get(en)<=nenemies){
					saldo+=1;
					break;
				}
			}
		}
		return saldo;
	}

	private List<List<Aim>> myPossibleCalls() {
		return possibleCalls(0, myAnts.size());
	}
	
	private List<List<Aim>> theirPossibleCalls() {
		return possibleCalls(0, theirAnts.size());
	}

	private List<List<Aim>> possibleCalls(int i, int max) {
		List<List<Aim>> res = new ArrayList<List<Aim>>();
		if (i == max - 1) {
			for (Aim a : Aim.values()) {
				List<Aim> t = new ArrayList<>();
				t.add(a);
				res.add(t);
			}
		} else {
			for (Aim a : Aim.values()) {
				for (List<Aim> rc : possibleCalls(i + 1, max)) {
					List<Aim> t = new ArrayList<>();
					t.add(a);
					for (Aim aa : rc) {
						t.add(aa);
					}
					res.add(t);
				}
			}
		}
		return res;
	}

}
