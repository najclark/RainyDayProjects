import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Stock {

	String f = System.getProperty("user.dir") + "/src/memory.txt";
	static String symbol = "goog";
	ArrayList<Double> prices = new ArrayList<Double>();
	static GraphPanel gp, buy;

	static double threshold = 0.02;
	String slope;
	int slopePerDays = 10;
	int readings = -1;
	int wipes = 0;
	Date open, close;

	public static void main(String[] args) {
		gp = new GraphPanel("Stock: " + symbol);
		buy = new GraphPanel("Bot: " + symbol);
		gp.setTop(0);
		gp.setBottom(0);
		buy.setTop(threshold);
		buy.setBottom(threshold);
		new Stock().once();
	}

	public void once() {
		open = new Date();
		close = new Date();

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

		while (new Date().getHours() != 24)
			; // Align on the day
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				open = new Date();
				close = new Date();

				open = StockQuote.toUTC(open);
				close = StockQuote.toUTC(close);

				open.setHours(14);
				open.setMinutes(30);
				close.setHours(21);

			}
		}, 0l, 1000 * 60 * 60 * 24);
	}

	public void daily() {
		Date cur = new Date();

		cur = StockQuote.toUTC(cur);

		if (cur.after(open) && cur.before(close)) {
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

		prices.add(price);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(5);

		if (readings > slopePerDays - 1 || wipes > 0) {
			ArrayList<Double> lastDays = new ArrayList<Double>();
			for (int i = prices.size() - slopePerDays; i < prices.size(); i++) {
				lastDays.add(prices.get(i));
			}

			slope = nf.format(StockQuote.generateSlope(lastDays));
			if (wipes == 0) {
				gp.plotPoint(readings - slopePerDays, price);
				buy.plotPoint(readings - slopePerDays, Double.valueOf(slope));
			} else {
				gp.plotPoint(readings, price);
				buy.plotPoint(readings, Double.valueOf(slope));
			}
		}

		readings++;

		if (readings > maxReadingsPerMonth) {
			wipes++;
			readings = 0;
			buy.wipe();
			gp.wipe();
		}
	}
}