/*
 *  StandAloneAnnie.java
 *
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  hamish, 29/1/2002
 *
 *  $Id: StandAloneAnnie.java,v 1.6 2006/01/09 16:43:22 ian Exp $
 */

package com.jesinity;

import gate.*;
import gate.corpora.RepositioningInfo;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/**
 * This class illustrates how to use ANNIE as a sausage machine
 * in another application - put ingredients in one end (URLs pointing
 * to documents) and get sausages (e.g. Named Entities) out the
 * other end.
 * <P><B>NOTE:</B><BR>
 * For simplicity's sake, we don't do any exception handling.
 */
public class StandAloneAnnie  {

  /** The Corpus Pipeline application to contain ANNIE */
  private CorpusController annieController;

  /**
   * Initialise the ANNIE system. This creates a "corpus pipeline"
   * application that can be used to run sets of documents through
   * the extraction system.
   */
  public void initAnnie() throws GateException, IOException {
    Out.prln("Initialising ANNIE...");

    // load the ANNIE application from the saved state in plugins/ANNIE
    File pluginsHome = Gate.getPluginsHome();
    File anniePlugin = new File(pluginsHome, "ANNIE");
    File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
    annieController =
      (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);

    Out.prln("...ANNIE loaded");
  } // initAnnie()

  /** Tell ANNIE's controller about the corpus you want to run on */
  public void setCorpus(Corpus corpus) {
    annieController.setCorpus(corpus);
  } // setCorpus

  /** Run ANNIE */
  public void execute() throws GateException {
    Out.prln("Running ANNIE...");
    annieController.execute();
    Out.prln("...ANNIE complete");
  } // execute()

