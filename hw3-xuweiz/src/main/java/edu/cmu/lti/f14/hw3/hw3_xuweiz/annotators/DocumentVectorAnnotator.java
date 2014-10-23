package edu.cmu.lti.f14.hw3.hw3_xuweiz.annotators;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.f14.hw3.hw3_xuweiz.VectorSpaceRetrieval;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.utils.StanfordLemmatizer;

/**
 * Description: Process documents into tokens
 * 
 * @author Xuwei Zou
 *
 */
public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {
  static private Map<String,Integer> stopwordsmap ;
  
//  @Override
//  public void initialize(UimaContext aContext) throws ResourceInitializationException {
//    stopwordsmap = new HashMap<String,Integer>();
//    String sLine;
//    URL docUrl = VectorSpaceRetrieval.class.getResource("/stopwords.txt");
//    if (docUrl == null) {
//       throw new IllegalArgumentException("Error opening /stopwords.txt");
//    }
//    BufferedReader br = null;
//    try {
//      br = new BufferedReader(new InputStreamReader(docUrl.openStream()));
//      while ((sLine = br.readLine()) != null)   {
//        stopwordsmap.put(sLine, 1);
//      }
//    } catch (IOException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//   finally{
//      try {
//        br.close();
//      } catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
//   }
//   
//    br=null;
//      
//  }
  
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
	  
		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			createTermFreqVector(jcas, doc);
		}

	}

	/**
   * A basic white-space tokenizer.
   *
	 * @param doc input text
	 * @return    a list of tokens.
	 */

	List<String> tokenize0(String doc) {
	  List<String> res = new ArrayList<String>();
//	  doc = doc.replace(",", " ");
//	  doc = doc.replace(".", " ");
//	  doc = doc.replace("!", " ");
//	  doc = doc.replace(";", " ");
//	  doc = doc.replace("?", " ");
//	  doc = doc.replace("-", " ");
//	  doc = doc.replace("'s", "");
//    
//	  doc=doc.toLowerCase();
//	  doc = StanfordLemmatizer.stemText(doc);
 //  System.out.println(doc);

	  for (String s: doc.split("\\s+"))
	//    if(!stopwordsmap.containsKey(s))
	      res.add(s);
	  return res;
	}



	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		Map<String,Integer> map = new HashMap<String,Integer>();
		List<String> slist = new ArrayList<String>();
		slist = tokenize0(docText);
		for(int i =0; i<slist.size();i++){
		  String s = slist.get(i);
		  if(map.containsKey(s)){
		    int value = map.get(s);
		    map.put(s, value+1);
		  }
		  else{
		    map.put(s, 1);
		  }
		  

		}
		
    Collection<Token> colist = new ArrayList<Token>();
    Set<String> aset = map.keySet();
    Iterator<String> it =aset.iterator();
    while(it.hasNext()){
      String key = it.next();
      Token t = new Token(jcas);
      t.setText(key);
      t.setFrequency(map.get(key));
      colist.add(t);
    }
    FSList list = createFSList(jcas,colist);
    doc.setTokenList(list);
		//TO DO: construct a vector of tokens and update the tokenList in CAS
    //TO DO: use tokenize0 from above 
		

	}
	private static FSList createFSList(JCas aJCas, Collection<Token> aCollection)
	   {
	     if (aCollection.size() == 0) {
	       return new EmptyFSList(aJCas);
	     }
	
	     NonEmptyFSList head = new NonEmptyFSList(aJCas);
	     NonEmptyFSList list = head;
	     Iterator<Token> i = aCollection.iterator();
	     while (i.hasNext()) {
	       head.setHead(i.next());
	       if (i.hasNext()) {
	         head.setTail(new NonEmptyFSList(aJCas));
	         head = (NonEmptyFSList) head.getTail();
	       }
	       else {
	         head.setTail(new NonEmptyFSList(aJCas));
	       }
	     }
	
	     return list;
	   }
}
