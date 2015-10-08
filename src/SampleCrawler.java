import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;
import edu.uci.ics.crawler4j.url.WebURL;


public class SampleCrawler {
	static HashMap<String,Integer> visitedpages = new HashMap<String,Integer>();
	static String user_agent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0 Safari";
	static HashMap<String,String> catalogmap = new HashMap<String,String>();
	static HashMap<String, ArrayList> inlinkmap = new HashMap<String,ArrayList>();
	static int doccount = 0;
	static String text = "" ;
	static long time = 0 ;
	static long finaltime = 0;
	public static void main(String[] args) throws IOException, Exception {

		/*Pattern filters = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g"
				+ "|png|tiff?|mid|mp2|mp3|mp4" + "|wav|avi|mov|mpeg|ram|m4v|pdf"
				+ "|rm|smil|wmv|swf|wma|zip|rar|gz))$");*/

		SortValueMap tovisitmap = new SortValueMap();

		//tovisitmap.put("https://www.google.com/search?client=safari&rls=en&q=LATIN+MUSIC&ie=UTF-8&oe=UTF-8", 0);
		tovisitmap.put("https://en.wikipedia.org/wiki/Music_of_Latin_America",0);
		tovisitmap.put("http://www.latingrammy.com/en", 0);
		tovisitmap.put("http://en.wikipedia.org/wiki/Category:Latin_Grammy_Award_winners", 0);
		
		
			/*tovisitmap.put("http://en.wikipedia.org/wiki/Ballroom_dance",0);
			tovisitmap.put("http://en.wikipedia.org/wiki/List_of_DanceSport_dances", 0);
			tovisitmap.put("http://www.justdanceballroom.com/styles.html", 0);*/


		//System.out.println(tovisitmap);
		crawlpages(tovisitmap);
	}


	private static void crawlpages(SortValueMap tovisitmap) throws Exception {
		
		
		SortValueMap childmap = new SortValueMap();
		for(Entry k : tovisitmap.entrySet()) {
			//System.out.println(k.toString());

			String key = (String) k.getKey();
			Integer value = (Integer) k.getValue();
			
			time = System.currentTimeMillis();
			//while(itr.hasNext()) {
			String curl = key;//itr.next().toString();

			try {
				
				
				if(finaltime - time < 1000) {
				//thread to sleep for the specified number of milliseconds
				Thread.sleep((long) (1000 - (finaltime - time)));
				}
			} catch ( java.lang.InterruptedException ie) {
				System.out.println(ie);
			}

			//System.out.println(curl);




			if(isCrawlable(curl, user_agent) && !visitedpages.containsKey(curl)) {

				Connection.Response res;
				try {
					res = Jsoup.connect(curl).timeout(10*1000).execute();
				}catch(Exception e) {
					System.out.println(e);
					continue;
				}

				String contentType=res.contentType();

				if(res.statusCode() == 200) {
					//System.out.println(curl);
					//System.out.println(contentType);

					if(contentType.equals("text/html; charset=utf-8") || contentType.equals("text/html; charset=UTF-8")) {
						System.out.println(doccount + " = " + curl);
						//System.out.println(surl + " is crawlable");	
						String urlto = "<DOCNO>"+curl+"</DOCNO>" + "\n";

						//RawHtml
						Document doc ;
						try {
							doc = Jsoup.connect(curl.toString()).get();
						}catch(Exception e) {
							System.out.println(e);
							continue;
						}
						//System.out.println(doc);
						//System.exit(0);;
						String docto = "<RAW>"+doc+"</RAW>" + "\n";

						// Text
						String textof = doc.text();
						//System.out.println(textof);
						String textto = "<CLEAN>"+ textof +"</CLEAN>" + "\n";

						//Title
						String title = doc.title();
						//System.out.println(title);
						String titleto = "<HEAD>"+title+"</HEAD>" + "\n";


						//Get Header Fields
						HttpURLConnection con =
								(HttpURLConnection) new URL(curl).openConnection();
						Map<String, List<String>> headers = con.getHeaderFields();
						//System.out.println(headers);
						String headersto = "<HEADERS>"+headers+"</HEADERS>" + "\n";

						String outlinks = "";
						//Links
						Elements a = doc.select("a[href]");
						for(Element linko : a) {
							String link = linko.attr("abs:href");

							link = canonicalize(link);
							//System.out.println(link);
							//System.out.println(linko.attr("abs:href"));
							outlinks = outlinks + link + "\n";

							/*if(inlinkmap.containsKey(link)) { 
								ArrayList<String> temp = inlinkmap.get(link);
								temp.add(curl); 
								inlinkmap.put(link, temp);
							}
							else {
								ArrayList<String> list = new ArrayList<String>();
								list.add(curl);
								inlinkmap.put(link, list);
							}*/

							if(tovisitmap.containsKey(link)) {
								tovisitmap.put(link, tovisitmap.get(link)+1);
							}
							else {
								tovisitmap.put(link, 0);
							}
						}

						String outlinksto = "<OUTLINKS>"+outlinks+"</OUTLINKS>" + "\n";

						String str = urlto + docto + textto + titleto + headersto + outlinksto;
						str = "<DOC>" + str + "</DOC>" ;
						text = text + str ;

						doccount++;
					}

				}
			}
			visitedpages.put(curl,0);
			
			//System.out.println("Visited = " + curl);
			if(doccount % 50 == 0)
			{
				int id = doccount / 50 ;
				File log = new File("C:\\Users\\AKI\\workspace\\WebCrawler\\" + id + ".txt");
				FileWriter fileWriter = new FileWriter(log, true);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(text);
				bufferedWriter.close();
				text = "";
			}
		}



		//tovisitmap = new SortValueMap();

		//tovisitmap.putAll(childmap);

		if(visitedpages.size() >= 20000)
			System.exit(0);
		
		finaltime = System.currentTimeMillis();
		crawlpages(tovisitmap);



	}



