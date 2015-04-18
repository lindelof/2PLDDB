/*******************************************************************************
 * Copyright (c) 2015
 * Author: Yidan(Evan) Zheng
 *     
 *     This file is part of 2PLDDB.
 *
 *     2PLDDB is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package g2pl.basics;
import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class Operation implements Serializable {

	private int transId = Constants.UNINIT_STATUS;
	private int type;
	private String varId;
	
	// math computation related
	private String op1;
	private String op2;
	private String operator;
	
	// constructor for commit and abort
	public Operation(int tid, int tp)
	{
		transId = tid;
		type = tp;
	}
	
	// constructor for read and write
	public Operation(int tid, int tp, String id)
	{
		transId = tid;
		type = tp;
		varId = id;
	}
	
	// constructor for math operation
	public Operation(int tid, int tp, String vid, String op1, String operator, String op2)
	{
		this.transId = tid;
		this.varId = vid;
		this.op1 = op1;
		this.operator = operator;
		this.op2 = op2;
		this.type = tp;
	}
	
	public int getTransId()
	{
		
		return transId;
	}
	
	public void setTransId(int id)
	{
		transId = id;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getVarId() throws Exception
	{		
		return varId;
	}
	
	public Lock getLock() throws Exception
	{
		
		int lockType;
		
		if(type == Constants.OP_READ)
			lockType = Constants.LOCK_READ;
		
		else if(type == Constants.OP_WRITE)
			lockType = Constants.LOCK_WRITE;
		
		else
			throw new Exception("This operation cannot create a lock!");
		
		Lock lock = new Lock(lockType, varId, transId);	
		
		return lock;
	}
	
	public String getOp1()
	{
		return op1;
	}
	
	public String getOp2()
	{
		return op2;
	}
	
	public String getOperator()
	{
		return operator;
	}
	
	public String getString()
	{
		String str = "";
		if(type == Constants.OP_READ)
			str += "[READ(" + varId + "), ";
		else if(type == Constants.OP_WRITE)
			str += "[WRITE(" + varId + "), ";
		else if(type == Constants.OP_MATH)
			str += "[MATH..., ";
		
		str += "tranaction " +transId + ", siteID " + Transaction.getSiteId(transId) +" ]";
		
		return str;
	}
}

