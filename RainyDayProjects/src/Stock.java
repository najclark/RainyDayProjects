import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
		
		Calendar c = Calendar.getInstance();
		DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = dateFormatter.parse(c.get(Calendar.YEAR)+"-"+(c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+" "+
					c.get(Calendar.HOUR_OF_DAY)+":"+59+":"+offset);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		int period = 1000*60*60;
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				daily();
			}
			
		}, date, period);
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
			if(Double.parseDouble(slope) > threshold){
				SendEmail.sendEmail("Stock: " + symbol, "The current stock has gone above the set threshold(" + threshold + "). We are predicting " + symbol + " to drop soon.", username, password);
			}
			else if(Double.parseDouble(slope) < -threshold){
				SendEmail.sendEmail("Stock: " + symbol, "The current stock has gone below the set threshold(" + -threshold + "). We are predicting " + symbol + " to rise soon.", username, password);
			}
			
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