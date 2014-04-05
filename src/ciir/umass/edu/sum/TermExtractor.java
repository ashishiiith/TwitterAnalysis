package ciir.umass.edu.sum;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ciir.umass.edu.qproc.KStemmer;
import ciir.umass.edu.qproc.NPExtractor;
import ciir.umass.edu.retrieval.utils.IndriSearchEngine;
import ciir.umass.edu.retrieval.utils.QueryProcessor;
import ciir.umass.edu.retrieval.utils.StringUtils;
import ciir.umass.edu.utilities.Sorter;

public class TermExtractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/*String queryFile = DataSource.stemmedQueryFile;
		int nTopicTerms = 500;
		String topicTermFile = "";
		String hDirectory = "";
		int topD = 50;
		int method = 0;//Lawrie & Croft, 2003.
		
		for(int i=0;i<args.length;i++)
		{
			if(args[i].compareTo("-query") == 0)
				queryFile = args[++i];
			else if(args[i].compareTo("-d") == 0)
				topD = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-t") == 0)
				nTopicTerms = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-m") == 0)
				method = Integer.parseInt(args[++i]);
			else if(args[i].compareTo("-dir") == 0)
				hDirectory = FileUtils.makePathStandard(args[++i]);
			else if(args[i].compareTo("-save") == 0)
				topicTermFile = args[++i];
			else if(args[i].compareTo("-qt") == 0)
				includeQueryTerm = true;
			else if(args[i].compareTo("-col") == 0)
				index = args[++i];
		}		*/
		
		String index = "/mnt/nfs/work2/ashishjain/adobe/training_index";
		try {
			DSPApprox e = new DSPApprox();	
			TermExtractor te = new TermExtractor(e, new IndriSearchEngine(index));
			//te.run(queryFile, nTopicTerms, hDirectory, topicTermFile);			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int topD = 1000;
			int topT = 20;
			Hierarchy.usePhrases = true;
			Hierarchy.usePhrasesOnly = true;
			do {
				System.out.print("query: ");
				String text = br.readLine();
				if(text.compareToIgnoreCase("exit") == 0)
					break;
				
				if(text.indexOf("=") != -1)
				{
					String[] s = text.split("=");
					if(s[0].compareToIgnoreCase("d") == 0)
						topD = Integer.parseInt(s[1]);
					else if(s[0].compareToIgnoreCase("t") == 0)
						topD = Integer.parseInt(s[1]);
					else if(s[0].compareToIgnoreCase("p") == 0)
					{
						Hierarchy.usePhrases = true;
						Hierarchy.usePhrasesOnly = false;
					}
					else if(s[0].compareToIgnoreCase("po") == 0)
					{
						Hierarchy.usePhrases = true;
						Hierarchy.usePhrasesOnly = true;
					}
					else if(s[0].compareToIgnoreCase("u") == 0)
						Hierarchy.usePhrases = false;
					else if(s[0].compareToIgnoreCase("th") == 0)
						NPExtractor.threshold = Double.parseDouble(s[1]);
					else if(s[0].compareToIgnoreCase("pr") == 0)
						Hierarchy.printRetDoc = true;
					else if(s[0].compareToIgnoreCase("pp") == 0)
						Hierarchy.printPhrase = true;
				}
				else
				{				
					List<TopicTerm> terms = te.extract(text, topD, topT);
					for(int i=0;i<terms.size();i++)
						System.out.println((i+1) + "\t" + terms.get(i).term);
				}
			}while(true);
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
	
	public static boolean includeQueryTerm = false;
	
	protected KStemmer stemmer = new KStemmer();
	protected DSPApprox extractor = null;
	protected IndriSearchEngine se = null;
	
	public TermExtractor(DSPApprox e, IndriSearchEngine se)
	{
		this.extractor = e;
		this.se = se;
	}
	public List<TopicTerm> extract(String query, int topD, int topTerm) throws Exception
	{
		List<TopicTerm> terms = null;
		try{
			Hierarchy h = new Hierarchy(se);
			h.estimate(query, topD);
			
			String q = stemmer.stem(query);			
			terms = extractor.generateTopicTerms(q, h, topTerm);			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return terms;
	}
	
	//not in use
	public void run(String queryFile, int topTerm, String hDir, String outputFile) throws Exception
	{
		BufferedWriter out = null;
		try{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
			List<String> qids = new ArrayList<String>();
			List<String> qtext = new ArrayList<String>();
			
			//read queries
			QueryProcessor.readIndriQueryFile(queryFile, qids, qtext);
			List<Integer> numTopics = new ArrayList<Integer>();
			int sum = 0;
			int nq = 0;
			for(int i=0;i<qids.size();i++)
			{
				String hFile = hDir + "q-" + qids.get(i);
				File f = new File(hFile);
				if(!f.exists())
					continue;
				
				//load hierarchy statistics
				System.out.println("model: " + hFile);
				Hierarchy h = new Hierarchy();
				h.load(hFile);
				
				String q = qtext.get(i);
				String[] qt = q.split("\\s+");
				HashSet<String> qts = new HashSet<String>();
				for(int k=qt.length-1;k>=0;k--)
					qts.add(qt[k]);
				
				List<TopicTerm> output = extractor.generateTopicTerms(q, h, topTerm);
				for(int j=0;j<output.size();j++)
				{
					String tt = output.get(j).term;
					if(includeQueryTerm)
					{
						String[] ss = tt.split("\\s+");
						boolean flag = true;
						for(int k=0;k<ss.length&&flag;k++)
							if(!qts.contains(ss[k]))
								flag = false;
						
						if(flag)//yes, the query subsums the topic ==> discard this topic
						{
							output.remove(j);
							j--;
						}
						else
						{
							HashSet<String> hs = new HashSet<String>();
							for(int k=0;k<ss.length;k++)
								hs.add(ss[k]);
							for(int k=qt.length-1;k>=0;k--)
							{
								if(!hs.contains(qt[k]))
									tt = qt[k] + " " + tt;
							}
							output.get(j).term = StringUtils.quote(tt);
						}
					}
					else
						output.get(j).term = StringUtils.quote(tt);
				}
				
				//save reformulation file
				out.write(qids.get(i) + "\t" + "0" + "\t" + StringUtils.quote(q));
				out.newLine();
				for(int j=0;j<output.size();j++)
				{
					out.write(qids.get(i) + "\t" + output.get(j).weight + "\t" + output.get(j).term);
					out.newLine();
				}
				
				numTopics.add(output.size());
				sum += output.size();
				if(output.size() > 0)
					nq++;
			}
			//print stats
			int[] idx = Sorter.sort(numTopics, true);
			double min = numTopics.get(idx[0]);
			double max = numTopics.get(idx[idx.length-1]);
			double median = numTopics.get(idx[idx.length/2]);
			double mean = ((double)sum)/numTopics.size();
			out.write("# min: " + min);
			out.newLine();
			out.write("# max: " + max);
			out.newLine();
			out.write("# mean: " + mean);
			out.newLine();
			out.write("# median: " + median);
			out.newLine();
			out.write("# nq=" + nq);
			out.newLine();
			int[] stats = new int[numTopics.get(idx[idx.length-1])+1];
			for(int i=0;i<stats.length;i++)
				stats[i] = 0;
			for(int i=0;i<numTopics.size();i++)
				stats[numTopics.get(i)]++;
			for(int i=0;i<stats.length;i++)
			{
				out.write("# " + i + "\t" + stats[i]);
				out.newLine();
			}

			out.close();
		}
		catch(Exception ex)
		{
			System.out.println(ex.toString());
		}
	}
}
