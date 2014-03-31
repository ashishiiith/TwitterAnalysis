package ciir.umass.edu.qproc;

import java.io.*;
import java.util.*;
import cmu.arktweetnlp.RunTagger;
import cmu.arktweetnlp.Tagger;

/**
 * Created by ashishjain on 3/25/14.
 */
public class POSTagger {
    static boolean checkNoun(String tag) {
        if (tag.startsWith("N") || tag.startsWith("^")) {
            return true;
        }
        else
            return false;
    }
    public static ArrayList<String> tag(String tweet) {

    	ArrayList<String> nounList = new ArrayList<String>();
        try {
		Tagger tag=new Tagger();
            	tag.loadModel("model.20120919");
       		List<Tagger.TaggedToken> t=tag.tokenizeAndTag(tweet);
        	for (Tagger.TaggedToken tags: t){
            	    System.out.println(tags.token+"\t"+tags.tag);
        	}
		ArrayList<String> tagList = new ArrayList<String>();
        	ArrayList<String> nounList = new ArrayList<String>();
        	boolean flag = false;
        	int index = -1;
		for (Tagger.TaggedToken tags: t) {

                    tagList.add(tags.tag);

                    if (checkNoun(tags.tag) && flag == false) {
                        nounList.add(tags.token);
                        flag = true;
                        index++;
                    }
                    else if (checkNoun(tags.tag) && flag == true) {
                        String noun = nounList.get(index);
                        noun = noun + " " + tags.token;
                        nounList.set(index, noun); //replacing with updated noun phrase in arraylist
                    }
                    else
                        flag = false;
        	}


    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

    return nounList;
    }
}
