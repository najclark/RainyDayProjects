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
	int offset = 0;

	public Stock(String symbol, int offset) {
		this.offset = offset;
		this.symbol = symbol;
		f = System.getProperty("user.dir") + "/src/" + symbol + "Memory.txt";
		buy = new GraphPanel("Stock: " + symbol);
		buy.setTop(threshold);
		buy.setBottom(threshold);
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
		
		Calendar c = Calendar.getInstance();
		
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, offset);
		
		long wait = c.getTimeInMillis()-System.currentTimeMillis();
		System.out.println(wait);
		try {
			Thread.sleep(wait);
			System.out.println("Aligned Thread: " + (60-offset));
			while(true) {
				daily();
				
				c.set(Calendar.MINUTE, 59);
				c.set(Calendar.SECOND, offset);
				wait = c.getTimeInMillis()-System.currentTimeMillis();
				Thread.sleep(wait);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
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

			String username = "";
			String password = "";
			
			if(Double.parseDouble(slope) > threshold){
				SendEmail.sendEmail("Stock: " + symbol, "The current stock has gone above the set threshold(" + threshold + "). We are predicting " + symbol + " to drop soon.", username, password);
			}
			else if(Double.parseDouble(slope) < -threshold){
				SendEmail.sendEmail("Stock: " + symbol, "The current stock has gone below the set threshold(" + -threshold + "). We are predicting " + symbol + " to rise soon.", username, password);
			}
		}
	}

	public void setData(double price) {
		Calendar cal = Calendar.getInstance();
		int maxReadingsPerMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) * 7; // 7
																					// readings
																					// per
																					// day
		System.out.println("Stock: " + symbol + ". Price: $" + price + ". Time: " + cal.getTime().toString());
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