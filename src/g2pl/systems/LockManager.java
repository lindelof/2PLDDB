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
package g2pl.systems;

import g2pl.basics.Constants;
import g2pl.basics.Lock;
import g2pl.basics.Operation;
import g2pl.basics.Pair;
import g2pl.basics.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Date;

public class LockManager {
	
	Hashtable<String, List<Lock>> lockTable;
	Hashtable<String, List<Operation>> queueTable;
	
	
	public LockManager()
	{
		lockTable = new Hashtable<String, List<Lock>>();
		queueTable = new Hashtable<String, List<Operation>>();
	}
	
	public boolean request_lock(Operation op) throws Exception
	{
		
		Lock lock = op.getLock();
			
		if(is_compatible(lock))
		{				
			Lock oldLock = find_lock(op.getTransId(), op.getVarId());
			
			if(oldLock == null)
				insert_lock(lock);
			else
				oldLock.upgradeType(lock.getType());
			
			return true; //granted
			
		}else
		{
			queue_operation(op.getVarId(), op);
			
			return false; //waiting
		}	
	}
	
	public List<Integer> release_locks(Transaction trans) throws Exception
	{
		List<Integer> unblockedOtherSites = new ArrayList<Integer>();
		List<Lock> locks = find_locks(trans.getTransId());
		
		for(Lock lock: locks)
		{
			remove_lock(lock);
			Operation nextOp = dequeue_operation(lock.getVarId());

			if(nextOp != null)
			{
				Lock nextLock = nextOp.getLock();
				if(is_compatible(nextLock))
				{
					Lock oldLock = find_lock(nextLock.getTransId(), nextLock.getVarId());
					
					if(oldLock == null)
						insert_lock(nextOp.getLock());
					else
						oldLock.upgradeType(nextLock.getType());
					
					int siteId = Transaction.getSiteId(nextOp.getTransId());
					unblockedOtherSites.add(siteId);
				}
			}
		}
		
		print_timestamp();
		System.out.println("*** transaction " + trans.getTransId() + " releases all locks ****\n\n");
		
		return unblockedOtherSites;
	}
	
	public Pair<Integer, List<Integer>> checkDeadlocks()
	{
		WaitForGraph wfg = new WaitForGraph();
		
		for(String key: lockTable.keySet())
		{
			List<Lock> locks = lockTable.get(key);
			List<Operation> blocked = queueTable.get(key);
			
			if(blocked != null && blocked.size() != 0)
			{
				for(Lock lock: locks)
				{
					Integer trans1 = lock.getTransId();
					for(Operation op: blocked)
					{
						Integer trans2 = op.getTransId();
						
						if(trans1 != trans2)
						{
							wfg.addDependency(trans1, trans2);
						}
					}
				}
			}
		}
		
		List<Integer> edgeToRemove = wfg.containsCircle();
				
		if(edgeToRemove != null)
		{
			System.out.println("*********deadlock detected***********");
			
			System.out.println("From: "+ edgeToRemove.get(0) + ",  To: " + edgeToRemove.get(1));
			
			Integer transToAbort = edgeToRemove.get(1);
			
			System.out.println("Abort transaction "+transToAbort);
			
			List<Integer> unblocked = abortTransaction(transToAbort);
			
			return new Pair<Integer, List<Integer>>(transToAbort, unblocked);
		}else
			return null;
	}
	
	public void print()
	{
		print_timestamp();
		
		print_lock_table();
		
		print_queue_table();
		
		System.out.println("\n");
	}
	
	private void print_timestamp()
	{
		Date now = new Date();
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(now);
		System.out.println("[" + timestamp + "]");
	}
	
	private void print_lock_table()
	{
		System.out.println("----------Lock table------------");
		if(lockTable.isEmpty())
		{
			System.out.println("[]");
		}else
		{
			for(String key: lockTable.keySet())
			{
				for(Lock lock: lockTable.get(key))
				{
					System.out.print(lock.getString() +" ");
				}
				System.out.println();
			}
		}
	}
	
