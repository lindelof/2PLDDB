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
package g2pl.basics;

import g2pl.systems.DataProcessor;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

public class Transaction implements Serializable{
	
	private int uid;
	
	private List<Operation> operations;
	Hashtable<String, Integer> pre_commit;
	Hashtable<String, Integer> pre_write;
	
	public Transaction(int tid){
		
		uid = tid;
		operations = new ArrayList<Operation>();
		pre_commit = new Hashtable<String, Integer>();
		pre_write = new Hashtable<String, Integer>();
		
	}
	
	public String getName()
	{
		return "" + uid;
	}
	
	public void executeOperation(Operation op) throws Exception
	{
		Thread.sleep(Constants.DELAY_MILLISEC);
		
		switch(op.getType())
		{
			case Constants.OP_READ:
			{
				String key = op.getVarId();
				int value = DataProcessor.get_key(key);
				
				pre_commit.put(key, value);
				
				break;
			}
			case Constants.OP_WRITE:
			{
				String key = op.getVarId();
				
				if(pre_write.containsKey(key))
					pre_commit.put(key, pre_write.get(key));
				else
					throw new Exception("Write operation value not available");
				
				break;
			}
			case Constants.OP_MATH:
			{
				String op1 = op.getOp1();
				String op2 = op.getOp2();
				String operator = op.getOperator();
				
				int op1_value, op2_value;
				if(isNumeric(op1))
					op1_value = Integer.parseInt(op1);
				else
					op1_value = pre_commit.get(op1);
				
				if(isNumeric(op2))
					op2_value = Integer.parseInt(op2);
				else
					op2_value = pre_commit.get(op2);
				
				int result;
				
				switch(operator)
				{
				case "+":
					result = op1_value + op2_value;
					break;
				case "-":
					result = op1_value - op2_value;
					break;
				case "*":
					result = op1_value * op2_value;
					break;
				case "/":
					result = op1_value / op2_value;
					break;
				default:
					throw new Exception("Undefined operator!");
				}
				
				pre_write.put(op.getVarId(), result);
			}
		}
	}
	
	public boolean addOperation(Operation op){
		
		op.setTransId(uid);		
		return operations.add(op);
		
	}
	
	public int getTransId()
	{
		return uid;
	}
	
	public List<Operation> getAllOperations()
	{
		return operations;
	}
	
	public List<Operation> getAllWrites()
	{
		List<Operation> wrts = new ArrayList<Operation>();
		
		for(Operation op: operations)
		{
			if(op.getType() == Constants.OP_WRITE)
				wrts.add(op);
		}
		
		return wrts;
	}
	
	private static boolean isNumeric(String str)
	{
	  return str.matches("\\d+");
	}
	
	public Boolean commit()
	{
		try{
			
			for(String key: pre_commit.keySet())
			{				
				int value = pre_commit.get(key);
				
				DataProcessor.update_key(key, value);
				
			}
		}catch(Exception e)
		{
			System.out.println(e);
			
			return false;
		}
		
		return true;
	}
	
	public static int getSiteId(int tid)
	{
		return tid % Constants.TID_OFFSET;
	}
}
