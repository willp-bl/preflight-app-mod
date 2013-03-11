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

import java.util.ArrayList;
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
public class Validator_A1b
{

    public static void main(String[] args) 
    {
    	
    	long startTime = System.currentTimeMillis();
    	
        if (args.length == 0)
        {
            System.out.println("Usage : java org.apache.pdfbox.preflight.Validator_A1b <file path>");
            System.out.println("Version : " + Version.getVersion());
            System.exit(1);
        }

        ValidationResult result = null;
        String pdfType = null;

        try {
        	FileDataSource fd = new FileDataSource(args[0]);
        	PreflightParser parser = new PreflightParser(fd);
        	try
        	{
        		parser.parse();
        		PreflightDocument document = parser.getPreflightDocument();
        		document.validate();
        		pdfType = document.specification.getFname();
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
        	System.out.println("<preflight name=\"" + args[0] + "\">");
        	System.out.println("     <executionTimeMS>"+(System.currentTimeMillis()-startTime)+"</executionTimeMS>");
        	System.out.println("     <exceptionThrown>"+e.getMessage()+"</exceptionThrown>");
        	System.out.println("</preflight>");
        }

        if (result.isValid())
        {
            System.out.println("<preflight name=\"" + args[0] + "\">");
            System.out.println("     <executionTimeMS>"+(System.currentTimeMillis()-startTime)+"</executionTimeMS>");
            System.out.println("     <isValid type=\""+pdfType+"\">true</isValid>");
            System.out.println("</preflight>");
            System.exit(0);
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
        	
            System.out.println("<preflight name=\"" + args[0] + "\">");
            System.out.println("     <executionTimeMS>"+(System.currentTimeMillis()-startTime)+"</executionTimeMS>");
            System.out.println("     <isValid type=\""+pdfType+"\">false</isValid>");
            for(int i=0;i<errorList.size();i++)
            {
            	ValidationError error = errorList.get(i);
            	int count = errorCount.get(i);
            	System.out.println("     <error>");
            	System.out.println("          <count>"+count+"</count>");            	
            	System.out.println("          <code>"+error.getErrorCode()+"</code>");            	
            	System.out.println("          <details>"+error.getDetails()+"</details>");            	
            	System.out.println("     </error>");
            }
            System.out.println("</preflight>");
            
            System.exit(-1);
        }
    }
}
