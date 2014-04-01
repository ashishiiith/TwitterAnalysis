package ciir.umass.edu.tweet;
import java.io.*;
import java.util.*;

import ciir.umass.edu.qproc.POSTagger;
/**
 *  * Created by ashishjain on 4/1/14.
 *   */
public class Phrase {
	public static void main(String[] args) {
    	try {
	POSTagger tagger = new POSTagger();
 	Map <String, Integer> hm = new HashMap<String, Integer>();
        BufferedReader br = new BufferedReader(new FileReader("samplefile"));
        String line;
	while ((line = br.readLine()) != null) {
            // process the line.
            List<String> nps = tagger.tag(line);
            Iterator itr =  nps.iterator();
            while(itr.hasNext()) {
            	hm.put(itr.next().toString(), 1);
	    //    System.out.println(itr.next());
    	   }
	}
	br.close();
	for (Map.Entry<String, Integer> entry : hm.entrySet()) {
            System.out.println("key=" + entry.getKey());
        }
     }
	catch(Exception e) {
        System.out.println("Error in HierarchyStatistics.save(): " + e.toString());
      }
    }
}
