import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;




public class Indexing {

	static HashMap<String, String> inlinkmap = new HashMap<String,String>();

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Node node = nodeBuilder().client(true).clusterName("phoenixwings").node();
		Client client = node.client();
		String filePath12 = "C:/Users/AKI/workspace/WebCrawler/inlinks.txt";
		String everything = "";
		BufferedReader br = new BufferedReader(new FileReader(filePath12));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
		} finally {
			br.close();
		}


		String[] docs = StringUtils.substringsBetween(everything, "<DOC>", "</DOC>");

		for (String td : docs) {
			String title = StringUtils.substringBetween(td, "<DOCNO>", "</DOCNO>");

			String inlinks = StringUtils.substringBetween(td , "<INLINKS>", "</INLINKS>");

			inlinkmap.put(title, inlinks);
		}

		System.out.println("Inlinkmp Done");


		// Walking thru Files in Folder
		Files.walk(Paths.get("C:\\Users\\AKI\\workspace\\WebCrawler\\CrawledDocs")).forEach(filePath -> {
			if (Files.isRegularFile(filePath)) {
				String filePath1 = filePath.toString();
				System.out.println(filePath1);

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
					//System.out.println(td);

					String title = StringUtils.substringBetween(td, "<DOCNO>", "</DOCNO>");
					GetResponse getResponse = client
							.prepareGet("crawlweb", "webdoc", title)
							.execute().actionGet();

					if(getResponse.getSource() == null)
					{
						//System.out.println("went in");
						String raw = StringUtils.substringBetween(td, "<RAW>", "</RAW>");
						String text = StringUtils.substringBetween(td, "<CLEAN>",  "</CLEAN>");
						String titleid = StringUtils.substringBetween(td, "<HEAD>", "</HEAD>");
						String outlinks  = StringUtils.substringBetween(td, "<OUTLINKS>", "</OUTLINKS>");
						String inlinks = "";
						if(inlinkmap.containsKey(title)) {
							inlinks = inlinkmap.get(title);
						}
						
						LinkedHashSet<String> line = new LinkedHashSet<String>(Arrays.asList(inlinks.split("\n")));
						String str = StringUtils.join(line, "\n");
						try {
							IndexResponse response = client.prepareIndex("crawlweb", "webdoc", title)
									.setSource(jsonBuilder()
											.startObject()
											.field("docno", title)
											.field("title", titleid)
											.field("text", text)
											.field("html_Source", raw)
											.field("in_links", str)
											.field("out_links", outlinks)
											.field("author", "akhil")
											.endObject()
											)
											.execute()
											.actionGet();
						} catch (Exception e) {
							e.printStackTrace();
						}
					
					}
					else
					{

						Map<String, Object> source = getResponse.getSource();
						//System.out.println(source.size());

						String oldtext = source.get("in_links").toString();

						//String[] texts = StringUtils.substringsBetween(td, "<TEXT>", "</TEXT>");
						String raw = StringUtils.substringBetween(td, "<RAW>", "</RAW>");
						String text = StringUtils.substringBetween(td, "<CLEAN>",  "</CLEAN>");
						String titleid = StringUtils.substringBetween(td, "<HEAD>", "</HEAD>");
						String outlinks  = StringUtils.substringBetween(td, "<OUTLINKS>", "</OUTLINKS>");
						String inlinks = "";
						if(inlinkmap.containsKey(title)) {
							inlinks = inlinkmap.get(title);
						}
						
						String newlinks = oldtext + inlinks ;
						//String[] links = text.split("\n");
						
						LinkedHashSet<String> line = new LinkedHashSet<String>(Arrays.asList(newlinks.split("\n")));
						String str = StringUtils.join(line, "\n");
						try {
							IndexResponse response = client.prepareIndex("crawlweb", "webdoc", title)
									.setSource(jsonBuilder()
											.startObject()
											.field("docno", title)
											.field("title", titleid)
											.field("text", text)
											.field("html_Source", raw)
											.field("in_links", str)
											.field("out_links", outlinks)
											.field("author", "akhil")
											.endObject()
											)
											.execute()
											.actionGet();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
	}
}

