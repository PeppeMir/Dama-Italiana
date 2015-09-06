package structure;

import java.awt.Point;

public class EatTree 
{
	public int value;
	public EatTree sx = null;
	public EatTree dx = null;
	public EatTree father = null;
	private Point location;
	public int depth;
	private boolean evitasx = false;
	private boolean evitadx = false;
	private EatTree max_tmpNode = null;
	
	public EatTree(int[][] matrix, Point current_loc, boolean up, EatTree _father, int maxNumOfLevels)
	{
		this.location = current_loc;
		this.value = matrix[current_loc.x][current_loc.y]; 
		this.father = _father;
		this.depth = -1;
		
		// i can go (up || down) ?
		if ((up && current_loc.x == 0) || (!up && current_loc.x == 7) || (maxNumOfLevels == 0))
		{
			this.sx = this.dx = null;
			return;
		}
		
		// i can go right?
		if ((current_loc.y + 1) > 7)
		{
			this.dx = null;
			evitadx = true;
		}
		
		// i can go left?
		if ((current_loc.y - 1) < 0)
		{
			this.sx = null;
			evitasx = true;
		}
		
		if (!evitasx)
		{
			Point nextsx;
			
			if (up)
				nextsx = new Point(current_loc.x - 1, current_loc.y - 1);
			else
				nextsx = new Point(current_loc.x + 1, current_loc.y - 1);
			
			this.sx = new EatTree(matrix, nextsx, up, this, maxNumOfLevels - 1);	
		}
		
		if (!evitadx)
		{
			Point nextdx;
			
			if (up)
				nextdx = new Point(current_loc.x - 1, current_loc.y + 1);
			else
				nextdx = new Point(current_loc.x + 1, current_loc.y + 1);
			
			this.dx = new EatTree(matrix, nextdx, up, this, maxNumOfLevels - 1);
		}
	}
	
	public EatTree getMaxDepthNode()
	{
		max_tmpNode = null;
		visitTreeForMaxDepth(this);		//initialize "max_tmpNode" to max depth
		return max_tmpNode;
	}
	
	private void visitTreeForMaxDepth(EatTree node)
	{
		if (max_tmpNode == null || (max_tmpNode.depth < node.depth))
			max_tmpNode = node;
		
		if (node.sx != null)
			visitTreeForMaxDepth(node.sx);
		
		if (node.dx != null)
			visitTreeForMaxDepth(node.dx);
	}
	
	public void setDepth(int value)
	{
		this.depth = value;
	}
	
	public Point getLocation()
	{
		return this.location;
	}
}
