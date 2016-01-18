import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

public class StockQuote {

	// Given symbol, get HTML
	private static String readHTML(String symbol) {
		URL url;
		InputStream is = null;
		BufferedReader br;
		String line, html = "";
		try {
			url = new URL("http://finance.yahoo.com/q?s=" + symbol);
			is = url.openStream(); // throws an IOException
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				html += line;
			}
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
			}
		}
		return html;
	}

	// Given symbol, get current stock price.
	public static double priceOf(String symbol) {
		String html = readHTML(symbol);
		int p = html.indexOf("yfs_l84", 0); // "yfs_l84" index
		int from = html.indexOf(">", p); // ">" index
		int to = html.indexOf("</span>", from); // "</span>" index
		String price = html.substring(from + 1, to);
		return Double.parseDouble(price.replaceAll(",", ""));
	}

	// Given symbol, get current stock name.
	public static String nameOf(String symbol) {
		String html = readHTML(symbol);
		int p = html.indexOf("<title>", 0);
		int from = html.indexOf("Summary for ", p);
		int to = html.indexOf("- Yahoo! Finance", from);
		String name = html.substring(from + 12, to);
		return name;
	}

	// Given symbol, get current date.
	public static String dateOf(String symbol) {
		String html = readHTML(symbol);
		int p = html.indexOf("<span id=\"yfs_market_time\">", 0);
		int from = html.indexOf(">", p);
		int to = html.indexOf("-", from); // no closing small tag
		String date = html.substring(from + 1, to);
		return date;
	}

	public static String readFile(String path) {
		try {
			BufferedReader txt;

			txt = new BufferedReader(new FileReader(path));
			String line, total = "";

			while ((line = txt.readLine()) != null) {
				total += line + "\n";
			}
			txt.close();

			return total;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String writeFile(String txt, String path) {
		try {
			String total = readFile(path);
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));

			total += txt + "\n";
			bw.write(total);
			bw.close();

			return total;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void wipeFile(String path){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static double avg(ArrayList<Double> list) {
		double total = 0;
		for (double d : list) {
			total += d;
		}
		total /= list.size();
		return total;
	}

	public static void generateStock(int days, double startPrice) {
		wipeFile(System.getProperty("user.dir") + "/src/memory.txt");
		double volatility = 0.007;
		double old_price = startPrice;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		for (int i = 0; i < days; i++) {
			double rnd = Math.random(); // generate number, 0 <= x < 1.0
			double change_percent = 2 * volatility * rnd;
			if (change_percent > volatility)
				change_percent -= (2 * volatility);
			double change_amount = old_price * change_percent;
			double new_price = old_price + change_amount;
			
			writeFile(nf.format(new_price).replace(",", ""), System.getProperty("user.dir") + "/src/memory.txt");
			old_price = new_price;
		}
	}
	
	public static double generateSlope(ArrayList<Double> prices){
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		ArrayList<Double> xy = new ArrayList<Double>();
		ArrayList<Double> x2 = new ArrayList<Double>();
		for(int i = 0; i < prices.size(); i++){
			x.add((double)i);
			y.add(prices.get(i));
			xy.add(i*prices.get(i));
			x2.add((double)(i*i));
		}
		double meanX = StockQuote.avg(x);
		double meanY = StockQuote.avg(y);
		double meanXY = StockQuote.avg(xy);
		double meanX2 = StockQuote.avg(x2);
		
		return (meanX * meanY - meanXY)/(meanX * meanX - meanX2);
	}
	
	public static Date toUTC(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
		Date utcCur = null;
		d.setMinutes(d.getMinutes() + d.getTimezoneOffset());
		return utcCur;
	}
}