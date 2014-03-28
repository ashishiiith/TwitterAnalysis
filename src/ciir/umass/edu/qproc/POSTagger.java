package ciir.umass.edu.qproc;

import java.io.*;
import java.util.*;
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
	        File temp = File.createTempFile("temp-file-name", ".tmp");
	        if(!temp.exists())
	            temp.createNewFile();
	        BufferedWriter output = new BufferedWriter(new FileWriter(temp));
	        output.write(tweet);
	        output.close();
	
	        //String pythonScriptPath = "/Users/ashishjain/sem2/courses/IndependentStudy/ark-tweet-nlp-0.3.2/runTagger.sh";
	        String pythonScriptPath = "/mnt/nfs/work3/vdang/TwitterAnalysis/TwitterAnalysis/lib/runTagger.sh";
	        String[] cmd = new String[4];
	        cmd[0] = pythonScriptPath;
	        cmd[1] = "--output-format";
	        cmd[2] = "conll";
	        cmd[3] = temp.getAbsolutePath();
	        System.out.println(cmd[3]);
	        Process p = Runtime.getRuntime().exec(cmd);
	
	        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String line = null;
	        ArrayList<String> tagList = new ArrayList<String>();
	        boolean flag = false;
	        int index = -1;
	        //int startindex = -1;
	        while((line = in.readLine()) != null) {
	            //System.out.println(line);
	            String[] tokens = line.split("\t");
	            if(tokens.length > 1) {
	
	                tagList.add(tokens[1]);
	                //if(tokens[1] == 'N')
	                if (checkNoun(tokens[1]) && flag == false) {
	                    nounList.add(tokens[0]);
	                    flag = true;
	                    index++;
	                }
	                else if (checkNoun(tokens[1]) && flag == true) {
	                    String noun = nounList.get(index);
	                    noun = noun + " " + tokens[0];
	                    nounList.set(index, noun); //replacing with updated noun phrase in arraylist
	                }
	                else
	                    flag = false;
	            }
	            else
	                flag = false;
	        }
	        temp.deleteOnExit(); //deleting temporary file
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

    return nounList;
    }
}
