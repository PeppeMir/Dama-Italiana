package engine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import javax.swing.JOptionPane;

import engine.Moves.Action;
import launcher.Scacchiera;
import structure.DamaTree;

public class Engine 
{
	public final static int MAXDEPTH = 6;	// max profondità di ricerca dell'albero
	private final static int MAXDIM_QUEUE = 20;
	private DamaTree radixOfTree;
	private Scacchiera scacchiera;
	private final static int RANDOMFACTOR = 10; // weight factor
	private final static int PEDINA = 100; 		// Pedina's Weigth
	private final static int DAMA = 200; 		// Dama's Weight
	private final static int JPOSFACTOR = 1;  			//one matrix along the -j worth 1
	private final static int BOARD = 10; 		// fattore per dama sui bordi
	
	public Engine(Scacchiera s)
	{
		this.scacchiera = s;
	}
	
	/***********************************************************************************************/
	/*********************************** FUNZIONI DI VALUTAZIONE ***********************************/
	/***********************************************************************************************/
	
	private static int coverBLACK(int[][] matrix, int i, int j)
	{
		int score = 0, tmp = 0;
		
		if (!Moves.inRange(i,j) || ((Moves.inRange(i-1,j-1)  && matrix[i-1][j-1] != Moves.BLACK ) && (Moves.inRange(i-1,j+1) && matrix[i-1][j+1] != Moves.BLACK)))
			if (i == -1)
				return 0;
			else
				return -1;
		else
		{
			tmp = coverBLACK(matrix, i-1, j-1);
			if(tmp!=-1)
				score += tmp + 2;
			
			tmp = coverBLACK(matrix, i-1, j+1);
			if(tmp!=-1)
				score += tmp + 2;
		}
		return score;
	}
	
	private static int coverWHITE(int[][] matrix, int i, int j)
	{
		int score = 0, tmp = 0;
		
		if ( !Moves.inRange(i,j) || ((Moves.inRange(i+1,j-1)  && matrix[i+1][j-1] != Moves.WHITE) && (Moves.inRange(i+1,j+1) && matrix[i+1][j+1] != Moves.WHITE)))
			if(i == 8)
				return 0;
			else
				return -1;
		else
		{
			tmp = coverWHITE(matrix, i+1, j-1);
			if(tmp != -1)
				score += tmp + 2;
			
			tmp = coverWHITE(matrix, i+1, j+1);
			
			if(tmp != -1)
				score += tmp + 2;
		}
		
		return score;
	}
	
