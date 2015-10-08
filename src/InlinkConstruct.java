import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;


public class InlinkConstruct {
	static HashMap<String, String> inlinkmap = new HashMap<String,String>();
	static HashMap<String, ArrayList<String>> newinlinkmap = new HashMap<String,ArrayList<String>>();
	static HashMap<String, Integer> idmap = new HashMap<String,Integer>();
	static int idno = 0;
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		Files.walk(Paths.get("C:/Users/AKI/workspace/WebCrawler/CrawledDocs")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				String filePath1 = filePath.toString();


				String testHtml=null;
				// Writing All Text to a String.
				try {
					testHtml = String.join("\n", Files.readAllLines(Paths.get(filePath1) ,Charset.forName("ISO-8859-1")));

				} catch (Exception e) {
					e.printStackTrace();
				}


				// Breaking all DOCS into String Arrays
				String[] tds = StringUtils.substringsBetween(testHtml, "<DOC>", "</DOC>");
				for (String td : tds) {
					idno ++;
					String title = StringUtils.substringBetween(td, "<DOCNO>", "</DOCNO>");
					System.out.println(idno);
					idmap.put(title,0);
				}
			}
		});


		Files.walk(Paths.get("C:/Users/AKI/workspace/WebCrawler/CrawledDocs")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				String filePath1 = filePath.toString();


				String testHtml=null;
				// Writing All Text to a String.
				try {
					testHtml = String.join("\n", Files.readAllLines(Paths.get(filePath1) ,Charset.forName("ISO-8859-1")));

				} catch (Exception e) {
					e.printStackTrace();
				}


				// Breaking all DOCS into String Arrays
				String[] tds = StringUtils.substringsBetween(testHtml, "<DOC>", "</DOC>");
				for (String td : tds) {
					idno ++;
					String title = StringUtils.substringBetween(td, "<DOCNO>", "</DOCNO>");
					//idmap.put(title,0);
					String outlinks = StringUtils.substringBetween(td, "<OUTLINKS>", "</OUTLINKS>");
					//String combinedtext = StringUtils.join(texts);
					System.out.println(idno);
					if(outlinks != null) {
						String[] inlinks = outlinks.split("\n");
						
						Set<String> set = new HashSet<String>();
						Collections.addAll(set, inlinks);

						for(String i : set) {
							if(idmap.containsKey(i))
							{
							if(inlinkmap.containsKey(i))
							{
								String templist = inlinkmap.get(i);
								templist= templist + title + "\n";
								inlinkmap.put(i, templist);
							}
							else
							{
								String templist = title + "\n";
								inlinkmap.put(i, templist);
								
							}
							}
						}

					}
				}
			}
		});

		/*Iterator<String> itr = idmap.keySet().iterator();
		while(itr.hasNext()) {
			String id=  itr.next();
			if(inlinkmap.containsKey(id))
			{
				ArrayList<String> ire = inlinkmap.get(id);
				newinlinkmap.put(id, ire);
			}
		}*/

		File log = new File("C:\\Users\\AKI\\workspace\\WebCrawler\\inlinks.txt");
		FileWriter fileWriter = new FileWriter(log, true);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		String str = null;
		Iterator<String> initr = inlinkmap.keySet().iterator();
			while(initr.hasNext()) {
			String title = initr.next();
			String list = inlinkmap.get(title);
			str = "<DOCNO>" + title + "</DOCNO>" + "<INLINKS>" + list + "</INLINKS>";
			str = "<DOC>" + str + "</DOC>";
			bufferedWriter.write(str);
			if(inlinkmap.get(title) == null) {
				System.out.println("Error Foudn");
			}

		}
			
		System.out.println(inlinkmap.size());
		bufferedWriter.close();
		System.out.println("Done");
	}
}