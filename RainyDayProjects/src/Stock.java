import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Stock {

	String f = "";
	static String symbol = "goog";
	ArrayList<Double> prices = new ArrayList<Double>();
	static GraphPanel buy;

	static double threshold = 0.02;
	String slope;
	int slopePerReadings = 10;
	int readings = -1;
	int wipes = 0;

	public static void main(String[] args) {
		buy = new GraphPanel("Bot: " + symbol);
		buy.setTop(threshold);
		buy.setBottom(threshold);
		new Stock("goog").once();
	}
	
	public Stock(String symbol) {
		this.symbol = symbol;
		f = System.getProperty("user.dir") + "/src/" + symbol + "Memory.txt";
		buy = new GraphPanel("Stock: " + symbol);
		buy.setTop(threshold);
		buy.setBottom(threshold);
		once();
	}

	public void once() {

		Timer timer = new Timer();
		String memory = StockQuote.readFile(f);
		double price;
		for (String line : memory.split("\n")) {
			if (line != "") {
				price = Double.parseDouble(line.split(":")[0].trim());

				setData(price);
			}
		}

		TimerTask hourlyTask = new TimerTask() {
			@Override
			public void run() {
				daily();
			}
		};
		while (new Date().getMinutes() != 59)
			; // Align on the hour
		timer.schedule(hourlyTask, 0l, 1000 * 60 * 60);
	}

	public void daily() {
		Date cur = new Date();

		StockQuote.toUTC(cur);

		if (cur.getHours() > 14 && cur.getHours() < 21) {
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
		System.out.println("Stock: " + symbol + ". Price: $" + price + ". " + StockQuote.toUTC(new Date()).toString());
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