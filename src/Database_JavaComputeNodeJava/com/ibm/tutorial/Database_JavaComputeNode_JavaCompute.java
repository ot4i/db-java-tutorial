package com.ibm.tutorial;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbJSON;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class Database_JavaComputeNode_JavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");

		MbMessage inMessage = inAssembly.getMessage();

		// create new empty message
		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly,
				outMessage);

		try {
			// optionally copy message headers
			copyMessageHeaders(inMessage, outMessage);
			// ----------------------------------------------------------
			// Add user code below
 			
	        // The IIB JDBC configurable service is called "TutorialExampleDB2ConfigurableService" ...
			//
			// mqsicreateconfigurableservice TESTNODE -c JDBCProviders -o TutorialExampleDB2ConfigurableService
			//  -n type4DatasourceClassName,type4DriverClassName,databaseType,jdbcProviderXASupport,portNumber,
			//     connectionUrlFormatAttr5,connectionUrlFormatAttr4,serverName,connectionUrlFormatAttr3,connectionUrlFormatAttr2,connectionUrlFormatAttr1,
			//     environmentParms,maxConnectionPoolSize,description,jarsURL,databaseName,databaseVersion,securityIdentity,connectionUrlFormat
			//  -v "com.ibm.db2.jcc.DB2XADataSource","com.ibm.db2.jcc.DB2Driver","DB2 Universal Database","false","50000","","","localhost","","","",
			//     "default_none","0","default_Description","C:\Program Files\IBM\SQLLIB\java","EMPLOYEE","10.5","TutorialJDBCIdentity","jdbc:db2://[serverName]:[portNumber]/[databaseName]:user=[user];password=[password];"						
			// mqsisetdbparms TESTNODE -n jdbc::TutorialJDBCIdentity -u YourDBUserid -p YourDBPassword
			Connection conn = getJDBCType4Connection("TutorialExampleDB2ConfigurableService", JDBC_TransactionType.MB_TRANSACTION_AUTO);  
	        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	        java.util.Date date = new java.util.Date();
	        long t = date.getTime();
	        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(t);
	        
	        String insertStatement = "INSERT INTO IIBADMIN.EMPLOYEE_RECORD (FIRSTNAME, LASTNAME, SALARY, WORKDEPT, JOB, CREATION_DATE) VALUES('"+
	        							inMessage.getRootElement().getFirstElementByPath("JSON/Data/FIRSTNAME").getValue()+"','"+	        							
	        							inMessage.getRootElement().getFirstElementByPath("JSON/Data/LASTNAME").getValue()+"',"+
	        							inMessage.getRootElement().getFirstElementByPath("JSON/Data/SALARY").getValue()+",'"+
	        							inMessage.getRootElement().getFirstElementByPath("JSON/Data/WORKDEPT").getValue()+"','"+
	        							inMessage.getRootElement().getFirstElementByPath("JSON/Data/JOB").getValue()+"','"+
	        							currentTimestamp+"')";	        								        	        
	        stmt.executeUpdate(insertStatement);	        	                	        
	        String selectStatement = "SELECT EMPNO FROM IIBADMIN.EMPLOYEE_RECORD AS A WHERE("+
	        							"A.FIRSTNAME='"+inMessage.getRootElement().getFirstElementByPath("JSON/Data/FIRSTNAME").getValue()+"' AND "+
	        							"A.LASTNAME='"+inMessage.getRootElement().getFirstElementByPath("JSON/Data/LASTNAME").getValue()+"' AND "+
	        							"A.SALARY="+inMessage.getRootElement().getFirstElementByPath("JSON/Data/SALARY").getValue()+" AND "+
	        							"A.WORKDEPT='"+inMessage.getRootElement().getFirstElementByPath("JSON/Data/WORKDEPT").getValue()+"' AND "+
	        							"A.JOB='"+inMessage.getRootElement().getFirstElementByPath("JSON/Data/JOB").getValue()+"' AND "+
	        							"A.CREATION_DATE='"+currentTimestamp+"')";
	        ResultSet rs = stmt.executeQuery(selectStatement);	        
	        String reply;
	        if (rs.next()) {
	        	int employeeNumber = rs.getInt(1);
	        	reply = "A row was inserted successfully. The assigned employee number (database column EMPNO) was "+Integer.toString(employeeNumber);
	        } else {
	        	reply = "A problem occurred";
	        }
	        MbElement outRoot = outMessage.getRootElement();
	        MbElement outputJSONRoot = outRoot.createElementAsLastChild(MbJSON.PARSER_NAME);
	        MbElement outputJsonData = outputJSONRoot.createElementAsLastChild(MbElement.TYPE_NAME, MbJSON.DATA_ELEMENT_NAME, null);
	        outputJsonData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "Message", reply);	        
	        if (stmt != null) stmt.close();
	        if (rs != null) rs.close();
	                  									
			// End of user code
			// ----------------------------------------------------------
		} catch (MbException e) {
			// Re-throw to allow Broker handling of MbException
			throw e;
		} catch (RuntimeException e) {
			// Re-throw to allow Broker handling of RuntimeException
			throw e;
		} catch (Exception e) {
			// Consider replacing Exception with type(s) thrown by user code
			// Example handling ensures all exceptions are re-thrown to be handled in the flow
			throw new MbUserException(this, "evaluate()", "", "", e.toString(),
					null);
		}
		// The following should only be changed
		// if not propagating message to the 'out' terminal
		out.propagate(outAssembly);
	}

	public void copyMessageHeaders(MbMessage inMessage, MbMessage outMessage)
			throws MbException {
		MbElement outRoot = outMessage.getRootElement();

		// iterate though the headers starting with the first child of the root
		// element
		MbElement header = inMessage.getRootElement().getFirstChild();
		while (header != null && header.getNextSibling() != null) // stop before
																	// the last
																	// child
																	// (body)
		{
			// copy the header and add it to the out message
			outRoot.addAsLastChild(header.copy());
			// move along to next header
			header = header.getNextSibling();
		}
	}

}