	public static int valuteDefensed(int[][] matrix)
	{
		int score = 0, numDameW = 0, numDameB = 0;
		int[][] posDamaW = new int[12][2];
		int[][] posDamaB = new int[12][2];
		
		for (int i = 0; i < Moves.DIM; i++)
			for (int j = 0; j < Moves.DIM; j++)
			{
				if (matrix[i][j] == Moves.WHITE)
				{
					score -= PEDINA;
					score -= JPOSFACTOR*i*i;
					score -= coverWHITE(matrix,i,j);
				}
				else 
					if (matrix[i][j] == Moves.BLACK)
					{
						score += PEDINA;
						score += JPOSFACTOR*(7-i)*(7-i); 
						score += coverBLACK(matrix,i,j);			
					}  
					else 
						if (matrix[i][j] == Moves.D_WHITE)
						{
							posDamaW[numDameW][0]=i;
							posDamaW[numDameW][1]=j;
							numDameW++;
							score -= DAMA;
							if (i==0 || i==7)
								score += BOARD;
							if (j==0 || j==7)
								score += BOARD;
						}
						else 
							if (matrix[i][j] == Moves.D_BLACK)
							{
								posDamaB[numDameB][0]=i;
								posDamaB[numDameB][1]=j;
								numDameB++;
								score += DAMA;
								if (i==0 || i==7)
									score -= BOARD;
								if (j==0 || j==7)
									score -= BOARD;
							}
			}

	    score += (int)(Math.random() * RANDOMFACTOR);
		return score;
	}
	
	
	/*** Funzione di valutazione Peso + Posizione :
	 	 Il peso dei pezzi viene moltiplicato per il quadrato dell’indice relativo alla riga in cui si trova il pezzo.  ***/
	public static int valuteBalanced(int[][] matrix) 
	{
		int score = 0;
		    
		for (int i = 0; i < Moves.DIM; i++)
			for (int j = 0; j < Moves.DIM; j++)
			{
				if (matrix[i][j] == Moves.WHITE)
			    {
					score -= PEDINA;
					score -= JPOSFACTOR * (7-i) * (7-i);	//duale: riga i = 7 <==> 7 - 7 = riga 0
			    }
			    else 
			    	if (matrix[i][j] == Moves.D_WHITE)
			    	{
				      score -= DAMA;
				      
					  if (i == 0 || i == 7)		//check board (WHITE)
						  score += BOARD;
					  
					  if (j == 0 || j == 7)
						  score += BOARD;
				    }
				    else 
				    	if (matrix[i][j] == Moves.D_BLACK)
				    	{
					      score += DAMA;
					      
						  if (i == 0 || i == 7)		//check board (BLACK)
							  score -= BOARD;
						  
						  if (j == 0 || j == 7)
							  score -= BOARD;
					    }
				    	else 
				    		if (matrix[i][j] == Moves.BLACK)
						    {
								score += PEDINA;
								score += JPOSFACTOR * i * i; 	
						    }  
			}
				       
		score += (int)(Math.random() * RANDOMFACTOR);
		return score;
	  }
	
	public static int valuteBalancedReversed(int[][] matrix) 
	{
		int score = 0;
	    
		for (int i = 0; i < Moves.DIM; i++)
			for (int j = 0; j < Moves.DIM; j++)
			{
				if (matrix[i][j] == Moves.WHITE)
			    {
					score -= PEDINA;
					score -= JPOSFACTOR * i * i;	//REVERSED
			    }
			    else 
			    	if (matrix[i][j] == Moves.D_WHITE)
			    	{
				      score -= DAMA;
				      
					  if (i == 0 || i == 7)		//check board (WHITE)
						  score += BOARD;
					  
					  if (j == 0 || j == 7)
						  score += BOARD;
				    }
				    else 
				    	if (matrix[i][j] == Moves.D_BLACK)
				    	{
					      score += DAMA;
					      
						  if (i == 0 || i == 7)		//check board (BLACK)
							  score -= BOARD;
						  
						  if (j == 0 || j == 7)
							  score -= BOARD;
					    }
				    	else 
				    		if (matrix[i][j] == Moves.BLACK)
						    {
								score += PEDINA;
								score += JPOSFACTOR * (7 - i) * (7 - i); 	//duale: riga i = 7 <==> 7 - 7 = riga 0
						    }  
			}
				       
		score += (int)(Math.random() * RANDOMFACTOR);
		return score;
	  }
	
	/***********************************************************************************************/
	/*********************************** END FUNZIONI DI VALUTAZIONE ***********************************/
	/***********************************************************************************************/
	
	/*** DFS initialization of tree
	 * 	 L'albero, a partire dal figlio subito DOPO la radice, è formato da nero-bianco alternati per livello
	 * @param node
	 */
	private void recursiveSonsInitialization(DamaTree node)
	{
		if (node != null && node.getDepth() < (MAXDEPTH - 1) && node.getSonsList().size() > 0)	//c'è almeno una mossa disponibile
		{
			for (DamaTree son : node.getSonsList())
			{
				ArrayList<DamaTree> sons_list = Moves.getAllPossibleMoves(son, ((son.getDepth() % 2) == 0) ? Moves.BLACK : Moves.WHITE);
				son.setSons(sons_list);
				recursiveSonsInitialization(son);
			}
		}
	}
	
