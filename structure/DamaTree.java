package structure;

import java.util.ArrayList;
import engine.Moves.Action;

public class DamaTree 
{
	private int[][] matrix;
	private int score;
	public int depth;
	public Action moveType;
	public DamaTree father;
	private ArrayList<DamaTree> sons;
	
	/*** Create radix of tree ***/
	public DamaTree(int[][] radix_matrix)
	{
		father = null;
		score = depth = 0;
		sons = null;
		matrix = radix_matrix;
	}
	
	public DamaTree(int[][] radix_matrix, DamaTree _father, int _depth, int _score, Action type)
	{
		this.matrix = radix_matrix;
		this.father = _father;
		this.score = _score;
		this.depth = _depth;
		this.moveType = type;
		sons = null;
	}
	
	public int[][] getMatrix()
	{
		return this.matrix;
	}
	
	public int getDepth()
	{
		return this.depth;
	}
	
	public void setSons(ArrayList<DamaTree> list)
	{
		this.sons = list;
	}
	
	public ArrayList<DamaTree> getSonsList()
	{
		return this.sons;
	}
	
	public void setScore(int s)
	{
		this.score = s;
	}
	
	public int getScore()
	{
		return this.score;
	}

}
