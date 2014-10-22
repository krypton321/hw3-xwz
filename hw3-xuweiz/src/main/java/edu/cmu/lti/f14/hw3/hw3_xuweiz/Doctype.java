package edu.cmu.lti.f14.hw3.hw3_xuweiz;

import java.util.List;
import java.util.Map;

import edu.cmu.lti.f14.hw3.hw3_xuweiz.typesystems.Token;

/**
 * Description: Store document related information.
 * 
 * @author Xuwei Zou
 *
 */
public class Doctype {
  private int rel;
  
  private int qid;
  
  private int rank;
  
  private double cosSim;
  
  private String doc;
  
  private Map<String,Integer> tList;
  
  public Doctype(int r, int q, String d, Map<String,Integer> t){
    rel = r;
    qid =q;
    doc = d;
    tList = t;
  }
  
  public void setrel(int r){
    rel =r;
  }
  
  public void setqid(int q){
    qid = q;
  }
  
  public void setrank(int r){
    rank =r;
  }
  
  public void setcossim(double c){
    cosSim = c;
  }
  public void setdoc(String d){
    doc = d;
  }
  
  public void settList(Map<String,Integer> t){
    tList = t;
  }
  
  public int getrel(){
    return rel;
  }
  
  public int getqid(){
    return qid;
  }
  
  public int getrank(){
    return rank;
  }
  
  public double getcossim(){
    return cosSim;
  }
  public String getdoc(){
    return doc;
  }
  
  public Map<String,Integer> gettList(){
    return tList;
  }
}
