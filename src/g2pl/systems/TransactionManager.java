/*******************************************************************************
 *     2PLDBB is a centralized two phase locking distributed system.
 *     Copyright (C) 2015  Yidan(Evan) Zheng
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package g2pl.systems;

import g2pl.basics.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionManager {

	public List<Transaction> history;
	
	int count = 0;
	int offset = Constants.TID_OFFSET;
	int transactionManagerId;
	
	
	public TransactionManager(int tmid)
	{
		transactionManagerId = tmid;
		history = new ArrayList<Transaction>();
	}
	
	
	public void loadTransactions(String filePath) throws Exception
	{
		List<String> commands = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
	    try {
	        String line = br.readLine();

	        while (line != null && !line.trim().isEmpty()) {
	            commands.add(line);
	            line = br.readLine();
	        }
	    } finally {
	        br.close();
	    }
	    
	    String[] commands_arr = new String[commands.size()];
	    
	    commands_arr = commands.toArray(commands_arr);
	    
	    pushTransactions(commands_arr);
	}
	
	public void pushTransactions(String[] commands) throws Exception
	{
		if(commands.length > 0)
		{
			Transaction trans = null;
			
			for(String command: commands)
			{
				if(command.toLowerCase().contains("transaction"))
				{
					if(trans != null)
					{
						history.add(trans);
						trans = new Transaction(getNewTransactionUID());
						
					}else
					{
						trans = new Transaction(getNewTransactionUID());
					}
				}else
				{
					switch(getCommandType(command))
					{
						case 'w':
						{
							Pattern pattern = Pattern.compile(".*\\((.*)\\).*");
							Matcher m = pattern.matcher(command);
							if(m.matches())
							{
								String varId = m.group(1);
								Operation op = new Operation(trans.getTransId(), Constants.OP_WRITE, varId);							
								trans.addOperation(op);
							}else
							{
								throw new Exception("Write operation format wrong!");
							}
							
							break;
						}
						case 'r':
						{
							Pattern pattern = Pattern.compile(".*\\((.*)\\).*");
							Matcher m = pattern.matcher(command);
							if(m.matches())
							{
								String varId = m.group(1);
								Operation op = new Operation(trans.getTransId(), Constants.OP_READ, varId);							
								trans.addOperation(op);
							}else
							{
								throw new Exception("Read operation format wrong!");
							}
							
							break;
						}
						case 'm':
						{
							Pattern pattern = Pattern.compile("m(.*)\\=(.*)(\\+|\\-|\\*|\\/)(.*);");
							Matcher m = pattern.matcher(command.trim());
							if(m.matches()) // binary operation
							{
								String varId = m.group(1);
								String op1 = m.group(2);
								String operator = m.group(3);
								String op2 = m.group(4);
								
								Operation op = new Operation(trans.getTransId(), Constants.OP_MATH, varId, op1, operator, op2);	
								trans.addOperation(op);
	
							}else
							{
								pattern = Pattern.compile("m(.*)\\=(.*);");
								m = pattern.matcher(command.trim());
								
								if(m.matches()) // unary operation
								{
									String varId = m.group(1);
									String op1 = m.group(2);
									Operation op = new Operation(trans.getTransId(), Constants.OP_MATH, varId, op1, "+", "0");
									trans.addOperation(op);
									
								}else								
									throw new Exception("Math operation format wrong!");
							}
							
							break;
						}
						default:
							throw new Exception("Undefined operation!");
					}
				}
			}
			
			if(trans != null)
				history.add(trans);
		}
	}
	
	public Transaction popTransactions()
	{
		if(history.size() >0)
		{
			return history.remove(0);
		}else
			return null;
	}
	
	private int getNewTransactionUID()
	{
		int uid = count * offset + transactionManagerId;
		count ++;
		
		return uid;
	}
	
	private char getCommandType(String command)
	{
		return command.trim().charAt(0);
	}
	
}
