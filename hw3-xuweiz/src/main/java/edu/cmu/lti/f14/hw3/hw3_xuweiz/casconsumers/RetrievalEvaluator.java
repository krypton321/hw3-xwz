package edu.cmu.lti.f14.hw3.hw3_xuweiz.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import edu.cmu.lti.f14.hw3.hw3_xuweiz.Doctype;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {

  /** query id number **/
  // public ArrayList<Integer> qIdList;

  /** query and text relevant values **/
  // public ArrayList<Integer> relList;

  public static HashMap<Integer, List<Doctype>> docMap;

  public static List<Doctype> questionList;

  public static int qnum = 0;

  public static List<Integer> mrrList;

  public void initialize() throws ResourceInitializationException {

    // qIdList = new ArrayList<Integer>();

    // relList = new ArrayList<Integer>();

    docMap = new HashMap<Integer, List<Doctype>>();

    questionList = new ArrayList<Doctype>();

    mrrList = new ArrayList<Integer>();
  }

  /**
   * TODO :: 1. construct the global word dictionary 2. keep the word frequency for each sentence
   */
  @Override
  public void processCas(CAS aCas) throws ResourceProcessException {

    JCas jcas;
    try {
      jcas = aCas.getJCas();
    } catch (CASException e) {
      throw new ResourceProcessException(e);
    }

    FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();

    if (it.hasNext()) {
      Document doc = (Document) it.next();

      // Make sure that your previous annotators have populated this in CAS
      FSList fsTokenList = doc.getTokenList();
      int rel = doc.getRelevanceValue();
      int qid = doc.getQueryID();
      String text = doc.getText();
      ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
      Map<String, Integer> qMap = new HashMap<String, Integer>();
      for (int i = 0; i < tokenList.size(); i++) {
        qMap.put(tokenList.get(i).getText(), tokenList.get(i).getFrequency());
      }
      if (rel == 99) {

        Doctype dt = new Doctype(rel, qid, text, qMap);
        questionList.add(dt);
        qnum++;
      } else if (docMap.containsKey(qid)) {
        List<Doctype> relList = docMap.get(qid);
        Doctype dt = new Doctype(rel, qid, text, qMap);
        relList.add(dt);
        docMap.put(qid, relList);
      } else {
        List<Doctype> relList = new ArrayList<Doctype>();
        Doctype dt = new Doctype(rel, qid, text, qMap);
        relList.add(dt);
        docMap.put(qid, relList);
      }

      // qIdList.add(doc.getQueryID());
      // relList.add(doc.getRelevanceValue());

      // Do something useful here

    }

  }

  /**
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
   */
  @Override
  public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
          IOException {

    super.collectionProcessComplete(arg0);

    // TODO :: compute the cosine similarity measure

    // System.out.println("hehe");
    List<Doctype> outputList = new ArrayList<Doctype>();
    for (int i = 0; i < questionList.size(); i++) {
      Doctype qdt = questionList.get(i);
      Map<String, Integer> qmap = qdt.gettList();
      if (docMap.containsKey(qdt.getqid())) {
        List<Doctype> dtList = docMap.get(qdt.getqid());
        double[] cosSim = new double[dtList.size()];
        for (int j = 0; j < dtList.size(); j++) {
          Doctype ddt = dtList.get(j);
          Map<String, Integer> dmap = ddt.gettList();
          double csim = computeCosineSimilarity(qmap, dmap);
          cosSim[j] = csim;
          ddt.setcossim(csim);
          if (ddt.getrel() == 1) {
            outputList.add(ddt);
          }
//          System.out.println("cosine=" + ddt.getcossim() + " rank=" + ddt.getrank() + " qid="
//                  + ddt.getqid() + " rel=" + ddt.getrel() + " " + ddt.getdoc());
        }
        Arrays.sort(cosSim);
        Doctype dt = outputList.get(i);
        for (int k = 0; k < cosSim.length; k++) {
          if (cosSim[k] == dt.getcossim()) {
            dt.setrank(cosSim.length-k);
            break;
          }
        }
    //    System.out.println(cosSim);
      } else {

      }
      // List<Doctype>

    }
    for (int i = 0; i < outputList.size(); i++) {
      Doctype dt = outputList.get(i);
      System.out.println("cosine=" + dt.getcossim() + " rank=" + dt.getrank() + " qid="
              + dt.getqid() + " rel=" + dt.getrel() + " " + dt.getdoc());
    }

    // TODO :: compute the rank of retrieved sentences

    // TODO :: compute the metric:: mean reciprocal rank
    double metric_mrr = compute_mrr(outputList);
    System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
  }

  /**
   * 
   * @return cosine_similarity
   */
  private double computeCosineSimilarity(Map<String, Integer> queryVector,
          Map<String, Integer> docVector) {
    double cosine_similarity = 0.0;

    // TODO :: compute cosine similarity between two sentences
    double numerator = 0.0, denominator1 = 0.0, denominator2 = 0.0;
    int temp1, temp2;
    Map<String, Integer> qmap = new HashMap<String, Integer>(queryVector);
    Map<String, Integer> dmap = new HashMap<String, Integer>(docVector);
    if ((qmap.size() < 1) || (dmap.size() < 1)) {
      return 0.0;
    }
    Set<String> qSet = qmap.keySet();
    Iterator<String> qIt = qSet.iterator();
    while (qIt.hasNext()) {
      String key = qIt.next();
      temp1 = qmap.get(key);
      if (dmap.containsKey(key)) {
        temp2 = dmap.get(key);
      } else {
        temp2 = 0;
      }
      dmap.remove(key);
      numerator += temp1 * temp2;
      denominator1 += temp1 * temp1;
      denominator2 += temp2 * temp2;
    }
    Set<String> dSet = dmap.keySet();
    Iterator<String> dIt = dSet.iterator();
    while (dIt.hasNext()) {
      String key = dIt.next();
      temp2 = dmap.get(key);
      denominator2 += temp2 * temp2;
    }

    cosine_similarity = numerator / (Math.sqrt(denominator1) * Math.sqrt(denominator2));
    return cosine_similarity;

  }

  /**
   * 
   * @return mrr
   */
  private double compute_mrr(List<Doctype> outputList) {
    double metric_mrr = 0.0;
    int q = outputList.size();
    for(int i = 0;i<q;i++){
      metric_mrr += 1.0/outputList.get(i).getrank();
    }
    metric_mrr = metric_mrr/q;
    // TODO :: compute Mean Reciprocal Rank (MRR) of the text collection

    return metric_mrr;
  }

}
