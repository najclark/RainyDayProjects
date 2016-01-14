import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class Stock {

	String f = System.getProperty("user.dir") + "/src/memory.txt";
	static String symbol = "goog";

	/*
	 * How it works trends
	 * 
	 * 
	 */
	ArrayList<Double> prices = new ArrayList<Double>();
	ArrayList<Double> upTrend = new ArrayList<Double>(); // change if UP
	ArrayList<Double> downTrend = new ArrayList<Double>();// change if DOWN
	ArrayList<Double> avgTrend = new ArrayList<Double>(); // change if BOTH
	ArrayList<Double> avgHigh = new ArrayList<Double>(); // change if UP
	ArrayList<Double> avgLow = new ArrayList<Double>(); // change if DOWN
	static GraphPanel gp, buy;

	double multiplier = 1;
	double lastPrice = 0;
	double avg = 0;
	static double threshold = 0.02;
	double upStreak, downStreak;
	String slope;
	int streak, days = 0;
	int slopePerDays = 10;
	int smallDays = -1;
	int wipes = 0;
	int lastMaxDays = 0;

	public static void main(String[] args) {
		// StockQuote.generateStock(1000, 10);
		gp = new GraphPanel("Stock: " + symbol);
		buy = new GraphPanel("Bot: " + symbol);
		gp.setTop(0);
		gp.setBottom(0);
		buy.setTop(threshold);
		buy.setBottom(threshold);
		new Stock().once();
	}

	public void once() {
		String memory = StockQuote.readFile(f);
		lastPrice = Double.valueOf(memory.split("\n")[0].split(":")[0].trim());
		double price;
		for (String line : memory.split("\n")) {
			price = Double.parseDouble(line.split(":")[0].trim());

			setData(price, lastPrice);
			lastPrice = price;

			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();

			System.out.println("upTrend: " + StockQuote.avg(upTrend));
			System.out.println("downTrend: " + StockQuote.avg(downTrend));
			System.out.println("avgTrend: " + avg);
			System.out.println("avgHigh: " + StockQuote.avg(avgHigh));
			System.out.println("avgLow: " + StockQuote.avg(avgLow));
			System.out.println("upStreak: " + upStreak);
			System.out.println("downStreak: " + downStreak);
			System.out.println("streak: " + streak);
			System.out.println("Price: $" + price);
			System.out.println("Slope: " + slope);
			System.out.println("Days: " + days);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// daily();
	}

	public void daily() {
		double price = StockQuote.priceOf(symbol);
		StockQuote.writeFile(price + " : " + StockQuote.dateOf(symbol), f);
		setData(price, lastPrice);

		System.out.println("upTrend: " + StockQuote.avg(upTrend));
		System.out.println("downTrend: " + StockQuote.avg(downTrend));
		System.out.println("avgTrend: " + StockQuote.avg(avgTrend));
		System.out.println("avgHigh: " + StockQuote.avg(avgHigh));
		System.out.println("avgLow: " + StockQuote.avg(avgLow));
		// System.out.println("upStreak: " + StockQuote.avg(upStreak));
		// System.out.println("downStreak: " + StockQuote.avg(downStreak));
		System.out.println("streak: " + streak);
		System.out.println("Days: " + days);
	}

	public void setData(double price, double lastPrice) {
		Calendar cal = Calendar.getInstance();
		int maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		int maxDaysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);

		prices.add(price);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		double diff = price - lastPrice;
		if (diff > 0) {
			if (streak < 0) {
				downStreak = (downStreak + (streak * -1)) / 2;
				// downStreak.add((double) (streak * -1));
				avgLow.add(price);
				streak = 0;
			}
			streak++;
			avgTrend.add((double) 1);
			avg++;
			upTrend.add(Double.parseDouble(nf.format(diff * multiplier)));
		} else if (diff < 0) {
			if (streak > 0) {
				upStreak = (upStreak + streak) / 2;
				// upStreak.add((double) streak);
				avgHigh.add(price);
				streak = 0;
			}
			streak--;
			avgTrend.add((double) -1);
			avg--;
			downTrend.add(Double.parseDouble(nf.format(diff * -1 * multiplier)));
		}
		if (smallDays > slopePerDays - 1 || wipes > 0) {
			ArrayList<Double> lastDays = new ArrayList<Double>();
			for (int i = prices.size() - slopePerDays; i < prices.size(); i++) {
				lastDays.add(prices.get(i));
			}
			nf.setMaximumFractionDigits(5);
			slope = nf.format(StockQuote.generateSlope(lastDays));
			if (wipes == 0) {
				gp.plotPoint(smallDays - slopePerDays, price);
				buy.plotPoint(smallDays - slopePerDays, Double.valueOf(slope));
			} else {
				gp.plotPoint(smallDays, price);
				buy.plotPoint(smallDays, Double.valueOf(slope));
			}
		}
		// avgTrend.add(Double.parseDouble(nf.format(diff * multiplier)));
		days++;
		smallDays++;
		lastPrice = price;

		if (smallDays > maxDaysInMonth) {
			wipes++;
			smallDays = 0;
			buy.wipe();
		}
		if (smallDays > maxDaysInYear) {
			wipes++;
			smallDays = 0;
			buy.wipe();
			gp.wipe();
		}
	}
}