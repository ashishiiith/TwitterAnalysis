package ciir.umass.edu.sum.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lemurproject.indri.DocumentVector;
import ciir.umass.edu.retrieval.dts.LanguageModel;
import ciir.umass.edu.sum.simmeasure.CosineSimScorer;
import ciir.umass.edu.sum.simmeasure.SimScorer;
import ciir.umass.edu.utilities.AgMax;
import ciir.umass.edu.utilities.Aggregator;
import ciir.umass.edu.utilities.Sorter;

public class DiversityFeature {
	
	protected int topT = -1;//use all terms to form the word vector
	protected double maxSimAllowed = 0.9;	
	protected double minRelAllowed = 0.15;
	protected double lambda = 0.5;
	
	protected static double TINY = -1000000000;
	protected Aggregator ag = new AgMax();
	protected SimScorer scorer = new CosineSimScorer();
	protected double[][] cache = null;
	
	public double getValue(DocumentVector[] dvs, double[] scores)
	{
		LanguageModel[] lms = new LanguageModel[dvs.length];
		cache = new double[dvs.length][];
		for(int i=0;i<dvs.length;i++)
		{
			lms[i] = buildDocumentLM(dvs[i], topT);
			cache[i] = new double[dvs.length];
			for(int j=0;j<dvs.length;j++)
				cache[i][j] = TINY;
		}
		
		List<Integer> R = new ArrayList<Integer>();//the initial ranking
		List<Integer> S = new ArrayList<Integer>();//the diverse ranking
		
		if(dvs.length == 0 || scores[0] < minRelAllowed)
			return 0;
		
		S.add(0);
		for(int i=1;i<dvs.length && scores[i] >= minRelAllowed;i++)
			R.add(i);		
		
		//start the greedy procedure
		double sim = 0.0;
		double relevance = 0.0;
		//System.out.println("START...");
		while(R.size() > 0)
		{
			double maxDiverse = -1.0;
			double maxSim = -1.0;
			int which = -1;			
			for(int i=0;i<R.size();i++)
			{
				relevance = scores[R.get(i)];
				sim = similarity(R.get(i), S, lms);
				double score = lambda * relevance - (1.0 - lambda) * sim;
				if(maxDiverse < score)
				{
					maxDiverse = score;
					which = i;
					if(sim > maxSim)
						maxSim = sim;
				}
			}
			if(which == -1 || maxSim > maxSimAllowed)
				break;
			//found the best document
			S.add(R.get(which));
			R.remove(which);
		}
		return S.size();
	}
	
	protected double similarity(int idx, List<Integer> S, LanguageModel[] lms)
	{
		double max = -1;
		for(int j=0;j<S.size();j++)
		{
			double sim = 0;
			if(cache[idx][S.get(j)] > TINY+1)
				sim = cache[idx][S.get(j)];
			else
			{
				sim = scorer.score(lms[idx], lms[S.get(j)]);
				cache[idx][S.get(j)] = sim;
			}
			if(sim > max)
				max = sim;
		}
		return max;
	}
	protected LanguageModel buildDocumentLM(DocumentVector dv, int topTerms)
	{
		HashMap<String, Long> ngramFreq = new HashMap<String, Long>();
		for(int i=0;i<dv.positions.length;i++)
		{
			String stem = dv.stems[dv.positions[i]];
			if(ngramFreq.get(stem) == null)
				ngramFreq.put(stem, 1L);
			else
				ngramFreq.put(stem, ngramFreq.get(stem).longValue() + 1);
		}
		
		if(topTerms != -1)
		{
			List<String> terms = new ArrayList<String>();
			List<Long> freqs = new ArrayList<Long>();
			for(String key : ngramFreq.keySet())
			{
				terms.add(key);
				freqs.add(ngramFreq.get(key).longValue());
			}
			int[] idx = Sorter.sortLong(freqs, false);
			int size = (idx.length>topTerms)?topTerms:idx.length;
			//re-init
			ngramFreq = new HashMap<String, Long>();
			for(int i=0;i<size;i++)
				ngramFreq.put(terms.get(idx[i]), freqs.get(idx[i]));
		}
		
		LanguageModel lm = new LanguageModel();
		lm.set(ngramFreq);
		return lm;
	}	
}
