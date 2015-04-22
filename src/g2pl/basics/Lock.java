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

import g2pl.systems.TransactionManager;

public class Lock {

	private String varId;
	private int type;
	private int transId;
	
	public Lock(int tp, String vId, int tid)
	{
		type = tp;
		varId = vId;
		transId = tid;
	}
	
	public String getVarId()
	{
		return varId;
	}
	
	public int getType()
	{
		return type;
	}
	
	public int getTransId()
	{
		return transId;
	}
	
	public void upgradeType(int incomingType)
	{
		if(type == Constants.LOCK_READ && incomingType == Constants.LOCK_WRITE )
			type = Constants.LOCK_READ_AND_WRITE;
		
		else if(type == Constants.LOCK_WRITE && incomingType == Constants.LOCK_READ)
			type = Constants.LOCK_READ_AND_WRITE;
	}
	
	public String getString()
	{
		String output = "[";
		
		output += varId + ", ";
		
		switch(type)
		{
		case Constants.LOCK_READ: 
			output += "read";
			break;
		case Constants.LOCK_WRITE:
			output += "write";
			break;
		case Constants.LOCK_READ_AND_WRITE:
			output += "read/write";
		}
		
		output += ", " + "tranaction " +transId + ", siteID " + Transaction.getSiteId(transId) +" ]";
		
		return output;
	}
}
