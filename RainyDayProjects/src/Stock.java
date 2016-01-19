import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Stock {

	String f = "";
	String symbol = "";
	ArrayList<Double> prices = new ArrayList<Double>();
	static GraphPanel buy;

	static double threshold = 0.02;
	String slope;
	int slopePerReadings = 10;
	int readings = -1;
	int wipes = 0;

	public Stock(String symbol) {
		this.symbol = symbol;
		f = System.getProperty("user.dir") + "/src/" + symbol + "Memory.txt";
		buy = new GraphPanel("Stock: " + symbol);
		buy.setTop(threshold);
		buy.setBottom(threshold);
		once();
	}

	public void once() {

		String memory = StockQuote.readFile(f);
		double price;
		for (String line : memory.split("\n")) {
			if (line != "") {
				price = Double.parseDouble(line.split(":")[0].trim());

				setData(price);
			}
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int minutes = calendar.get(Calendar.MINUTE);
		while (minutes != 59) {
			minutes = calendar.get(Calendar.MINUTE);
			// Align on the hour
		}
		while (true) {
			daily();
			try {
				Thread.sleep(1000 * 60 * 60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void daily() {
		Date cur = new Date();

		StockQuote.toUTC(cur);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(cur);
		int hours = calendar.get(Calendar.HOUR);
		if (hours >= 14 && hours <= 21) {
			double price = StockQuote.priceOf(symbol);
			StockQuote.writeFile(price + " : " + StockQuote.dateOf(symbol), f);
			setData(price);
		}
	}

	public void setData(double price) {
		Calendar cal = Calendar.getInstance();
		int maxReadingsPerMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) * 7; // 7
																					// readings
																					// per
																					// day
		System.out.println("Stock: " + symbol + ". Price: $" + price);
		prices.add(price);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);

		if (readings > slopePerReadings - 1 || wipes > 0) {
			ArrayList<Double> lastDays = new ArrayList<Double>();
			for (int i = prices.size() - slopePerReadings; i < prices.size(); i++) {
				lastDays.add(prices.get(i));
			}

			slope = nf.format(StockQuote.generateSlope(lastDays));
			if (wipes == 0) {
				buy.plotPoint(readings - slopePerReadings, Double.valueOf(slope));
			} else {
				buy.plotPoint(readings, Double.valueOf(slope));
			}
		}

		readings++;

		if (readings > maxReadingsPerMonth) {
			wipes++;
			readings = 0;
			buy.wipe();
		}
	}
}