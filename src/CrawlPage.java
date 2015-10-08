import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.io.IOUtils;

import crawlercommons.robots.*;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;
import edu.uci.ics.crawler4j.url.URLCanonicalizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

 class crawlerExecution implements Runnable{

	String [] seeds=new String[5];
	Queue links=new LinkedList();
	Thread t;
	HashMap<String,Integer> inlinks=new HashMap<String,Integer>();
	HashMap<String,String> outlinks=new HashMap<String,String>();
	HashMap<String,Integer> catalog=new HashMap<String,Integer>();
	HashMap<String,String> inlinkStrings=new HashMap<String,String>();
    ArrayList<String> visitedlinks=new ArrayList<String>();
	 MultiHashMap inlinksmap = new MultiHashMap();
	
	public crawlerExecution(String[] seeds)
	{
		this.seeds=seeds;
		for(int i=0;i<seeds.length;i++)
		{
			String tempurl;
			try {
				tempurl = canonicalize(new URL(seeds[i])).toString();
			
			links.add(tempurl);
			inlinks.put(tempurl,0);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void run() 
	{
	    synchronized(this)
		{
		try
		{
			int fileincrementer=1;
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("corpus/document"+fileincrementer+".txt")));
			PrintWriter inlinkwriter = new PrintWriter(new BufferedWriter(new FileWriter("corpus/inlinks.txt")));
			PrintWriter outlinkwriter= new PrintWriter(new BufferedWriter(new FileWriter("corpus/outlinks.txt")));

		Iterator ir=links.iterator();
		int count=0;
		int maincount=0;
		String prevurl=null;
		writer.println("<Doc>");
		
		while(ir.hasNext())
		{
		 long inittime=System.currentTimeMillis();
			//this.wait(1000);
			
			String url=ir.next().toString();     
			if(isValidURL(url) && (!visitedlinks.contains(url)))
			{  
				
				Jsoup.parse(url);
				
				Connection.Response response=Jsoup.connect(url).timeout(100000).ignoreContentType(true).ignoreHttpErrors(true).execute();
				if(response.statusCode()==200 && (response.contentType().equals("text/html; charset=utf-8") || response.contentType().equals("text/html; charset=UTF-8")))
				{
					//System.out.println(url +" inlinks count= "+ inlinks.get(url));
					//writer.println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------");
					//writer.println(url +" inlinks count= "+ inlinks.get(url));
					
					//writer.println("--------------------------------------------------------------------------------------------------------------------------------------------------------");
					Document htmldoc=Jsoup.connect(url).
						userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0").timeout(10000).get();
				//System.out.println(url);
				   writer.println("<Docno>"+url+"</Docno>");
				   writer.println("<RawText>"+htmldoc+"</RawText>");
				   writer.println("<CleanText>"+htmldoc.body().text()+"</CleanText>");
				   writer.println("<title>" +htmldoc.title()+"</title>");
					
				   Elements links=htmldoc.select("a[href]");
				   String tempurl=null;
				   String alllinks=url+"\t";
				   for(Element e : links)
				   {
					 String absurl=e.absUrl("href");
					 if(absurl.length()>0)
					 {
						if(canonicalize(new URL(absurl))!=null)
						{
							tempurl=canonicalize(new URL(absurl)).toString();
					    alllinks=alllinks.concat(tempurl+"\t");
					  if(inlinksmap.containsKey(tempurl))
					  {
					    if(!inlinksmap.containsValue(tempurl, url))
					    	
					   {
						  
						  int temp=Integer.parseInt(inlinks.get(tempurl).toString());
						  temp=temp+1;
						//  writer.println(tempurl +" "+temp);
						  inlinksmap.put(tempurl,url);
						   inlinks.put(tempurl, temp);
								  
					   }
					  }
					   else 
					   {
						   
						   inlinksmap.put(tempurl, url);
						    inlinks.put(tempurl, 1); 
					   }
					         
					  // System.out.println(tempurl);
				   }
					 }
				   }
				  outlinks.put(url, alllinks);
				  
				   count++;
				   maincount++;
			     visitedlinks.add(url);
					 
				}
				
				System.out.println(maincount);
			}
			
			ir.remove();
			//links.remove(url);
			if(links.size()==0)
			{
				//System.out.println("inside if  condition");
				ValueComparator bvc =  new ValueComparator(inlinks);
				TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
				sorted_map.putAll(inlinks);
				
				 for (Map.Entry<String,Integer> pair: sorted_map.entrySet()) {
					 String tempurl=pair.getKey().toString();
					// System.out.println(tempurl +" "+pair.getValue());
					 if(!visitedlinks.contains(tempurl))
							links.add(tempurl);
			           
			        }
			
				
				ir=links.iterator();
			}
			if(count==50)
			{
				writer.println("</Doc>");
				writer.close();
			
				fileincrementer++;
				 writer = new PrintWriter(new BufferedWriter(new FileWriter("corpus/document"+fileincrementer+".txt")));
					writer.println("<Doc>");
				count=0;
			}
			
			if(maincount>20000)
			{
				writer.close();
				break;
			}	
			
			long endtime=System.currentTimeMillis();
			if((inittime-endtime) <1000)
				this.wait(1000-(inittime-endtime));
		}
		
		
		 Iterator keys=inlinksmap.keySet().iterator();
		  
		 keys=outlinks.keySet().iterator();
		 System.out.println("outlinks are "+ outlinks.size());
		 while(keys.hasNext())
		 {
			 String key=keys.next().toString();
			 String val=outlinks.get(key);
		//	 System.out.println(val);
			 outlinkwriter.println(val);
			
		 }
		 outlinkwriter.close();
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}
	}
	
	

	
	private static String canonicalize(URL url)  {
		String newcurl=null;
		try
		{
		String curl;
	
		curl = URLCanonicalizer.getCanonicalURL(url.toString());
		if(curl != null) {
		URL newurl = new URL(curl);
		if(newurl != null) {
		 newcurl = newurl.getProtocol() + "://" + newurl.getHost() 
				+ newurl.getFile();
		
		}
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println("new url is"+newcurl);
		return newcurl;
	}
	
	
	public boolean isValidURL(String url)
	{
		boolean urlAllowed=false;
		try
		{
		String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";
		 HttpClient httpClient = HttpClients.createDefault();
		// System.out.println(url + "url is");
		URL urlObj = new URL(url);
		String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
		                + (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
		Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
	
		BaseRobotRules rules = robotsTxtRules.get(hostId);
		if (rules == null) {
		    HttpGet httpget = new HttpGet(hostId + "/robots.txt");
		    
		    HttpContext context = new BasicHttpContext();
		   
		    HttpResponse response = httpClient.execute(httpget);
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
		 urlAllowed = rules.isAllowed(url);
		return urlAllowed;
	}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return urlAllowed;
}
	
}

 public class CrawlPage
 {
	 public static void main(String[] args)
	 {
		 String[] links={"https://www.google.com/search?client=safari&rls=en&q=LATIN+MUSIC&ie=UTF-8&oe=UTF-8",
			 "http://www.ranker.com/list/latin-jazz-bands-and-musicians/reference",
					"http://en.wikipedia.org/wiki/Grammy_Award_for_Best_Latin_Jazz_Album",
					"https://en.wikipedia.org/wiki/Latin_jazz",
					"http://jazz.about.com/od/introductiontojazz/tp/Five_Latin_Jazz_Legends.htmz"};
		 crawlerExecution ce=new crawlerExecution(links);
		 try {
			 Thread t=new Thread(ce);
			 t.start();
	            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
 }

 class ValueComparator implements Comparator<String> {

		Map<String, Integer> base;
		public ValueComparator(Map<String, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.    
		public int compare(String a, String b) {
			if (base.get(a) > base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}