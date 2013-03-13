/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

 /*
	Modified by William Palmer (William.Palmer@bl.uk)
	Modifications copyright British Library, 2013
 
 */
 
package org.apache.pdfbox.preflight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.activation.FileDataSource;

import org.apache.pdfbox.Version;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;

/**
 * This class is a simple main class used to check the validity of a pdf file.
 * 
 * Usage : java net.awl.edoc.pdfa.Validator <file path>
 * 
 * @author gbailleul
 * 
 */
public class Validator_A1b_XML
{
	
	private static void validate(String inputFile, String outputFile) throws IOException {

		System.out.println("Processing: "+inputFile+" -> "+outputFile);

		long startTime = System.currentTimeMillis();

		PrintWriter out = new PrintWriter(new FileWriter(outputFile));
		
		ValidationResult result = null;
		String pdfType = null;

		try {
			FileDataSource fd = new FileDataSource(inputFile);
			PreflightParser parser = new PreflightParser(fd);
			try
			{
				parser.parse();
				PreflightDocument document = parser.getPreflightDocument();
				document.validate();
				pdfType = document.getSpecification().getFname();
				result = document.getResult();
				document.close();
			}
			catch (SyntaxValidationException e)
			{
				result = e.getResult();
			}
		} 
		catch(Exception e) 
		{
			out.println("<?xml version=\"1.0\" ?>");
			out.println("<preflight name=\"" + inputFile + "\">");
			out.println("     <executionTimeMS>"+(System.currentTimeMillis()-startTime)+"</executionTimeMS>");
			out.println("     <isValid type=\""+pdfType+"\">false</isValid>");
			out.println("     <exceptionThrown>");
			out.println("          <message>"+e.getMessage()+"</message>");
			out.println("          <stackTrace>");
			e.printStackTrace(out);
			out.println("          </stackTrace>");
			out.println("     </exceptionThrown>");
			out.println("</preflight>");
		}

		if(result==null) {
			out.close();
			return;
		}

		if (result.isValid())
		{
			out.println("<?xml version=\"1.0\" ?>");
			out.println("<preflight name=\"" + inputFile + "\">");
			out.println("     <executionTimeMS>"+(System.currentTimeMillis()-startTime)+"</executionTimeMS>");
			out.println("     <isValid type=\""+pdfType+"\">true</isValid>");
			out.println("</preflight>");
		}
		else
		{

			ArrayList<ValidationError> errorList = new ArrayList<ValidationError>();
			ArrayList<Integer> errorCount = new ArrayList<Integer>();

			List<ValidationError> errors = result.getErrorsList();
			while(!errors.isEmpty()){
				ValidationError error = result.getErrorsList().get(0);
				boolean found = false;
				for(int i=0;i<errorList.size();i++) {
					ValidationError e2 = errorList.get(i);
					if(e2.getErrorCode().equals(error.getErrorCode())&
							e2.getDetails().equals(error.getDetails())) {
						found = true;
						errorCount.set(i, errorCount.get(i)+1);
						break;
					}
				}
				if(!found) {
					errorList.add(error);
					errorCount.add(1);
				} 
				errors.remove(0);
			}

			out.println("<?xml version=\"1.0\" ?>");
			out.println("<preflight name=\"" + inputFile + "\">");
			out.println("     <executionTimeMS>"+(System.currentTimeMillis()-startTime)+"</executionTimeMS>");
			out.println("     <isValid type=\""+pdfType+"\">false</isValid>");
			for(int i=0;i<errorList.size();i++)
			{
				ValidationError error = errorList.get(i);
				int count = errorCount.get(i);
				out.println("     <error>");
				out.println("          <count>"+count+"</count>");            	
				out.println("          <code>"+error.getErrorCode()+"</code>");            	
				out.println("          <details>"+error.getDetails()+"</details>");            	
				out.println("     </error>");
			}
			out.println("</preflight>");
		}
		out.close();
	}

    public static void main(String[] args) 
    {
    	
        if (args.length == 0)
        {
            System.out.println("Usage : java org.apache.pdfbox.preflight.Validator_A1b <file path>");
            System.out.println("Usage : java org.apache.pdfbox.preflight.Validator_A1b --list <listoffiles.txt>");
            System.out.println("Version : " + Version.getVersion());
            System.exit(1);
        }
        List<String> files = new LinkedList<String>();
        if(args[0].equals("--list")) {
        	String file = args[1];
        	if(new File(file).exists()) {
        		try {
					BufferedReader buf = new BufferedReader(new FileReader(args[1]));
					while(buf.ready()) {
						String f = buf.readLine();
						if(new File(f).exists()) {
							files.add(f);
						}
					}
					buf.close();
				} catch (FileNotFoundException e) {
					System.out.println("File not found: "+file);
					System.exit(-2);
				} catch (IOException e) {
					System.out.println("IO error");
					System.exit(-2);
				}
        	} else {
				System.out.println("File not found: "+file);
        		System.exit(-2);
        	}
        } else {
        	if(new File(args[0]).exists()) {
        		files.add(args[0]);
        	} else {
				System.out.println("File not found: "+args[0]);
        		System.exit(-2);
        	}
        }
        
        for(String file:files) {
        	try {
				validate(file, file+".preflight.xml");
			} catch (IOException e) {
				System.out.println("Failed to validate: "+file);
			}
        }


    }
}
