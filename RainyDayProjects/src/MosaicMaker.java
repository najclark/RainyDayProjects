import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class MosaicMaker implements ActionListener {

	private JFrame frame;
	private HashMap<String, Color> map;
	private BufferedImage main, product;
	private int scaleFactor = 0, y = 0;
	private JButton bMake, bMain, bFiller;
	private JEditorPane dtrpnOutput;
	private JProgressBar pg;
	private JPanel panel_1, panel_2;
	private JTextField tScaleFactor;
	private Canvas canvas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MosaicMaker window = new MosaicMaker();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MosaicMaker() {
		initialize();
		
		bMake.addActionListener(this);
		bMain.addActionListener(this);
		bFiller.addActionListener(this);
		
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		map = new HashMap<String, Color>();

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 12, 428, 248);
		frame.getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		bMain = new JButton("Main Image");
		panel.add(bMain, BorderLayout.WEST);
		
		bFiller = new JButton("Filler Images");
		panel.add(bFiller, BorderLayout.EAST);
		
		bMake = new JButton("Make");
		panel.add(bMake, BorderLayout.CENTER);
		bMake.setEnabled(false);
		
		pg = new JProgressBar(0, 100);
		panel.add(pg, BorderLayout.SOUTH);
		pg.setStringPainted(true);
		
		dtrpnOutput = new JEditorPane();
		dtrpnOutput.setText("output");
		panel.add(dtrpnOutput, BorderLayout.NORTH);
		dtrpnOutput.setEditable(false);
		
		panel_1 = new JPanel();
		panel_1.setBounds(10, 0, 428, 15);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));
		
		tScaleFactor = new JTextField();
		panel_1.add(tScaleFactor);
		tScaleFactor.setColumns(10);
		
		panel_2 = new JPanel();
		panel_2.setBounds(10, 272, frame.getWidth(), frame.getHeight()-panel.getHeight()+panel.getY());
		frame.getContentPane().add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		canvas = new Canvas();
		canvas.setBounds(0, 0, panel_2.getWidth(), panel_2.getHeight());
		panel_2.add(canvas, BorderLayout.CENTER);
	
		
		tScaleFactor.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				if(tScaleFactor.getText().equals("")){
					tScaleFactor.setForeground(Color.gray);
					tScaleFactor.setText("Scale Factor");
				}
				else if (!map.isEmpty() && main != null && !tScaleFactor.getText().equals("Scale Factor")) {
					bMake.setEnabled(true);
				}
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				if(tScaleFactor.getText().equals("Scale Factor")){
					tScaleFactor.setForeground(Color.black);
					tScaleFactor.setText("");
				}
			}
		});
		
	}

    protected void paintComponent(Graphics g, BufferedImage b) {
        g.drawImage(b, 0, 0, null); // see javadoc for more info on the parameters            
    }
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String btn = ((JButton) ae.getSource()).getText();
		
		if (btn.equals("Main Image")) {
			JButton open = new JButton();
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Select a File");
			fc.setMultiSelectionEnabled(false);
			if (fc.showOpenDialog(open) == JFileChooser.APPROVE_OPTION) {

				String ftoz = fc.getSelectedFile().getAbsolutePath();

				main = (BufferedImage) NickUtils.getImage(ftoz);

			}

			if (!map.isEmpty() && main != null && !tScaleFactor.getText().equals("Scale Factor")) {
				bMake.setEnabled(true);
			}
		} else if (btn.equals("Make")) {

			if(tScaleFactor.getText().equals("Scale Factor") || Integer.parseInt(tScaleFactor.getText()) < 0){
				JOptionPane.showMessageDialog(null, "Set the scale factor!");
				return;
			}
			
			scaleFactor = Integer.parseInt(tScaleFactor.getText());
			
			String path = JOptionPane.showInputDialog("Path to output to:");

			product = new BufferedImage(scaleFactor * main.getWidth(), scaleFactor * main.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			Graphics2D g = product.createGraphics();
			panel_2.setSize(500, 500);
			canvas.setSize(500, 500);
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					int xOffset = 0, yOffset = 0;
					long start, time;
					
					for (; y < main.getHeight(); y++) {
						start = System.currentTimeMillis();
						yOffset = y * scaleFactor;
						for (int x = 0; x < main.getWidth(); x++) {

							Color m = new Color(main.getRGB(x, y));
							xOffset = x * scaleFactor;

							String closest = "";
							int closestTotal = Integer.MAX_VALUE;
							for (String key : map.keySet()) {
								Color filler = map.get(key);
								int difference = Math.abs(filler.getRed() - m.getRed())
										+ Math.abs(filler.getGreen() - m.getGreen()) + Math.abs(filler.getBlue() - m.getBlue());

								if (difference < closestTotal) {
									closestTotal = difference;
									closest = key;
								}
							}

							g.drawImage(NickUtils.getImage(closest), xOffset, yOffset, scaleFactor, scaleFactor, null);
							paintComponent(canvas.getGraphics(), (BufferedImage) NickUtils.resizeImage(product, canvas.getWidth(), canvas.getHeight()));
						}

						time = (System.currentTimeMillis() - start) / 1000;
						
						dtrpnOutput.setText("Minutes remaining: " + NickUtils.etaMinutes(y, main.getHeight(), time) + ". Row: "
								+ y + "/" + main.getHeight() + ".");
						pg.setValue((int) (y * 100 / main.getHeight()));
					}
					
					g.dispose();
//					Graphics g2 = canvas.getGraphics();
//					canvas.getGraphics().drawImage(product, 0, 0, null);
					NickUtils.saveImage(product, path, "png");
					dtrpnOutput.setText("Done!");
					
				}
			}).start();
			



		} else if (btn.equals("Filler Images")) {
			JButton open = new JButton();
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Select some Files");
			fc.setMultiSelectionEnabled(true);
			if (fc.showOpenDialog(open) == JFileChooser.APPROVE_OPTION) {

				File[] files = fc.getSelectedFiles();
				String[] ftoz = new String[files.length];
				int index = 0;
				for (File f : files) {
					ftoz[index] = f.getAbsolutePath();
					index++;
				}

				for (String f : ftoz) {
					map.put(f, NickUtils.getAvgColor(NickUtils.toBufferedImage(NickUtils.getImage(f))));
				}

			}
			if (!map.isEmpty() && main != null && !tScaleFactor.getText().equals("Scale Factor")) {
				bMake.setEnabled(true);
			}
		}

	}
}