  /**
   * Run from the command-line, with a list of URLs as argument.
   * <P><B>NOTE:</B><BR>
   * This code will run with all the documents in memory - if you
   * want to unload each from memory after use, add code to store
   * the corpus in a DataStore.
   */
  public static void main(String argo[]) throws GateException, IOException {


    System.setProperty("gate.plugins.home","/Applications/GATE_Developer_8.1/plugins");
    System.setProperty("gate.site.config","/Applications/GATE_Developer_8.1/gate.xml");

    // initialise the GATE library
    Out.prln("Initialising GATE...");
    Gate.init();
    Out.prln("...GATE initialised");

    // initialise ANNIE (this may take several minutes)
    StandAloneAnnie annie = new StandAloneAnnie();
    annie.initAnnie();

//    String[] arzgs = {"R3 CEV is a New York-based financial innovation firm that Mike Hearn joined as the chief platform officer several months prior to his announcement that Bitcoin was a “failed experiment.” R3 focuses on distributed ledger technology and has partnered with 42 banks around the world, such as Goldman Sachs, HSBC and Toronto Dominion, over the past year to create a blockchain consortium of financial institutions.\n" +
//            "R3 CEV believes in the value of a private “permissioned” blockchain, rather than a public “permissionless” blockchain. The entire world has access to a permissionless ledger, and it requires a digital asset, such as bitcoin, to operate as a financial incentive to encourage people and businesses to contribute their computing power to secure the network. This also deters fraud as the network grows larger. Only a select group of trusted parties is required to maintain a permissioned blockchain.\n" +
//            "Since its foundation, R3 has operated mostly outside of public scrutiny. In an interview with Coindesk this past summer, founder David Rutter and partner Todd McDonald explained that “R3 CEV has been playing a quiet yet concerted game to bring blockchain technology to the world’s largest banks and financial institutions.”\n" +
//            "Recently though, R3 has emerged from the shadows, and company leaders have begun to discuss their plan publically and how it affects bitcoin and the financial services industry. Rutter announced in a press release that “partnering with a broad range of institutions has always been central to our strategy [and] securing the backing of 42 of the world’s leading banks demonstrates the level of interest in our initiative, and we now look forward to exploring collaboration with non-bank institutions and expanding our already diverse group.”\n" +
//            "On January 14th The Hutchins Center on Fiscal and Monetary Policy at the Brookings Institution hosted a livecast with a group that included Charley Cooper, the managing partner of business development and marketing at R3 CEV, in what was probably the most thorough explanation of their perspective on the market.\n" +
//            "Cooper explained that the focus at R3 has shifted over the past couple years to distributed ledger technology after discussions with both Wall Street bankers as well as technologists. Wall Street bankers have become interested in understanding blockchain technology, and technologists, the ones building the software, need to familiarize themselves with the financial services market anti-money laundering laws and know-your-customer regulations.\n" +
//            "Put simply by Cooper in the webcast, “there are amazing technology companies who are making really cool stuff that is totally irrelevant to what the financial services market is doing.” Financial services employees operate in one of the most highly regulated markets in the world, regulations that technologists must understand to ensure their technology has a real-life application.\n" +
//            "Cooper adds that there are several instances in history where banking consortiums have come together to successfully improve the banking experience, such as Markit Tradeweb, FXall and E Speed Broker Tech. In an interview with American Banker, he explains that R3 CEV is building technology to custom fit the needs of the banking consortium, rather than many financial technology companies that are building software before showing banks, only to find their model doesn’t comply with regulations.\n" +
//            "The belief at R3 is that innovation is more possible once all the market players are on the same team, rather than the decentralized and open source nature of innovation in the bitcoin blockchain community. In addition, both Cooper and Rutter believe the bitcoin blockchain is not suitable for use on the scale of the financial services market due to its limited block size, the complexity of transactions and regulators not approving of anonymous nodes varying the network. Cooper admitted, though, he might change his mind if regulators began to agree with a permissionless blockchain system.\n" +
//            "On January 20th, R3 announced the launch of a private distributed ledger that connects 11 member banks using Ethereum technology and hosted on a virtual private network in Microsoft Azure’s Blockchain as a Service. According to International Business Times, the banks to join the peer-to-peer network first include Barclays, BMO Financial Group, Credit Suisse, Commonwealth Bank of Australia, HSBC, Natixis, Royal Bank of Scotland, TD Bank, UBS, UniCredit and Wells Fargo. The successful completion of this network marks an important step for blockchain technology; it is now being used by the world’s premier financial institutions.\n" +
//            "Even if the R3 team is right in that a private network is the best way to stimulate innovation in the financial services industry, there are many applications for Bitcoin and the Bitcoin blockchain in industries outside of financial services. If either Bitcoins’ permissionless blockchain or the permissioned blockchain of the banking consortium picks up more traction in 2016, it will likely have a positive effect on the entire financial technology sector.\n" +
//            "This article has been updated to correct the timeline and show that Hearn joined R3 CEV months before his post about Bitcoin's failure.\n" +
//            "\n" +
//            "Michael Gord is the founder of Bitcoin Canada and the McGill Cryptocurrency Club. While at McGill, Michael organized the Bitcoin Airdrop events where he gave hundreds of students their first bitcoin.\n" +
//            "\n" +
//            "This article has been edited to correct the spelling of firms Tradeweb and Markit"};


    String[] args = {
            "https://gate.ac.uk/wiki/code-repository/",
            "http://uk.businessinsider.com/uber-protests-in-paris-2016-1",
            "http://uk.businessinsider.com/11-banks-in-r3-consortium-use-blockchain-technology-to-trade-2016-1",
            "http://www.eu-startups.com/2015/07/the-15-hottest-european-fintech-startups-in-2015/",
             "http://www.foxbusiness.com/features/2015/05/18/30-hot-fintech-startups-to-watch.html"
    };

    // create a GATE corpus and add a document for each command-line
    // argument
    Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
    for(int i = 0; i < args.length; i++) {
      URL u = new URL(args[i]);
      FeatureMap params = Factory.newFeatureMap();
      params.put("sourceUrl", u);
      params.put("preserveOriginalContent", new Boolean(true));
      params.put("collectRepositioningInfo", new Boolean(true));
      //Out.prln("Creating doc for " + u);
      Document doc = (Document)
        Factory.createResource("gate.corpora.DocumentImpl", params);
      //doc.setContent(new DocumentContentImpl(args[i]));
      corpus.add(doc);
    } // for each of args

    // tell the pipeline about the corpus and run it
    annie.setCorpus(corpus);
    annie.execute();

    // for each document, get an XML document with the
    // person and location names added
    Iterator iter = corpus.iterator();
    int count = 0;
    String startTagPart_1 = "<span GateID=\"";
    String startTagPart_2 = "\" title=\"";
    String startTagPart_3 = "\" style=\"background:Red;\">";
    String endTag = "</span>";

    while(iter.hasNext()) {
      Document doc = (Document) iter.next();
      AnnotationSet defaultAnnotSet = doc.getAnnotations();
      Set annotTypesRequired = new HashSet();
      annotTypesRequired.add("Person");
      annotTypesRequired.add("Location");
      annotTypesRequired.add("Organization");
      Set<Annotation> peopleAndPlaces =
        new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));

