import java.net.*;
import java.util.List;
import java.util.Map;
import java.io.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;






public class SpiderTest
{
    public static void main(String[] args) throws Exception
    {
    	/*String sUrl = "http://test.com:8080/test and test/a?query=world";
    	//String sUrl = "http://www.google.com";
    	URL url = new URL(sUrl);
    	//String html = url.getContent().toString();
    	URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
    	String canonical = uri.toString();
    	System.out.println(canonical);
    	//System.out.println(html);
*/    	
    	
    	
    	HttpURLConnection.setFollowRedirects(false);
    	HttpURLConnection con =
    	         (HttpURLConnection) new URL("http://www.google.com").openConnection();
    	Map<String, List<String>> sr = con.getHeaderFields();
    	System.out.println(sr);
    	      con.setRequestMethod("HEAD");
    	      System.out.println(con.getContentType());
    	      //System.out.println(con.getContent());
    	      System.out.println(con.getResponseCode() == 200);
    	      //return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
    	      con.disconnect();
    	      
    	      
    	      
    	      
    	  URL url = new URL("http://en.wikipedia.org/wiki/Category:Latin_Grammy_Award_winners");
    	  Document doc = Jsoup.connect(url.toString()).get();
    	    //System.out.println(doc);
    	    Elements a = doc.select("a[href]");
			for(Element linko : a)
			{
				System.out.println(linko.attr("abs:href"));
			}
    	
    }
}


