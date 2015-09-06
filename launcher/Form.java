package launcher;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

@SuppressWarnings("serial")
public class Form extends JFrame{

	private final int squaredim = 80;
	private Scacchiera scacchiera;
	
	public Form(String txt)
	{
		super(txt);
		this.setLocation(new Point(300, 15));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenu();
		this.setSize(this.squaredim * 8, (this.squaredim * 8) + 45);
		loadScacchiera();
	}
	
	private void loadScacchiera()
	{
		scacchiera = new Scacchiera();
		this.getContentPane().add(scacchiera);
	}
	
	private void createMenu()
	{
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Game");
		menuBar.add(menu);
		
		//submenu's
		JMenuItem menuItem1 = new JMenuItem("Play Human vs CPU");
		menuItem1.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) 
            {
            	scacchiera.resetStatus();
            	scacchiera.HUMANvsCPU = true;
            	System.out.println("HUMANvsCPU mode running...");
            }
		});
		
		JMenuItem menuItem2 = new JMenuItem("Play CPU vs CPU");
		menuItem2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) 
            {
            	scacchiera.resetStatus();
            	scacchiera.CPUvsCPU = true;
            	System.out.println("CPUvsCPU mode running...");
            	scacchiera.playCPUvsCPU();
            }
		});
		JMenuItem menuItem3 = new JMenuItem("Stop game");
		menuItem3.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) 
            {
            	scacchiera.resetStatus();
            	System.out.println("Game stopped\n\n");
            }
		});
		menu.add(menuItem1);
		menu.add(menuItem2);
		menu.add(menuItem3);
		
		this.setJMenuBar(menuBar);
	}
}