	private static String canonicalize(String link) throws Exception {
		String newcurl = link ;
		String curl = URLCanonicalizer.getCanonicalURL(link);
		if(curl != null) {
			URL newurl = new URL(curl);

			if(newurl != null) {
				//System.out.println(newurl);

				newcurl = newurl.getProtocol() + "://" + newurl.getHost() 
						+ newurl.getFile();
			}
		}
		return newcurl;
	}



	//Stack Overflow Reference
	public static boolean isCrawlable(String page_url, String user_agent) throws IOException {
		String USER_AGENT = user_agent;
		String url = page_url;
		HttpClient httpclient = HttpClients.createDefault();
		boolean urlAllowed = false;
		URL urlObj = new URL(url);
		if(urlObj != null) {
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				HttpGet httpget = new HttpGet(hostId + "/robots.txt");
				HttpContext context = new BasicHttpContext();
				HttpResponse response = null;
				try {
					response = httpclient.execute(httpget, context);
				} catch(Exception e) {
					System.out.println(e);
				}
				if(response!= null)
				{
					if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 404) {
						rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
						// consume entity to deallocate connection
						EntityUtils.consumeQuietly(response.getEntity());
					} else {
						BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
						SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
						rules = robotParser.parseContent(hostId, IOUtils.toByteArray(entity.getContent()),
								"text/plain", USER_AGENT);
					}
					robotsTxtRules.put(hostId, rules);
				}
			}

			try {
				urlAllowed = rules.isAllowed(url);
			} catch(Exception e) {
				System.out.println(e);
			}
		}
		else
		{
			urlAllowed = false;
		}
		return urlAllowed;
		//return true;
	}

}


//Stackoverflow Reference
class SortValueMap extends HashMap<String,Integer>{

	@Override
	public Set<Entry<String,Integer>> entrySet() {
		List<Entry<String,Integer>> entries = new ArrayList<Entry<String,Integer>>(super.entrySet());
		Collections.sort(entries, new Comparator<Entry<String,Integer>>(){

			@Override
			public int compare(Map.Entry<String,Integer> o1, Map.Entry<String,Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}});
		return new LinkedHashSet<Entry<String,Integer>>(entries);
	}
}



