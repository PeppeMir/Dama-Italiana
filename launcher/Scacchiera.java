package launcher;

import javax.swing.*;
import structure.EatTree;
import engine.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Scacchiera extends JPanel		// scacchiera classic 8 x 8
{
	private int[][] matrix = new int[Moves.DIM][Moves.DIM];
	private final static int CELLSPACE = 80;
	private final static int INNERRAY = 20;
	private Engine engine;
	public boolean HUMANvsCPU = false;
	public boolean CPUvsCPU = false;
	private int numWHITE = 12;
	private int numBLACK = 12;
	
	//Graphics
	private ArrayList<Rectangle> coord_list = new ArrayList<Rectangle>();	//for pick correlation
	private Point mossaStart;
	private Point mossaEnd;
	private Color color1 = new Color(139,69,19);		//dark brown
	private Color color2 = new Color(210,180,140);		//light brown
	
	public Scacchiera()
	{
		super();
		this.setBackground(color2);
		initializeInternal();
		
		this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) 
            {
            	if (HUMANvsCPU)
            	{
	                for (Rectangle r : coord_list)
	                	if (r.contains(evt.getPoint()))
	                	{
	                		mossaStart = new Point(r.y / CELLSPACE, r.x / CELLSPACE);
	                		break;
	                	}
            	}
            }
            public void mouseReleased(MouseEvent evt) 
            {
            	if (HUMANvsCPU && (matrix[mossaStart.x][mossaStart.y] == Moves.WHITE || matrix[mossaStart.x][mossaStart.y] == Moves.D_WHITE))
            	{
	            	for (Rectangle r : coord_list)
	                	if (r.contains(evt.getPoint()))
	                	{
	                		mossaEnd = new Point(r.y / CELLSPACE, r.x / CELLSPACE);
	                		moveWhite();
	                		break;
	                	}
            	}
            }
        });
	}

	/*** Effettua una mossa del bianco e fa partire il calcolo del nero ***/
	private void moveWhite()
	{
		EatTree checker_tree = new EatTree(this.matrix, mossaStart, true, null, Engine.MAXDEPTH);
		Moves.setLastEatPosition(checker_tree, 0, Moves.BLACK);	//switch enemy
		EatTree max_depth_node = checker_tree.getMaxDepthNode();
		EatTree end_eat_dama = Moves.searchMaxDepthDamaEat(matrix[mossaStart.x][mossaStart.y], mossaStart, matrix);
		
		if (matrix[mossaStart.x][mossaStart.y] == Moves.D_WHITE && end_eat_dama != null && end_eat_dama.depth > 0)		// >=1 of eat dama!
		{
			Point end_position = end_eat_dama.getLocation();
			if (!end_position.equals(mossaEnd))
			{
				JOptionPane.showMessageDialog(null, "Mangiata migliore con arrivo in (" + end_position.x + "," + end_position.y + ")");
				return;
			}
			else
			{
				matrix[end_position.x][end_position.y] = matrix[mossaStart.x][mossaStart.y];
				
				while (end_eat_dama.father != null)
				{
					end_eat_dama = end_eat_dama.father;
					end_position = end_eat_dama.getLocation();
					matrix[end_position.x][end_position.y] = Moves.FREE;
				}
			}
		}
		else
			if (max_depth_node.depth > 0)	// >=1 of eat pedina!
			{
				Point end_position = max_depth_node.getLocation();
				if (!end_position.equals(mossaEnd))
				{
					JOptionPane.showMessageDialog(null, "Mangiata migliore con arrivo in (" + end_position.x + "," + end_position.y + ")");
					return;
				}
				else
				{
					matrix[end_position.x][end_position.y] = matrix[mossaStart.x][mossaStart.y];
					
					while (max_depth_node.father != null)
					{
						max_depth_node = max_depth_node.father;
						end_position = max_depth_node.getLocation();
						matrix[end_position.x][end_position.y] = Moves.FREE;
					}
				}
			}
			else
			{
				if (matrix[mossaStart.x][mossaStart.y] == Moves.WHITE)
				{
					// evito spostamento in basso e quando resto sulla stessa casella
					if (mossaStart.equals(mossaEnd) || (mossaStart.x - mossaEnd.x) != 1 || Math.abs((mossaStart.y - mossaEnd.y)) != 1 )
						return;
				}
				else
					if (matrix[mossaStart.x][mossaStart.y] == Moves.D_WHITE)
					{
						if (mossaStart.equals(mossaEnd) || Math.abs((mossaStart.x - mossaEnd.x)) != 1 || Math.abs((mossaStart.y - mossaEnd.y)) != 1 )
							return;
					}
				
				// rilascio su posizione libera ==> switch
				if (matrix[mossaEnd.x][mossaEnd.y] == Moves.FREE)
				{	
					matrix[mossaEnd.x][mossaEnd.y] = matrix[mossaStart.x][mossaStart.y];	//switch pedina
					matrix[mossaStart.x][mossaStart.y] = Moves.FREE;
				}
			}
			
			if (mossaEnd.x == 0)
				matrix[mossaEnd.x][mossaEnd.y] = Moves.D_WHITE;
			
			
			
			this.paint(getGraphics());					//repaint scacchiera
			recountPedine(matrix);
			
			if (this.numBLACK == 0)
			{
				System.out.println("WHITE player WINS");
				JOptionPane.showMessageDialog(null, "Vince il BIANCO");
			}
			else
				engine.builtSearchTree(matrix, Moves.BLACK);
		}
	
	public void playCPUvsCPU()
	{
		for (int i = 0; i < 200; i++)		//###DEBUG:: upper bound evita loop ###
		//while (this.numBLACK > 0 && this.numWHITE > 0)
		{
			if (this.numWHITE > 0 && this.numBLACK > 0)
			{
				if (!engine.builtSearchTree(matrix, Moves.WHITE))
					return;
			}
			else
				return;
			
			if (this.numWHITE > 0 && this.numBLACK > 0)
			{
				if (!engine.builtSearchTree(matrix, Moves.BLACK))
					return;
			}
			else
				return;
		}
	}
	
	private void initializeInternal()
	{
		for (int row = 0; row < matrix.length; row++)
			for (int col = 0; col < matrix.length; col++)
				if ((col % 2) == (row % 2))
				{
					coord_list.add(new Rectangle(col * CELLSPACE, row * CELLSPACE, CELLSPACE, CELLSPACE));
					
					if (row < 3)
						matrix[row][col] = Moves.BLACK;
					else
						if (row >= 5)
							matrix[row][col] = Moves.WHITE;
						else
							matrix[row][col] = Moves.FREE;
				}
				else
					matrix[row][col] = Moves.INACCESSIBLE;	
		
		engine = new Engine(this);
		this.numBLACK = this.numWHITE = 12;
		//matrix[5][1] = Moves.D_WHITE;
	}
	
	public void resetStatus()
	{
		this.CPUvsCPU = this.HUMANvsCPU = false;
		initializeInternal();
		paintComponent(this.getGraphics());
	}
	
	public void updateMatrix(int[][] new_matrix)
	{
		if (new_matrix != null)
		{
			this.matrix = new_matrix;
			this.paint(getGraphics());					//repaint scacchiera
			recountPedine(new_matrix);
			
			if (numWHITE == 0)
			{
				System.out.println("BLACK player WINS");
				JOptionPane.showMessageDialog(null, "Vince il NERO");
			}
			else
				if (numBLACK == 0)
				{
					System.out.println("WHITE player WINS");
					JOptionPane.showMessageDialog(null, "Vince il BIANCO");
				}
		}
		else
			JOptionPane.showMessageDialog(null, "ERROR: null matrix -.-");
	}
	
	private void recountPedine(int[][] new_matrix)
	{
		numBLACK = numWHITE = 0;
		for (int i = 0; i < Moves.DIM; i++)
			for (int j = 0; j < Moves.DIM; j++)
			{
				if (new_matrix[i][j] == Moves.WHITE || new_matrix[i][j] == Moves.D_WHITE)
					numWHITE++;
				else
					if (new_matrix[i][j] == Moves.BLACK || new_matrix[i][j] == Moves.D_BLACK)
						numBLACK++;
			}
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		/*** Drawing scacchiera ***/
		g2.setColor(color1);
		for (int row = 0; row < matrix.length; row++)
			for (int col = 0; col < matrix.length; col++)
				if ((row % 2) == (col % 2))
					g2.fillRect(col * CELLSPACE, row * CELLSPACE, CELLSPACE, CELLSPACE);
		
		/*** Drawing pedine **/
		for (int row = 0; row < matrix.length; row++)
			for (int col = 0; col < matrix.length; col++)
				if (matrix[row][col] == Moves.BLACK)
				{
					g2.setColor(Color.black);
					g2.fillOval(col * CELLSPACE, row * CELLSPACE, CELLSPACE, CELLSPACE);
				}
				else
					if (matrix[row][col] == Moves.WHITE)
					{
						g2.setColor(Color.yellow);
						g2.fillOval(col * CELLSPACE, row * CELLSPACE, CELLSPACE, CELLSPACE);
					}
					else
						if (matrix[row][col] == Moves.D_BLACK)
						{
							int x = col * CELLSPACE;
							int y = row * CELLSPACE;
							g2.setColor(Color.black);
							g2.fillOval(x, y, CELLSPACE, CELLSPACE);
							g2.setColor(Color.red);
							g2.fillOval(x + INNERRAY, y + INNERRAY, 2*INNERRAY, 2*INNERRAY);
						}
						else
							if (matrix[row][col] == Moves.D_WHITE)
							{
								int x = col * CELLSPACE;
								int y = row * CELLSPACE;
								g2.setColor(Color.yellow);
								g2.fillOval(x, y, CELLSPACE, CELLSPACE);
								g2.setColor(Color.DARK_GRAY);
								g2.fillOval(x + INNERRAY, y + INNERRAY, 2*INNERRAY, 2*INNERRAY);
							}
	}

}
