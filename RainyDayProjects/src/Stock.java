import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class Stock {

	String f = System.getProperty("user.dir") + "/src/memory.txt";
	String symbol = "goog";

	/*
	 * How it works trends
	 * 
	 * 
	 */
	ArrayList<Double> upTrend = new ArrayList<Double>(); // change if UP
	ArrayList<Double> downTrend = new ArrayList<Double>();// change if DOWN
	ArrayList<Double> avgTrend = new ArrayList<Double>(); // change if BOTH
	ArrayList<Double> avgHigh = new ArrayList<Double>(); // change if UP
	ArrayList<Double> avgLow = new ArrayList<Double>(); // change if DOWN
//	ArrayList<Double> upStreak = new ArrayList<Double>(); // change if UP
//	ArrayList<Double> downStreak = new ArrayList<Double>(); // change if DOWN
	static GraphPanel gp;

	double multiplier = 1;
	double lastPrice = 0;
	double avg = 0;
	double upStreak, downStreak;
	int streak, days = 0;

	public static void main(String[] args) {
		//StockQuote.generateStock(1000, 10);
		gp = new GraphPanel();
		gp.plotPoint(10);
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
			System.out.println("Days: " + days);
			gp.plotPoint(price);
			new Scanner(System.in).next();
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
		//System.out.println("upStreak: " + StockQuote.avg(upStreak));
		//System.out.println("downStreak: " + StockQuote.avg(downStreak));
		System.out.println("streak: " + streak);
		System.out.println("Days: " + days);
	}

	public void setData(double price, double lastPrice) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		double diff = price - lastPrice;
		if (diff > 0) {
			if (streak < 0) {
				downStreak = (downStreak + (streak*-1)) / 2;
				//downStreak.add((double) (streak * -1));
				avgLow.add(price);
				streak = 0;
			}
			streak++;
			avgTrend.add((double) 1);
			avg++;
			upTrend.add(Double.parseDouble(nf.format(diff * multiplier)));
		} else if (diff < 0) {
			if (streak > 0) {
				upStreak = (upStreak + streak)/2;
				//upStreak.add((double) streak);
				avgHigh.add(price);
				streak = 0;
			}
			streak--;
			avgTrend.add((double) -1);
			avg--;
			downTrend.add(Double.parseDouble(nf.format(diff * -1 * multiplier)));
		}
		//avgTrend.add(Double.parseDouble(nf.format(diff * multiplier)));
		days++;
		lastPrice = price;
	}
}