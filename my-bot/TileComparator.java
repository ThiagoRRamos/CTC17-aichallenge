import java.util.Comparator;

public class TileComparator implements Comparator<Tile> {

	private DiffusionMap dm;
	private Ants ants;

	public TileComparator(DiffusionMap dm, Ants a) {
		this.dm = dm;
		this.ants = a;
	}

	@Override
	public int compare(Tile t1, Tile t2) {
		Aim a1 = dm.getBestDirection(t1);
		Aim a2 = dm.getBestDirection(t2);
		if (a1 == null) {
			if(a2 == null){
				return 0;
			}
			return +1;
		}
		if (a2 == null) {
			return -1;
		}
		Tile td1 = ants.getTile(t1, a1);
		Tile td2 = ants.getTile(t2, a2);
		return -(int) ((dm.getValue(td1) - dm.getValue(t1)) - (dm.getValue(td2) - dm
				.getValue(t2)));
	}

}