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

import g2pl.comm.*;
import g2pl.basics.Constants;
import g2pl.basics.Operation;
import g2pl.basics.Pair;
import g2pl.basics.Transaction;

import java.util.*;
import java.rmi.server.*; 
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class CentralSite implements Comm_Server{
	
	private static LockManager lockmanager = new LockManager();;
	
	private static Hashtable<Integer, Comm_Site> sitesTable = new Hashtable<Integer, Comm_Site>();
	
	private static int siteCount = 1;
	
	
	public static void main (String[] argv) {
		
		try {
			 
			// build central site
			final CentralSite server = new CentralSite();	
			
			DataProcessor.create_database();
			
			Comm_Server stub = (Comm_Server) UnicastRemoteObject.exportObject(server, 0);
			
			Registry registry = LocateRegistry.createRegistry(Constants.PORT_NUMBER);
			
			registry.bind("2pl_server", stub);
			
			System.out.println("Server is ready.");
			
			// check wait for graph periodically 
			Timer checkwfg = new Timer();
			checkwfg.schedule(new TimerTask(){
				@Override
	            public void run() {
					server.check_deadlocks();
				}
				
			}, Constants.DEADLOCK_CHECK_PERIOD, Constants.DEADLOCK_CHECK_PERIOD);
			
		
		}catch (Exception e) {
			
			System.out.println("Server failed: " + e);
		}
	}
	
	
	public synchronized Boolean request_lock(Operation op)
	{
		try{
			Boolean result = lockmanager.request_lock(op);
			
			lockmanager.print();
			
			return result;
			
		}catch(Exception e)
		{
			System.out.println("error: " + e);
			
			return false;
		}
	}
	
	public synchronized void release_lock(Transaction trans)
	{
		try{
			List<Integer> unblockedSites = lockmanager.release_locks(trans);
			
			
			for(int unblockedSiteId: unblockedSites)
			{
				Comm_Site site_stub = get_otherSite_stub(unblockedSiteId);	
				
				site_stub.unblock();
			}
			
			lockmanager.print();
			
		}catch(Exception e)
		{
			System.out.println("error: " + e);
		}
	}
	
	public synchronized int obtain_next_siteId()
	{
		siteCount ++;
		return siteCount;
	}
	
	public synchronized void check_deadlocks()
	{
		
		Pair<Integer, List<Integer>> res = lockmanager.checkDeadlocks();
		
		
		if(res != null)
		{
			int transToAbort = res.first;
			int siteId = Transaction.getSiteId(transToAbort);
			
			// abort transaction
			try{
				Comm_Site site_stub = get_otherSite_stub(siteId);				
				site_stub.abort();
				
			}catch(Exception e)
			{
				System.out.println(e);
			}
			
			// unblock sites whose operations are granted
			List<Integer> unblocked = res.second;
			
			for(Integer site: unblocked)
			{
				try{
					Comm_Site site_stub = get_otherSite_stub(site);				
					site_stub.unblock();
					
				}catch(Exception e)
				{
					System.out.println(e);
				}
			}
			
			
		}
	}
	
	private Comm_Site get_otherSite_stub(int siteId) throws Exception
	{
		if(!sitesTable.containsKey(siteId))
		{
			Registry registry = LocateRegistry.getRegistry(Constants.PORT_NUMBER);	
			Comm_Site site_stub = (Comm_Site) registry.lookup(Constants.CLIENT_PREFIX + siteId);
			sitesTable.put(siteId, site_stub);
			return site_stub;
		}else
			return sitesTable.get(siteId);
	}

}