	private void print_queue_table()
	{
		System.out.println("----------Queue table------------");
		if(queueTable.isEmpty())
		{
			System.out.println("[]");
		}else{
			for(String key: queueTable.keySet())
			{
				for(Operation op: queueTable.get(key))
				{
					System.out.print(op.getString() +" ");
				}
				System.out.println();
			}
		}
	}
	
	
	private boolean insert_lock(Lock lock)
	{
		String varId = lock.getVarId();
		
		if(!lockTable.containsKey(varId))
			lockTable.put(varId, new ArrayList<Lock>());
		
		return lockTable.get(varId).add(lock);

	}
	
	private boolean remove_lock(Lock lock)
	{
		String varId = lock.getVarId();
		
		List<Lock> locks = lockTable.get(varId);
		
		for(Lock alock: locks)
		{
			if(alock.getTransId() == lock.getTransId())
			{
				lockTable.get(varId).remove(alock);
				
				if(lockTable.get(varId).size() == 0)
					lockTable.remove(varId);
				
				return true;
			}
		}
		
		return false;
		
	}
	
	private List<Integer> abortTransaction(int tid)
	{
		Transaction trans = new Transaction(tid);
		try
		{
			print();
			
			remove_blocked_operations(trans);
			print();
			List<Integer> unblocked = release_locks(trans);
			
			print();
			
			return unblocked;	
		}catch(Exception e)
		{
			System.out.println(e);
			
			return new ArrayList<Integer>();
		}
	}
	
	private void remove_blocked_operations(Transaction trans) 
	{
		int tid = trans.getTransId();
		
		for(String key: queueTable.keySet())
		{
			List<Integer> toBeRemoved = new ArrayList<Integer>();
			for(int i = queueTable.get(key).size() - 1; i >= 0; i--)
			{
				Operation op = queueTable.get(key).get(i);
				
				if(op.getTransId() == tid)
				{
					toBeRemoved.add(i);
				}
			}
			
			for(int i: toBeRemoved)
			{
				queueTable.get(key).remove(i);
			}
		}
		
	}

	private boolean queue_operation(String varId, Operation op)
	{		
		if(!queueTable.containsKey(varId))
			queueTable.put(varId, new ArrayList<Operation>());
		
		return queueTable.get(varId).add(op);
	}
	
	private Operation dequeue_operation(String varId) throws Exception
	{
		if(!queueTable.containsKey(varId))
			return null;
		
		List<Operation> ops = queueTable.get(varId);
		
		if(ops.size() == 0)
		{
			queueTable.remove(varId);
			return null;
		}else
		{
			// first check the next operation is compatible with current locks
			if(is_compatible(ops.get(0).getLock()))
			{
				Operation op = queueTable.get(varId).remove(0);
				
				if(queueTable.get(varId).size() == 0)
					queueTable.remove(varId);
				
				return op;
			}else
				return null;
		}
	}
	
	
	private boolean is_compatible(Lock lock) throws Exception
	{
		String varId = lock.getVarId();
		
		List<Lock> locks = lockTable.get(varId);
		
		if(locks == null)
			return true;
		
		if(lock.getType() == Constants.LOCK_READ)
		{
			for(int i = 0; i < locks.size(); i++)
			{
				if(locks.get(i).getTransId() != lock.getTransId() && locks.get(i).getType() >= Constants.LOCK_WRITE)
					return false;
			}
		}else if(lock.getType() == Constants.LOCK_WRITE)
		{
			for(int i = 0; i < locks.size(); i++)
			{
				if(locks.get(i).getTransId() != lock.getTransId())
					return false;
			}
		}else
		{
			throw new Exception("lock type incorrect!");
		}
		
		return true;
	}
	
	private Lock find_lock(int transId, String varId)
	{
		
		List<Lock> locks = lockTable.get(varId);
		
		if(locks == null)
			return null;
		
		for(int i = 0; i < locks.size(); i++)
		{
			Lock lock = locks.get(i);
			
			if(lock.getTransId() == transId)
				return lock;
		}
		
		return null;
	}
	
	private List<Lock> find_locks(int transId)
	{
		List<Lock> locks = new ArrayList<Lock>();
		
		Set<String> keys = lockTable.keySet();
		
		for(String key: keys)
		{
			List<Lock> locks_for_key = lockTable.get(key);
			
			for(int i = 0; i < locks_for_key.size(); i++)
			{
				if(locks_for_key.get(i).getTransId() == transId)
					locks.add(locks_for_key.get(i));
			}
		}
		
		return locks;
	}
}
