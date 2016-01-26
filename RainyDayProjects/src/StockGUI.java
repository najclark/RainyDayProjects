import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class StockGUI {

	int threads = 0;
	JFrame frame;
	JPanel panel;

	public static void main(String[] args) {
		new StockGUI().setup();
	}

	public void setup() {
		frame = new JFrame("Stock Analyzer");
		panel = new JPanel();
		JButton button = new JButton("Add Stock");
		GridLayout gl = new GridLayout(4, 1);
		gl.setColumns(1);
		panel.setLayout(gl);
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
				if (!StockQuote.fileExists(new File(f))) {
					StockQuote.createFile(f);
				}
				threads++;
				new Thread(new Runnable() {

					@Override
					public void run() {
						Stock s = new Stock(symbol, 60 - threads);
						JButton btn = new JButton(symbol);
						btn.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								s.showHistory();
							}
						});
						panel.add(btn);
						if (threads % 2 == 0) {
							frame.setSize(frame.getWidth() - 1, frame.getHeight() - 1);
						} else {
							frame.setSize(frame.getWidth() + 1, frame.getHeight() + 1);
						}
						s.once();
					}
				}).start();
			}
		});
	}
}
