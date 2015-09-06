package engine;

import java.awt.Point;
import java.util.ArrayList;
import structure.*;

public class Moves 
{
	private static int tmpx,tmpy;
	
	public final static int DIM = 8;
	public final static int FREE = 0;
	public final static int BLACK = 1;
	public final static int WHITE = -1;
	public final static int INACCESSIBLE = -5;
	public final static int D_BLACK = 2;
	public final static int D_WHITE = -2;
	public final static int EATFACTOR = 50;
	
	public enum Action
	{
		EAT,
		MOVE
	}
	
	/*** Effettua una copia esatta della matrice ***/
	private static int[][] cloneMatrix(int[][] matrix)
	{
		int[][] cloned = new int[DIM][DIM];
		for (int row = 0; row < DIM; row++)
			for (int col = 0; col < DIM; col++)
				cloned[row][col] = matrix[row][col];
		
		return cloned;
	}
	
	/**** methods for EAT TREE ****/
	private static boolean isOfColor(EatTree node, int color)
	{
		return (node != null && node.value == color);
	}
	
	private static boolean isNodeFree(EatTree node)
	{
		return (node != null && node.value == FREE);
	}
	
	public static void setLastEatPosition(EatTree start_cell, int depth, int enemy)
	{
		boolean eat_sx = (isOfColor(start_cell.sx, enemy) && isNodeFree(start_cell.sx.sx));
		boolean eat_dx = (isOfColor(start_cell.dx, enemy) && isNodeFree(start_cell.dx.dx));
		
		if (!eat_sx && !eat_dx)
		{
			start_cell.setDepth(depth);
			return;
		}
		else
		{
			if (eat_sx)
				setLastEatPosition(start_cell.sx.sx, depth + 1, enemy);
			
			if (eat_dx)
				setLastEatPosition(start_cell.dx.dx, depth + 1, enemy);
		}
	}
	
	public static void setLastEatPositionAdvanced(EatTree start_cell, int depth, int enemy_pedina, int enemy_dama)
	{
		boolean eat_sx = ((isOfColor(start_cell.sx, enemy_pedina) || isOfColor(start_cell.sx, enemy_dama)) && isNodeFree(start_cell.sx.sx));
		boolean eat_dx = ((isOfColor(start_cell.dx, enemy_pedina) || isOfColor(start_cell.dx, enemy_dama)) && isNodeFree(start_cell.dx.dx));
		
		if (!eat_sx && !eat_dx)
		{
			start_cell.setDepth(depth);
			return;
		}
		else
		{
			if (eat_sx)
				setLastEatPositionAdvanced(start_cell.sx.sx, depth + 1, enemy_pedina, enemy_dama);
			
			if (eat_dx)
				setLastEatPositionAdvanced(start_cell.dx.dx, depth + 1, enemy_pedina, enemy_dama);
		}
	}
	
	/*** Ricerca della mangiata migliore per la dama ---LIMITED---
	 * 
	 * @param value colore della dama da cui partire;
	 * @param loc posizione della scacchiera della dama di partenza;
	 * @param matrix matrice completa (clone);
	 * @return la casella di arrivo oppure null se non vi sono mangiate disponibili.
	 */
	public static EatTree searchMaxDepthDamaEat(int value, Point loc, int[][] matrix)
	{
		int enemy_pedina = (value == D_WHITE) ? BLACK : WHITE;
		int enemy_dama = (value == D_WHITE) ? D_BLACK : D_WHITE;
		EatTree up_treePED = new EatTree(matrix, loc, true, null, Engine.MAXDEPTH);
		EatTree down_treePED = new EatTree(matrix, loc, false, null, Engine.MAXDEPTH);
		EatTree up_treeDAMA = new EatTree(matrix, loc, true, null, Engine.MAXDEPTH);
		EatTree down_treeDAMA = new EatTree(matrix, loc, false, null, Engine.MAXDEPTH);
		
		setLastEatPositionAdvanced(up_treePED, 0, enemy_pedina, enemy_dama);
		setLastEatPositionAdvanced(down_treePED, 0, enemy_pedina, enemy_dama);
		setLastEatPositionAdvanced(up_treeDAMA, 0, enemy_dama, enemy_dama);
		setLastEatPositionAdvanced(down_treeDAMA, 0, enemy_dama, enemy_dama);
		
		EatTree max_depthUP_PED = up_treePED.getMaxDepthNode();
		EatTree max_depthDOWN_PED = down_treePED.getMaxDepthNode();
		EatTree max_depthUP_DAMA = up_treeDAMA.getMaxDepthNode();
		EatTree max_depthDOWN_DAMA = down_treeDAMA.getMaxDepthNode();
		
		int max = Math.max(Math.max(max_depthUP_PED.depth, max_depthDOWN_PED.depth), Math.max(max_depthUP_DAMA.depth, max_depthDOWN_DAMA.depth));

		if (max <= 0)
			return null;
		else
		{
			if (max == max_depthUP_PED.depth)
				return max_depthUP_PED;
			else
				if (max == max_depthDOWN_PED.depth)
					return max_depthDOWN_PED;
				else
					if (max == max_depthUP_DAMA.depth)
						return max_depthUP_DAMA;
					else
						if (max == max_depthDOWN_DAMA.depth)
							return max_depthDOWN_DAMA;
						else
							return null;
		}
	}
	
