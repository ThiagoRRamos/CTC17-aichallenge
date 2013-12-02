import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttackAnalysis {

	private Ants ants;
	private int cols;
	private int rows;
	private int[][] mine;
	private Map<Tile, List<Tile>> mineTile;
	private int[][] theirs;
	private Map<Tile, List<Tile>> theirsTile;
	private MyBot bot;

	public AttackAnalysis(Ants ants, MyBot myBot) {
		this.ants = ants;
		this.rows = ants.getRows();
		this.cols = ants.getCols();
		this.bot = myBot;
		mine = new int[rows][cols];
		theirs = new int[rows][cols];
		mineTile = new HashMap<Tile, List<Tile>>();
		theirsTile = new HashMap<Tile, List<Tile>>();
	}

	public void process() {
		for (Tile my : ants.getMyAnts()) {
			for (Tile t : tileswithinradius(my)) {
				mine[t.getRow()][t.getCol()] += 1;
				addTiletoMineMap(t, my);
			}
		}
		for (Tile thei : ants.getEnemyAnts()) {
			for (Tile t : tileswithinradius(thei)) {
				theirs[t.getRow()][t.getCol()] += 1;
				addTiletoTheirsMap(t, thei);
			}
		}
	}

	private void addTiletoTheirsMap(Tile t, Tile thei) {
		if (!theirsTile.containsKey(t)) {
			theirsTile.put(t, new ArrayList<Tile>());
		}
		theirsTile.get(t).add(thei);
	}

	private void addTiletoMineMap(Tile t, Tile my) {
		if (!mineTile.containsKey(t)) {
			mineTile.put(t, new ArrayList<Tile>());
		}
		mineTile.get(t).add(my);
	}

	public void decideAttacks() {
		for (Tile my : ants.getMyAnts()) {
			for (Tile neigh : ants.possibleNextStates(my)) {
				if (theirs[neigh.getRow()][neigh.getCol()] > 1) {
					int numberEnemies = theirs[neigh.getRow()][neigh.getCol()];
					for (Tile enemy : theirsTile.get(neigh)) {
						for (Tile enemyneigh : ants.possibleNextStates(enemy)) {
							if (mineTile.get(enemyneigh) != null
									&& mineTile.get(enemyneigh).contains(my)) {
								if (mine[enemyneigh.getRow()][enemyneigh
										.getCol()] > numberEnemies) {
								} else if (mine[enemyneigh.getRow()][enemyneigh
										.getCol()] < numberEnemies) {
									fugir(my, enemy);
									break;
								} else {
								}
							} else {
							}
						}
					}
				}
			}
		}
	}

	private void fugir(Tile my, Tile enemy) {
		Aim d = getOppositeDirection(ants.getDirections(my, enemy).get(0));
		bot.doMoveDirection(my, d);
	}

	private Aim getOppositeDirection(Aim aim) {
		switch (aim) {
		case EAST:
			return Aim.WEST;
		case NORTH:
			return Aim.SOUTH;
		case SOUTH:
			return Aim.NORTH;
		case WEST:
			return Aim.EAST;
		default:
			break;
		}
		throw new RuntimeException("OPPS");
	}

	public void printMine() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				System.err.print(mine[i][j]);
			}
			System.err.println("");
		}
	}

	public void printTheirs() {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				System.err.print(theirs[i][j]);
			}
			System.err.println("");
		}
	}

	private List<Tile> tileswithinradius(Tile my) {
		List<Tile> res = new ArrayList<>();
		int mx = (int) Math.sqrt(ants.getAttackRadius2()) + 1;
		for (int row = -mx; row <= mx; ++row) {
			for (int col = -mx; col <= mx; ++col) {
				double d = Math.pow((Math.abs(row) - 1), 2.0)
						+ Math.pow((Math.abs(col) - 1), 2.0);
				double d2 = row * row + col * col;
				if (d <= ants.getAttackRadius2() + 1
						&& d2 >= ants.getAttackRadius2()) {
					int mrow = my.getRow() + row;
					int mcol = my.getCol() + col;
					if (mrow < 0) {
						mrow += rows;
					} else {
						mrow = mrow % rows;
					}
					if (mcol < 0) {
						mcol += cols;
					} else {
						mcol = mcol % cols;
					}
					res.add(new Tile(mrow, mcol));
				}
			}
		}
		return res;
	}
}
