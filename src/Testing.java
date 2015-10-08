import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;


public class Testing {
	public static void main(String[] args) {
		Node node = nodeBuilder().client(true).clusterName("phoenixcore").node();
		Client client = node.client();
		
		
		GetResponse getResponse = client
				.prepareGet("crawldocs", "webdoc", "https://en.wikipedia.org/wiki/Bajofondo")
				.execute().actionGet();
		
		if(getResponse.equals(null))
			System.out.println("NULL SHIT");
		else
		{
			
		Map<String, Object> source = getResponse.getSource();
		System.out.println(source.size());
		
		String oldtext = source.get("in_links").toString();
		
		String newtext= "";
		
		String text = oldtext + newtext ;
		//String[] links = text.split("\n");
		
		StringBuilder builder = new StringBuilder(); 
		for (String line: new LinkedHashSet<String>(Arrays.asList(text.split("\n"))) ) {
		    builder.append(line);
		}
		String result = builder.toString();
		
		System.out.println(text);
		}
	
			
		 
	}

}