	/**** END methods for EAT TREE ****/
	
	public static boolean inRange(int i, int j)
	{
		return (i > -1 && i < DIM && j > -1 && j < DIM);
	}
	
	private static boolean isDama(int[][] matrix, int i, int j)
	{
		return (matrix[i][j] == D_BLACK || matrix[i][j] == D_WHITE);
	}
	
	private static boolean isFree(int[][] matrix, int i, int j)
	{
		return (inRange(i,j) && matrix[i][j] == FREE);
	}
	
	/*** Controlla se la pedina di turno "turn" spostandosi in posizione "end_pos" nella matrice "cloned_matrix"
	 * diventa oppure no una dama.
	 * @param cloned_matrix
	 * @param turn
	 * @param end_pos
	 */
	private static void checkBeDama(int[][] cloned_matrix, Point end_pos)
	{
		for (int y = 0; y < DIM; y++)
		{
			if ((y % 2) != 0 && cloned_matrix[7][y] == BLACK)
				cloned_matrix[end_pos.x][end_pos.y] = D_BLACK;
			else	
				if ((y % 2) == 0 && cloned_matrix[0][y] == WHITE)
					cloned_matrix[end_pos.x][end_pos.y] = D_WHITE;
		}
	}
	
	/*** Controlla se la pedina in posizione matrix[i][j] è impossibilitata a muoversi a causa:
	 * del bordo scacchiera;
	 * della presenza di una pedina della propria squadra che la blocca;
	 * NON vengono invece considerate gli "blocchi" da parte di pedine della squadra avversaria, in quanto potrebbero
	 * risultare mangiate.
	 * @param matrix
	 * @param i
	 * @param j
	 * @param turn
	 * @return
	 */
	private static boolean isLocked(int[][] matrix, int i, int j, int turn)
	{
		if (!isDama(matrix,i,j))
		{
			boolean locked_dx, locked_sx;
			
			if (matrix[i][j] == BLACK)
			{
				locked_dx = (!inRange(i+1,j+1) || matrix[i][j] == matrix[i+1][j+1]);
				locked_sx = (!inRange(i+1,j-1) || matrix[i][j] == matrix[i+1][j-1]);
			}
			else
			{
				locked_dx = (!inRange(i-1,j+1) || matrix[i][j] == matrix[i-1][j+1]);
				locked_sx = (!inRange(i-1,j-1) || matrix[i][j] == matrix[i-1][j-1]);
			}
			
			return (locked_dx && locked_sx);
		}
		else
			return false;
	}
	
	/*** Verifica la possibilità di spostare la pedina nella casella di sx, in base al proprio turno.
	 * 
	 * @param matrix
	 * @param i
	 * @param j
	 * @param turn
	 * @return true  se la casella alla propria sx è vuota.
	 * @return false se la pedina è impossibilitata a spostarsi a sx.
	 */
	private static boolean canMoveSx(int[][] matrix, int i, int j, int turn)
	{
		tmpy = (j - 1);
		
		if (turn == BLACK)
			tmpx = (i + 1);
		else
			tmpx = (i - 1);
		
		return isFree(matrix,tmpx,tmpy);
	}
	
