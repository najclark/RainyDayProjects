import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;



public class Stock {
	// ----------------------------------------------------------------
	String f = "";
	String symbol = "";
	ArrayList<Double> prices = new ArrayList<Double>();
	static GraphPanel buy;

	static double threshold = 0.1;
	String slope;
	int slopePerReadings = 10;
	int readings = -1;
	int wipes = 0;
	int offset = 0;
	boolean warned = false;

	String username = "";
	String password = "";
	
	// ----------------------------------------------------------------
	public Stock(String symbol, int offset) {
		this.offset = offset;
		this.symbol = symbol;
		f = System.getProperty("user.dir") + "/src/" + symbol + "Memory.txt";
		buy = new GraphPanel("Stock: " + symbol);
		buy.setTop(threshold);
		buy.setBottom(threshold);
		System.out.println("Initialized Thread: " + (60 - offset));
	}

	// ----------------------------------------------------------------
	public void once() {

		String memory = StockQuote.readFile(f);
		double price;
		for (String line : memory.split("\n")) {
			if (line != "") {
				price = Double.parseDouble(line.split(":")[0].trim());
				setData(price);
			}
		}

		Calendar c = Calendar.getInstance();
		c.set(Calendar.SECOND, offset);

		long wait = c.getTimeInMillis() - System.currentTimeMillis();
		try {
			Thread.sleep(wait);
			System.out.println("Aligned Thread: " + (60 - offset));
			while (true) {
				daily();

				c.add(Calendar.MINUTE, 1);
				c.set(Calendar.SECOND, offset);
				wait = c.getTimeInMillis() - System.currentTimeMillis();
				Thread.sleep(wait);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------
	public void daily() {

		Calendar now = Calendar.getInstance();
		int hour = now.get(Calendar.HOUR_OF_DAY);

		int dow = now.get(Calendar.DAY_OF_WEEK);
		boolean isWeekday = ((dow >= Calendar.MONDAY) && (dow <= Calendar.FRIDAY));
		if (hour >= 7 && hour < 13 && isWeekday) {
			warned = false;
			double price = StockQuote.priceOf(symbol);
			StockQuote.writeFile(price + " : " + StockQuote.dateOf(symbol), f);
			setData(price);
			if (readings > slopePerReadings || wipes > 0) {
				if (Double.parseDouble(slope) > threshold) {
					if (!username.equals("")) {
						SendEmail
								.sendEmail("Stock: " + symbol,
										"The current stock has gone above the set threshold(" + threshold
												+ "). We are predicting " + symbol + " to drop soon.",
										username, password);
					}
				} else if (Double.parseDouble(slope) < -threshold) {
					if (!username.equals("")) {
						SendEmail
								.sendEmail("Stock: " + symbol,
										"The current stock has gone below the set threshold(" + -threshold
												+ "). We are predicting " + symbol + " to rise soon.",
										username, password);
					}
				}
			}
		} else if (!warned) {
			warned = true;
			System.out.println("Market closed! It reopens at: 7:00");
		}
	}

	// ----------------------------------------------------------------
	public void setData(double price) {
		prices.add(price);
		Calendar cal = Calendar.getInstance();
		int maxReadingsPerMonth = cal.getActualMaximum(Calendar.MINUTE) * 6;
		System.out.println("Stock: " + symbol + ". Price: $" + price + ". Slope: " + slope);
		StockQuote.writeFile("Stock: " + symbol + ". Price: $" + price + ". Slope: " + slope,
				System.getProperty("user.dir") + "/src/output.txt");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);

		if (readings > slopePerReadings - 1 || wipes > 0) {
			ArrayList<Double> lastDays = new ArrayList<Double>();
			for (int i = prices.size() - slopePerReadings; i < prices.size(); i++) {
				lastDays.add(prices.get(i));
			}

			slope = nf.format(StockQuote.generateSlope(lastDays));

			if (wipes == 0) {
				buy.plotPoint(Double.valueOf(slope));
			} else {
				buy.plotPoint(Double.valueOf(slope));
			}
		}

		readings++;

		if (readings > maxReadingsPerMonth) {
			wipes++;
			readings = 0;
			buy.wipe();
		}
	}
	// ----------------------------------------------------------------

	public void showHistory() {
		String memory = StockQuote
				.readFile(System.getProperty("user.dir") + "/src/" + symbol + "Memory.txt");

		GraphPanel gp = new GraphPanel("History: " + symbol);

		for (String line : memory.split("\n")) {
			if (line != "") {
				gp.plotPoint(Double.parseDouble(line.split(":")[0].trim()));
			}
		}
	}
}