	private DamaTree MiniMax(DamaTree node, int depth, int turn)
	{
		if (node != null)
		{
			if (node.getDepth() != depth)
			{
				for (DamaTree son : node.getSonsList())
					MiniMax(son, depth, turn);
			}
			else
			{
				PriorityQueue<DamaTree> queue;
				
				if ((depth % 2 != 0 && turn == Moves.BLACK) || (depth % 2 == 0 && turn == Moves.WHITE))		//get min
				{
					queue = new PriorityQueue<DamaTree>(MAXDIM_QUEUE, new Comparator<DamaTree>() {
															          public int compare(DamaTree o1, DamaTree o2) 
															          {
															        	  return o1.getScore() - o2.getScore();
													
															          } });
				}
				else 					//get max
				{
					queue = new PriorityQueue<DamaTree>(MAXDIM_QUEUE, new Comparator<DamaTree>() {
															          public int compare(DamaTree o1, DamaTree o2) 
															          {
															        	  return o2.getScore() - o1.getScore();
													
															          } });
				}
				
				for (DamaTree son : node.getSonsList())		//figli già valutati
					queue.add(son);
				
				DamaTree el = queue.poll();
				if (el == null)
					return null;
				
				node.setScore(el.getScore());
				return el;
			}
		}
		
		return null;
	}
	
	private void detectMoveDeletingForEat(ArrayList<DamaTree> list)
	{
		boolean exists_eat = false;
		
		for (DamaTree el : list)
		{
			if (el.moveType == Action.EAT)
			{
				exists_eat = true;
				break;
			}
		}
		
		if (!exists_eat)
			return;
		
		for (int i = list.size() - 1; i >= 0; i--)
			if (list.get(i).moveType == Action.MOVE)
				list.remove(i);

	}
	
	public boolean builtSearchTree(int[][] start_matrix, int turn)
	{
		/*** set time ***/
		long Tempo1, Tempo2;
		Tempo1 = System.currentTimeMillis();
		
		this.radixOfTree = new DamaTree(start_matrix);
		ArrayList<DamaTree> sons_list = Moves.getAllPossibleMoves(this.radixOfTree, turn);	//inizializza figli
		
		//se vi sono mangiate, elimino tutte le mosse di tipo "move" dalla lista in quanto le prime sono prioritarie
		detectMoveDeletingForEat(sons_list);
		
		if (sons_list.size() > 0)
		{
			/*** very tree built ***/
			this.radixOfTree.setSons(sons_list);
			recursiveSonsInitialization(this.radixOfTree);
			
			/*** Selecting best max for black move ***/
			DamaTree max_depth_node = null;
			for (int i = MAXDEPTH - 1; i >= 0; i--)
				max_depth_node = MiniMax(this.radixOfTree, i, turn);
			
			int[][] m = null;
			if (max_depth_node != null)
			{
				m = max_depth_node.getMatrix();
				/*** get time ***/
				Tempo2 = System.currentTimeMillis();
				System.out.println(max_depth_node.moveType + " - " + (Tempo2 - Tempo1) + "ms");
			}
			
			scacchiera.updateMatrix(m);
			return true;
		}
		else
		{
			if (turn == Moves.WHITE)
			{
				System.out.println("no moves for WHITE player: BLACK WINS");
				JOptionPane.showMessageDialog(null, "Nessuna mossa disponibile per il BIANCO: Vince il NERO");
			}
			else
			{
				System.out.println("no moves for BLACK player: WHITE WINS");
				JOptionPane.showMessageDialog(null, "Nessuna mossa disponibile per il NERO: Vince il BIANCO");
			}
			
			return false;
		}
	}
	
	private static void print_matrix(int[][] m)
	{
		for (int i=0; i < 8; i++)
		{
			for (int j = 0; j < 8; j++)
				System.out.print(m[i][j] + ", ");
			System.out.print("\n");
		}
		
		System.out.print("\n\n");
	}
}