	/*** Verifica la possibilità di spostare la pedina nella casella di dx, in base al proprio turno.
	 * 
	 * @param matrix
	 * @param i
	 * @param j
	 * @param turn
	 * @return true  se la casella alla propria dx è vuota.
	 * @return false se la pedina è impossibilitata a spostarsi a dx.
	 */
	private static boolean canMoveDx(int[][] matrix, int i, int j, int turn)
	{
		tmpy = (j + 1);
		
		if (turn == BLACK)
			tmpx = (i + 1);
		else
			tmpx = (i - 1);
		
		return isFree(matrix,tmpx,tmpy);
	}
	
	private static boolean canEatDama(int[][] matrix, int i, int j, int turn)
	{
		int enemy_pedina = (turn == WHITE) ? BLACK : WHITE;
		int enemy_dama = (turn == WHITE) ? D_BLACK : D_WHITE;
		boolean eatdownsx, eatdowndx, eatupsx, eatupdx;

		eatdownsx = (inRange(i+1,j-1) && (matrix[i+1][j-1] == enemy_pedina || matrix[i+1][j-1] == enemy_dama) && isFree(matrix, i+2, j-2));
		eatdowndx = (inRange(i+1,j+1) && (matrix[i+1][j+1] == enemy_pedina || matrix[i+1][j+1] == enemy_dama) && isFree(matrix, i+2, j+2));
		eatupsx =   (inRange(i-1,j-1) && (matrix[i-1][j-1] == enemy_pedina || matrix[i-1][j-1] == enemy_dama) && isFree(matrix, i-2, j-2));
		eatupdx =   (inRange(i-1,j+1) && (matrix[i-1][j+1] == enemy_pedina || matrix[i-1][j+1] == enemy_dama) && isFree(matrix, i-2, j+2));

		return (eatdownsx || eatdowndx || eatupdx || eatupsx);
	}
	
	private static boolean canEat(int[][] matrix, int i, int j, int turn)
	{
		int enemy = (turn == WHITE) ? BLACK : WHITE;
		boolean eatdx, eatsx;
		
		if (turn == BLACK)
		{
			eatdx = (inRange(i+1,j+1) && matrix[i+1][j+1] == enemy && isFree(matrix, i+2, j+2));
			eatsx = (inRange(i+1,j-1) && matrix[i+1][j-1] == enemy && isFree(matrix, i+2, j-2));
		}
		else
		{
			eatdx = (inRange(i-1,j+1) && matrix[i-1][j+1] == enemy && isFree(matrix, i-2, j+2));
			eatsx = (inRange(i-1,j-1) && matrix[i-1][j-1] == enemy && isFree(matrix, i-2, j-2));
		}
		
		return (eatdx || eatsx);
	}

	private static void updateMultipleEat(int[][] cloned_matrix, EatTree end_node, Point start_pos)
	{
		Point end_pos = end_node.getLocation();
		cloned_matrix[end_pos.x][end_pos.y] = cloned_matrix[start_pos.x][start_pos.y];
		checkBeDama(cloned_matrix, end_pos);		//è diventata dama??
		
		while (end_node.father != null)
		{
			end_node = end_node.father;
			end_pos = end_node.getLocation();
			cloned_matrix[end_pos.x][end_pos.y] = FREE;
		}
	}
	
	/*** Esegue la mossa sulla matrice clonata.
	 * 
	 * @param s_i row start index 
	 * @param s_j column start index 
	 * @param f_i row end index 
	 * @param f_j column end index 
	 * @return cloned  la matrice sulla quale ho eseguito la mossa (s_i, s_j) --> (f_i, f_j)
	 */
	private static int[][] executeMoveOnCloned(int[][] matrix, int s_i, int s_j, int f_i, int f_j)
	{
		int[][] cloned = cloneMatrix(matrix);
		cloned[f_i][f_j] = cloned[s_i][s_j];
		cloned[s_i][s_j] = FREE;
		
		checkBeDama(cloned, new Point(f_i,f_j));		//è diventata dama??
		
		return cloned;
	}
	
