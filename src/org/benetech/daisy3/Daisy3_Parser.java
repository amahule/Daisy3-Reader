package org.benetech.daisy3;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Daisy3_Parser {

	/**
	 * Parse the input file using DOM parser and return a String
	 * @param daisy3_file The File to be parsed
	 * @return String Selective Contents of the file i.e. not all elements from the XML
	 */
	public String parseFile(File daisy3_file){
		
		String str_ParsedFile="";
		
		System.out.println("********* Inside parseFile ******");
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		StringBuffer str_buf = new StringBuffer();

		try{

			str_buf.append("\n\nThis is a Daisy 3 book, brought to you by Bookshare.\n\n");

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file.
			//Entire XML document is returned
			doc = db.parse(daisy3_file);

			//Get the root element of the xml document
			Element root = doc.getDocumentElement();

			System.out.println("******* Name of the root: "+root);
			NodeList doctitle = root.getElementsByTagName("doctitle");
			String str_doctitle = doctitle.item(0).getTextContent();
			str_buf.append("Book Title: "+str_doctitle+".\n\n");

			StringBuffer authors = new StringBuffer();
			NodeList docauthor_list = root.getElementsByTagName("docauthor");

			if(docauthor_list != null && docauthor_list.getLength() > 0){
				for(int i = 0; i < docauthor_list.getLength(); i++){
					authors.append(docauthor_list.item(i).getTextContent());
					if(i < docauthor_list.getLength() - 1){
						authors.append(", ");
					}
				}
			}

			authors.append(".");

			if(docauthor_list.getLength() > 1){
				str_buf.append("Authors: ");
			}else if(docauthor_list.getLength() == 1){
				str_buf.append("Author: ");
			}
			str_buf.append(authors+"\n\n\n");


			NodeList nodelist = root.getElementsByTagName("p");
			Node node;

			//Check if the nodelist is populated
			if(nodelist != null && nodelist.getLength() > 0){
				for(int i = 0; i < nodelist.getLength(); i++){
					node = nodelist.item(i);
					str_buf.append(node.getTextContent()+"\n");
				}
				str_ParsedFile = str_buf.toString();
			}

		}
		catch(ParserConfigurationException e){
			e.printStackTrace();}
		catch(SAXException e){
			e.printStackTrace();}
		catch(IOException ioe){
			ioe.printStackTrace();}

		return str_ParsedFile;		

	}

}
