import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import crawlercommons.robots.SimpleRobotRulesParser;

public class CrawlerCommonTest {
	public static void main(String args[]) {
		boolean isSearchCrawlable = isCrawlable(
				"https://www.foodblogs.com/",
				"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
		boolean isNewsalertsCrawlable = isCrawlable(
				"https://www.google.com/search?client=safari&rls=en&q=LATIN+MUSIC&ie=UTF-8&oe=UTF-8", "user_agent");
		System.out.println("foodblogs CRAWLBALE " + isSearchCrawlable);
		System.out.println("NEWSALERT CRAWLBALE " + isNewsalertsCrawlable);
		// IN MILLI SECONDS
		long crawlDelay = getCrawlDelay("http://www.seobook.com", "Googlebot");
		System.out.println("CRAWL DELAY " + crawlDelay);
		//System.out.println(getContents("http://www.seobook.com"));
	}

	public static long getCrawlDelay(String page_url, String user_agent) {
		try {
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			System.out.println(hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							IOUtils.toByteArray(robotsContent), "text/plain",
							user_agent);
				}
			}
			return rules.getCrawlDelay();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}

	
	@SuppressWarnings("deprecation")
	public static boolean isCrawlable(String page_url, String user_agent) {
		try {
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			System.out.println(hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							IOUtils.toByteArray(robotsContent), "text/plain",
							user_agent);
				}
			}
			return rules.isAllowed(page_url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getContents(String page_url) {
		InputStream is = null;
		try {
			URLConnection openConnection = new URL(page_url).openConnection();
			openConnection
					.addRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
			is = openConnection.getInputStream();
			String theString = IOUtils.toString(is);
			return theString;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getContents1(String page_url) {
		try {
			URL oracle = new URL(page_url);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					oracle.openStream()));
			String content = new String();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				content += inputLine + "\n";
			}
			in.close();
			return content;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