	/*** Calcola i figli di "radix" in base alle mosse possibili e restituisce il vettore di figli **/
	public static ArrayList<DamaTree> getAllPossibleMoves(DamaTree radix, int turn)
	{
		ArrayList<DamaTree> moves = new ArrayList<DamaTree>();
		int damaOfTurn, opponent;
		
		if (turn == BLACK)
		{
			damaOfTurn = D_BLACK;
			opponent = WHITE;
		}
		else
		{
			damaOfTurn = D_WHITE;
			opponent = BLACK;
		}
		
		int[][] tmp_matrix = radix.getMatrix();
		boolean force_eat = false;
		
		for (int row = 0; row < DIM; row++)
			for (int col = 0; col < DIM; col++)
				if ((row % 2) == (col % 2) && (tmp_matrix[row][col] == turn || tmp_matrix[row][col] == damaOfTurn) && !isLocked(tmp_matrix, row, col, turn))
				{
					if (isDama(tmp_matrix, row, col))	// DAME
					{
						if (canEatDama(tmp_matrix, row, col, turn))
						{
							force_eat = true;
							int[][] cloned = cloneMatrix(tmp_matrix);
							EatTree max_depth_node = searchMaxDepthDamaEat(cloned[row][col], new Point(row,col), cloned);
							
							int score = 0;
							if (turn == WHITE)
								score = -(max_depth_node.depth * EATFACTOR);	//force eating select in tree
							else
								score = max_depth_node.depth * EATFACTOR;
							
							updateMultipleEat(cloned, max_depth_node, new Point(row,col));
							score += Engine.valuteBalanced(cloned);	//applico la funzione di valutazione per lo stato in questione
							moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.EAT));
						}
						
						if (!force_eat)
						{
							if (canMoveSx(tmp_matrix, row, col, turn))
							{
								int[][]cloned = executeMoveOnCloned(tmp_matrix, row, col, tmpx, tmpy);
								int score = Engine.valuteBalanced(cloned); 	//applico la funzione di valutazione per lo stato in questione
								
								moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.MOVE));
							}
							
							if (canMoveDx(tmp_matrix, row, col, turn))
							{
								int[][]cloned = executeMoveOnCloned(tmp_matrix, row, col, tmpx, tmpy);
								int score = Engine.valuteBalanced(cloned); 	//applico la funzione di valutazione per lo stato in questione
								
								moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.MOVE));
							}
							
							if (canMoveDx(tmp_matrix, row, col, opponent))
							{
								int[][]cloned = executeMoveOnCloned(tmp_matrix, row, col, tmpx, tmpy);
								int score = Engine.valuteBalanced(cloned); 	//applico la funzione di valutazione per lo stato in questione
								
								moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.MOVE));
							}
							
							if (canMoveSx(tmp_matrix, row, col, opponent))
							{
								int[][]cloned = executeMoveOnCloned(tmp_matrix, row, col, tmpx, tmpy);
								int score = Engine.valuteBalanced(cloned); 	//applico la funzione di valutazione per lo stato in questione
								
								moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.MOVE));
							}
						}
					}
					else		//PEDINE
					{
						if (canEat(tmp_matrix, row, col, turn))
						{
							force_eat = true;
							int[][] cloned = cloneMatrix(tmp_matrix);
							EatTree tree = new EatTree(cloned, new Point(row, col), (turn == WHITE), null, 4);
							setLastEatPosition(tree, 0, (turn == BLACK) ? WHITE : BLACK);	//switch enemy
							EatTree max_depth_node = tree.getMaxDepthNode();
							int score = 0;
							
							if (turn == WHITE)
								score = -(max_depth_node.depth * EATFACTOR);	//force eating select in tree
							else
								score = max_depth_node.depth * EATFACTOR;
							
							updateMultipleEat(cloned, max_depth_node, new Point(row,col));	//application of tree results-eats
							score += Engine.valuteBalanced(cloned);	//applico la funzione di valutazione per lo stato in questione
							
							moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.EAT));
						}
						
						if (!force_eat)
						{
							if (canMoveSx(tmp_matrix, row, col, turn))
							{
								int[][]cloned = executeMoveOnCloned(tmp_matrix, row, col, tmpx, tmpy);
								int score = Engine.valuteBalanced(cloned); 	//applico la funzione di valutazione per lo stato in questione
								
								moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.MOVE));
							}
							
							if (canMoveDx(tmp_matrix, row, col, turn))
							{
								int[][]cloned = executeMoveOnCloned(tmp_matrix, row, col, tmpx, tmpy);
								int score = Engine.valuteBalanced(cloned); 	//applico la funzione di valutazione per lo stato in questione
								
								moves.add(new DamaTree(cloned, radix, radix.getDepth() + 1, score, Action.MOVE));
							}
						}
					}
				}
		
		return moves;	
	}
}
