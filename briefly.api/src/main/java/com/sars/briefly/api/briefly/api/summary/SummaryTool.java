package com.sars.briefly.api.briefly.api.summary;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;

public class SummaryTool {
	
	//char[] in;
	FileInputStream in;
	//CharSequence out;
	ArrayList<Sentence> sentences, contentSummary;
	ArrayList<Paragraph> paragraphs;
	int noOfSentences, noOfParagraphs;

	double[][] intersectionMatrix;
	LinkedHashMap<Sentence,Double> dictionary;
	public double compressionRate;
	public String summaryText = "";
	public double nOfWordsSummary;
	public int nOfParagraphsSummary;
	
	public SummaryTool(){
		in = null;
		//out = null;
		noOfSentences = 0;
		noOfParagraphs = 0;
	}
	
	public void init(){
		summaryText = "";
		sentences = new ArrayList<Sentence>();
		paragraphs = new ArrayList<Paragraph>();
		contentSummary = new ArrayList<Sentence>();
		dictionary = new LinkedHashMap<Sentence,Double>();
		noOfSentences = 0;
		noOfParagraphs = 0;
		try {
	        in = new FileInputStream(System.getProperty("user.dir") + File.separator + "sum.txt");
	        //out = new FileOutputStream("output.txt");
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	
	public void extractSentenceFromContext(){
		int nextChar,j=0;
		int prevChar = -1;
        try{
	        while((nextChar = in.read()) != -1) {
				j=0;
	        	char[] temp = new char[100000];
	        	while((char)nextChar != '.'){
	        		//System.out.println(nextChar + " ");
	        		temp[j] = (char)nextChar;
	        		if((nextChar = in.read()) == -1){
	        			break;
	        		}
	        		if((char)nextChar == '\n' && (char)prevChar == '\n'){
	        			noOfParagraphs++;
	        		}
	        		j++;
	        		prevChar = nextChar;
	        	}

	        	sentences.add(new Sentence(noOfSentences,(new String(temp)).trim(),(new String(temp)).trim().length(),noOfParagraphs));
	        	noOfSentences++;
	        	prevChar = nextChar;
	        }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	}
	
	
	public void groupSentencesIntoParagraphs(){
		int paraNum = 0;
		Paragraph paragraph = new Paragraph(0);

		for(int i=0;i<noOfSentences;i++){
			if(sentences.get(i).paragraphNumber == paraNum){
				//continue
			}else{
				paragraphs.add(paragraph);
				paraNum++;
				paragraph = new Paragraph(paraNum);
				
			}
			paragraph.sentences.add(sentences.get(i));
		}

		paragraphs.add(paragraph);
	}

	public double noOfCommonWords(Sentence str1, Sentence str2){
		double commonCount = 0;

		for(String str1Word : str1.value.split("\\s+")){
			for(String str2Word : str2.value.split("\\s+")){
				if(str1Word.compareToIgnoreCase(str2Word) == 0){
					commonCount++;
				}
			}
		}

		return commonCount;
	}

	public void createIntersectionMatrix(){
		intersectionMatrix = new double[noOfSentences][noOfSentences];
		for(int i=0;i<noOfSentences;i++){
			for(int j=0;j<noOfSentences;j++){

				if(i<=j){
					Sentence str1 = sentences.get(i);
					Sentence str2 = sentences.get(j);
					intersectionMatrix[i][j] = noOfCommonWords(str1,str2) / ((double)(str1.nOfWords + str2.nOfWords) /2);
				}else{
					intersectionMatrix[i][j] = intersectionMatrix[j][i];
				}
				
			}
		}
	}

	public void createDictionary(){
		for(int i=0;i<noOfSentences;i++){
			double score = 0;
			for(int j=0;j<noOfSentences;j++){
				score+=intersectionMatrix[i][j];
			}
			dictionary.put(sentences.get(i), score);
			((Sentence)sentences.get(i)).score = score;
		}
	}

	public void createSummary(){

	      for(int j=0;j<=noOfParagraphs;j++){
	      		int primary_set = paragraphs.get(j).sentences.size()/5; 

	      		//Sort based on score (importance)
	      		Collections.sort(paragraphs.get(j).sentences,new SentenceComparator());
		      	for(int i=0;i<=primary_set;i++){
		      		contentSummary.add(paragraphs.get(j).sentences.get(i));
		      	}
	      }

	      //To ensure proper ordering
	      Collections.sort(contentSummary,new SentenceComparatorForSummary());
		
	}


	public void printSentences(){
		for(Sentence sentence : sentences){
			System.out.println(sentence.number + " => " 
					+ sentence.value + " => " + sentence.stringLength  
					+ " => " + sentence.nOfWords + " => " + sentence.paragraphNumber);
		}
	}

	public void printIntersectionMatrix(){
		for(int i=0;i<noOfSentences;i++){
			for(int j=0;j<noOfSentences;j++){
				System.out.print(intersectionMatrix[i][j] + "    ");
			}
			System.out.print("\n");
		}
	}

	public void printDicationary(){
		  // Get a set of the entries
	      Set set = dictionary.entrySet();
	      // Get an iterator
	      Iterator i = set.iterator();
	      // Display elements
	      while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print(((Sentence)me.getKey()).value + ": ");
	         System.out.println(me.getValue());
	      }
	}

	public void printSummary(){
		System.out.println("no of paragraphs = "+ noOfParagraphs);
		for(Sentence sentence : contentSummary){
			System.out.println(sentence.value);
			summaryText = summaryText + sentence.value;
		}
	}

	public double getWordCount(ArrayList<Sentence> sentenceList){
		double wordCount = 0.0;
		for(Sentence sentence:sentenceList){
			wordCount +=(sentence.value.split(" ")).length;
		}
		return wordCount;
	}

	public void printStats(){
		System.out.println("number of words in Context : " + getWordCount(sentences));
		System.out.println("number of words in Summary : " + getWordCount(contentSummary));
		System.out.println("Commpression : " + getWordCount(contentSummary) / getWordCount(sentences) );
	}
	
	public void finalizeSummary() {
		compressionRate = getWordCount(contentSummary) / getWordCount(sentences) * 100;
		/*for(Sentence sentence : contentSummary){
			System.out.println(sentence.value);
			summaryText = summaryText + sentence.value;
		}*/
		System.out.println("1 -> ");
		System.out.println(summaryText);
		nOfWordsSummary = getWordCount(contentSummary);
		/*if(noOfParagraphs == 0) {
			nOfParagraphsSummary = 1;
		}
		else
			nOfParagraphsSummary = noOfParagraphs;*/
	}
	
}