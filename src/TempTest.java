import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.collect.TreeMultimap;


public class TempTest {

	public static void main(String[] args) {

		TreeMap<String,Double> map = new TreeMap<String,Double>();
		ValueComparator bvc =  new ValueComparator(map);
		TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);

		map.put("A",99.5);
		map.put("B",67.4);
		map.put("C",67.4);
		map.put("G",67.4);
		map.put("D",67.3);
		map.put("E",67.3);
		map.put("F",69.0);
		//map.put("D",(double) 69);
		
		SortValueMap  newmap = new SortValueMap();
		newmap.put("A",100);
		newmap.put("B",68);
		newmap.put("C",68);
		newmap.put("G",68);
		newmap.put("D",67);
		newmap.put("E",67);
		newmap.put("F",69);
		System.out.println("Map: "+newmap);
		System.out.println(newmap.get("C"));
		System.out.println("unsorted map: "+map);
		
		

	sorted_map.putAll(map);
		//sorted_map.containsKey("A");
		//System.out.println(sorted_map.get("A"));
		
		

		System.out.println("results: "+sorted_map);
	}
}

class ValueComparator1 implements Comparator<String> {

	Map<String, Double> base;
	public ValueComparator1(Map<String, Double> base) {
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