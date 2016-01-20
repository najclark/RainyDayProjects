import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class StockGUI {

	int threads = 0;
	
	public static void main(String[] args) {
		new StockGUI().setup();
	}
	
	public void setup() {
		JFrame frame = new JFrame("Stock Analyzer");
		JPanel panel = new JPanel();
		JButton button = new JButton("Add Stock");
		
		frame.add(panel);
		panel.add(button);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String symbol = JOptionPane.showInputDialog("Stock Symbol");
				String f = System.getProperty("user.dir") + "/src/" + symbol + "Memory.txt";
				if(!StockQuote.fileExists(new File(f))) {
					StockQuote.createFile(f);
				}
				threads++;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						Stock s = new Stock(symbol, 60-threads);
					}
				}).start();
			}
		});
	}
}
