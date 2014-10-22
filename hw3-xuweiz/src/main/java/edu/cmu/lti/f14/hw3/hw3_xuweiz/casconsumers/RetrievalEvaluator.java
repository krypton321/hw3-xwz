package edu.cmu.lti.f14.hw3.hw3_xuweiz.casconsumers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
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
import edu.cmu.lti.f14.hw3.hw3_xuweiz.DoctypeComparator;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Document;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Token;
import edu.cmu.lti.f14.hw3.hw3_xuweiz.utils.Utils;

public class RetrievalEvaluator extends CasConsumer_ImplBase {


  // public ArrayList<Integer> qIdList;


  // public ArrayList<Integer> relList;
  /** document information map **/
  public static HashMap<Integer, List<Doctype>> docMap;

  /** question information list **/
  public static List<Doctype> questionList;

  public static int qnum = 0;

  /** Relevant document rank list**/
  public static List<Integer> mrrList;
  
  public String reportdir ="report.txt";
/**
 * Initialize report directory and static parameter in this method.
 */
  public void initialize() throws ResourceInitializationException {

    // qIdList = new ArrayList<Integer>();

    // relList = new ArrayList<Integer>();

    docMap = new HashMap<Integer, List<Doctype>>();

    questionList = new ArrayList<Doctype>();

    mrrList = new ArrayList<Integer>();

    PrintWriter writer = null;
    try {
      writer = new PrintWriter(reportdir);
      writer.print("");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      try {
        writer.close();
      } catch (Exception ex) {
      }
    }
  }

  /**
   * TODO :: 1. construct the global document dictionary 
   *                 2. construct the global question list 
   * @param aJCas
   *          store queries vectors
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
   * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 
   *              2. Compute the MRR metric
   *              3. Output the result.
   * @param arg0
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
        for (int j = 0; j < dtList.size(); j++) {
          Doctype ddt = dtList.get(j);
          Map<String, Integer> dmap = ddt.gettList();
          double csim = computeCosineSimilarity(qmap, dmap);
          ddt.setcossim(csim);
        }
        DoctypeComparator dc = new DoctypeComparator();
        dtList.sort(dc);
        for (int k = 0; k < dtList.size(); k++) {
          Doctype dt = dtList.get(k);
          dt.setrank(k + 1);
          if (dt.getrel() == 1) {
            outputList.add(dt);
          }
          Formatter formate = new Formatter();
//          String reportline = "cosine=" + formate.format("%.4f", dt.getcossim()) + "  rank="
//                  + dt.getrank() + "  qid=" + dt.getqid() + " rel=" + dt.getrel() + " " + dt.getdoc();
//          System.out.println(reportline);
        }

      } else {

      }

    }

    for (int i = 0; i < outputList.size(); i++) {
      Formatter formate = new Formatter();
      Doctype dt = outputList.get(i);
      String reportline = "cosine=" + formate.format("%.4f", dt.getcossim()) + "  rank="
              + dt.getrank() + "  qid=" + dt.getqid() + " rel=" + dt.getrel() + " " + dt.getdoc();
      System.out.println(reportline);
      print_report(reportline);
    }

    // TODO :: compute the rank of retrieved sentences

    // TODO :: compute the metric:: mean reciprocal rank
    double metric_mrr = compute_mrr(outputList);
    Formatter formate = new Formatter();
    String reportline = "(MRR) Mean Reciprocal Rank ::" + formate.format("%.4f",metric_mrr);
    System.out.println(reportline);
    print_report(reportline);
  }

  /**
   * Compute cosine_similarity
   * @param queryVector
   * @param docVector
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
   * Compute mrr
   * @param outputList
   * @return mrr
   */
  private double compute_mrr(List<Doctype> outputList) {
    double metric_mrr = 0.0;
    int q = outputList.size();
    for (int i = 0; i < q; i++) {
      metric_mrr += 1.0 / outputList.get(i).getrank();
    }
    metric_mrr = metric_mrr / q;
    // TODO :: compute Mean Reciprocal Rank (MRR) of the text collection

    return metric_mrr;
  }


  private void print_report(String str) throws ResourceProcessException {
    FileWriter writer = null;
    BufferedWriter bw = null;
    try {
      writer = new FileWriter(reportdir, true);
      bw = new BufferedWriter(writer);
      bw.append(str + "\n");

    } catch (IOException e) {
      throw new ResourceProcessException(e);
    } finally {
      try {
        bw.close();
      } catch (Exception ex) {
      }
      try {
        writer.close();
      } catch (Exception ex) {
      }
    }
  }
}