      FeatureMap features = doc.getFeatures();
      String originalContent = (String)
        features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
      RepositioningInfo info = (RepositioningInfo)
        features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);

      ++count;
      File file = new File("StANNIE_" + count + ".HTML");
      Out.prln("File name: '"+file.getAbsolutePath()+"'");
      if(originalContent != null && info != null) {
        Out.prln("OrigContent and reposInfo existing. Generate file...");

        Iterator it = peopleAndPlaces.iterator();
        Annotation currAnnot;
        SortedAnnotationList sortedAnnotations = new SortedAnnotationList();

        while(it.hasNext()) {
          currAnnot = (Annotation) it.next();
          sortedAnnotations.addSortedExclusive(currAnnot);
        } // while

        StringBuffer editableContent = new StringBuffer(originalContent);
        long insertPositionEnd;
        long insertPositionStart;
        // insert anotation tags backward
        Out.prln("Unsorted annotations count: "+peopleAndPlaces.size());
        Out.prln("Sorted annotations count: "+sortedAnnotations.size());
        for(int i=sortedAnnotations.size()-1; i>=0; --i) {
          currAnnot = (Annotation) sortedAnnotations.get(i);
          insertPositionStart =
            currAnnot.getStartNode().getOffset().longValue();
          insertPositionStart = info.getOriginalPos(insertPositionStart);
          insertPositionEnd = currAnnot.getEndNode().getOffset().longValue();
          insertPositionEnd = info.getOriginalPos(insertPositionEnd, true);
          if(insertPositionEnd != -1 && insertPositionStart != -1) {
            editableContent.insert((int)insertPositionEnd, endTag);
            editableContent.insert((int)insertPositionStart, startTagPart_3);
            editableContent.insert((int)insertPositionStart,
                                                          currAnnot.getType());
            editableContent.insert((int)insertPositionStart, startTagPart_2);
            editableContent.insert((int)insertPositionStart,
                                                  currAnnot.getId().toString());
            editableContent.insert((int)insertPositionStart, startTagPart_1);
          } // if
        } // for

        FileWriter writer = new FileWriter(file);
        writer.write(editableContent.toString());
        writer.close();
      } // if - should generate
      else if (originalContent != null) {
        Out.prln("OrigContent existing. Generate file...");

        Iterator it = peopleAndPlaces.iterator();
        Annotation currAnnot;
        SortedAnnotationList sortedAnnotations = new SortedAnnotationList();

        while(it.hasNext()) {
          currAnnot = (Annotation) it.next();
          sortedAnnotations.addSortedExclusive(currAnnot);
        } // while

        StringBuffer editableContent = new StringBuffer(originalContent);
        long insertPositionEnd;
        long insertPositionStart;
        // insert anotation tags backward
        Out.prln("Unsorted annotations count: "+peopleAndPlaces.size());
        Out.prln("Sorted annotations count: "+sortedAnnotations.size());
        for(int i=sortedAnnotations.size()-1; i>=0; --i) {
          currAnnot = (Annotation) sortedAnnotations.get(i);
          insertPositionStart =
            currAnnot.getStartNode().getOffset().longValue();
          insertPositionEnd = currAnnot.getEndNode().getOffset().longValue();
          if(insertPositionEnd != -1 && insertPositionStart != -1) {
            editableContent.insert((int)insertPositionEnd, endTag);
            editableContent.insert((int)insertPositionStart, startTagPart_3);
            editableContent.insert((int)insertPositionStart,
                                                          currAnnot.getType());
            editableContent.insert((int)insertPositionStart, startTagPart_2);
            editableContent.insert((int)insertPositionStart,
                                                  currAnnot.getId().toString());
            editableContent.insert((int)insertPositionStart, startTagPart_1);
          } // if
        } // for

        FileWriter writer = new FileWriter(file);
        writer.write(editableContent.toString());
        writer.close();
      }
      else {
        Out.prln("Content : "+originalContent);
        Out.prln("Repositioning: "+info);
      }

      String xmlDocument = doc.toXml(peopleAndPlaces, false);
      String fileName = new String("StANNIE_toXML_" + count + ".HTML");
      FileWriter writer = new FileWriter(fileName);
      writer.write(xmlDocument);
      writer.close();

    } // for each doc
  } // main

  /**
   *
   */
  public static class SortedAnnotationList extends Vector {
    public SortedAnnotationList() {
      super();
    } // SortedAnnotationList

    public boolean addSortedExclusive(Annotation annot) {
      Annotation currAnot = null;

      // overlapping check
      for (int i=0; i<size(); ++i) {
        currAnot = (Annotation) get(i);
        if(annot.overlaps(currAnot)) {
          return false;
        } // if
      } // for

      long annotStart = annot.getStartNode().getOffset().longValue();
      long currStart;
      // insert
      for (int i=0; i < size(); ++i) {
        currAnot = (Annotation) get(i);
        currStart = currAnot.getStartNode().getOffset().longValue();
        if(annotStart < currStart) {
          insertElementAt(annot, i);
          /*
           Out.prln("Insert start: "+annotStart+" at position: "+i+" size="+size());
           Out.prln("Current start: "+currStart);
           */
          return true;
        } // if
      } // for

      int size = size();
      insertElementAt(annot, size);
//Out.prln("Insert start: "+annotStart+" at size position: "+size);
      return true;
    } // addSorted
  } // SortedAnnotationList
} // class StandAloneAnnie