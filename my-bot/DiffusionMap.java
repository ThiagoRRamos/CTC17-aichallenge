import java.lang.StringBuffer;
import java.io.PrintStream;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Comparator;

public class DiffusionMap {
	
	private double[][] values;
	private Ants ants;
	private int rows;
	private int cols;

	public DiffusionMap(Ants ants){
		this.ants = ants;
		rows = ants.getRows();
		cols = ants.getCols();
		values = new double[rows][cols];
		init(ants);
	}

	public void clear(){
		values = new double[rows][cols];
	}

	public Comparator<Tile> getComparator(){
		return new TileComparator(this, ants);
	}

	public double getValue(Tile t){
		int row = t.getRow();
		int col = t.getCol();
		if(row < 0 || row >= rows){
			row = (row+rows)%rows;
		}
		if(col < 0 || col >= cols){
			col = (col+cols)%cols;
		}
		return values[row][col];
	}

	public double getValue(int row, int col){
		if(row < 0 || row >= rows){
			row = (row+rows)%rows;
		}
		if(col < 0 || col >= cols){
			col = (col+cols)%cols;
		}
		return values[row][col];
	}

	public void addValue(Tile t, double val){
		values[t.getRow()][t.getCol()] += val;
	}

	public boolean addElement(Tile t,double value, double decreaseFactor, double k){
		Queue<Tile> tiles = new LinkedList<>();
		Queue<Double> vals = new LinkedList<>();
		Set<Tile> alreadyUsed = new HashSet<>();
		tiles.add(t);
		vals.add(value);
		while(!tiles.isEmpty()){
			Tile n = tiles.poll();
			double val = vals.poll();
			alreadyUsed.add(n);
			addValue(n, val);
			if(val*k > 1){
				for(Tile no : ants.possibleNextStates(n)){
					if(!alreadyUsed.contains(no) && ants.isPassable(no)){
						tiles.add(no);
						vals.add(val/decreaseFactor);
					}
				}
			}
		}
		return true;
	}

	public void init(Ants ants){

	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<ants.getRows(); i++){
			for(int j=0;j<ants.getCols(); j++){
				sb.append(String.format("%2.0f", getValue(i,j)/100.0f)+" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void finishRound(){

	}

	public Aim getBestDirection(Tile t){
		int row = t.getRow();
		int col = t.getCol();
		double bestValue = -Double.MAX_VALUE;
		Aim bestDirection = null;
		for(Aim a : Aim.values()){
			int newR = (row + a.getRowDelta()+rows)%rows;
			int newC = (col + a.getColDelta()+cols)%cols;
			if(ants.isPassable(new Tile(newR,newC))){
				double n = values[newR][newC];
				if(n>bestValue){
					bestValue = n;
					bestDirection = a;
				}
			}
		}
		if(bestValue > values[row][col]){
			return bestDirection;
		}
		return null;
	}

	public void print(PrintStream ps, int rowstart, int rowend, int colStart, int colEnd){
		for(int r = rowstart; r < rowend; r++){
			StringBuffer sb = new StringBuffer();
			for(int c = colStart;c < colEnd; c++){
				sb.append(String.format("%2.0f", getValue(r,c))+" ");
			}
			ps.println(sb);
		}
	}

}