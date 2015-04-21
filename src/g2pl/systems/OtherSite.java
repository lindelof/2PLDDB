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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;

import g2pl.basics.*;
import g2pl.comm.*;

public class OtherSite implements Runnable, Comm_Site{
	
	TransactionManager tm;
	int clientId;	
	Comm_Server server_stub;
	
	public Boolean blocked = false;
	public Boolean abort = false;
	
	public static void main(String[] argv)
	{
		try{
			
			// setup server communication
			Registry registry = LocateRegistry.getRegistry(Constants.PORT_NUMBER);
			Comm_Server temp_stub = (Comm_Server) registry.lookup("2pl_server");
			
			int siteId = temp_stub.obtain_next_siteId();
			
			String historyPath = argv[0];
			
			(new OtherSite(siteId, historyPath)).run();
			
		}catch(ConnectException e)
		{
			System.out.println("Fail to initaite othersite since central site is down.");
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	public OtherSite(int id, String historyPath) throws Exception
	{
		clientId = id;
		tm = new TransactionManager(clientId);
		tm.loadTransactions(historyPath);
		
		// setup server communication
		Registry registry = LocateRegistry.getRegistry(Constants.PORT_NUMBER);	
		server_stub = (Comm_Server) registry.lookup("2pl_server");
		
		// setup its own communication
		Comm_Site client_stub = (Comm_Site) UnicastRemoteObject.exportObject(this, 0);
		registry.bind(Constants.CLIENT_PREFIX + id, client_stub);
		System.out.println("Client " + clientId + " is ready.");
	}

	@Override
	public void run() {
		
		while(true)
		{
			try{
				synchronized(this)
				{
					Transaction trans = tm.popTransactions();
					
					if(trans == null)
					{	
						blocked();
						System.out.println("*****waiting for next transaction******");
					}
					
					else
					{
						String tag = "[T" + trans.getTransId() + "]";
						System.out.println(tag +":\n\tStarting transaction " + trans.getTransId());
						
						for(Operation op: trans.getAllOperations())
						{
							switch(op.getType())
							{
								case Constants.OP_READ:
								{
									Boolean result = server_stub.request_lock(op);
									
									if(!result)
										//wait for lock
										blocked();
									
									if(abort == true)
										break;
									
									trans.executeOperation(op);
									break;
								}
								case Constants.OP_WRITE:
								{
									Boolean result = server_stub.request_lock(op);
									if(!result)
										//wait for lock
										blocked();
									
									if(abort == true)
										break;
									
									trans.executeOperation(op);
									break;
								}
								case Constants.OP_MATH:
								{
									trans.executeOperation(op);
									break;
								}
							}
							
							if(abort == true)
								break;
						}
						
						if(abort == true)
						{
							System.out.println("\tTransaction " + trans.getTransId() + " is aborted");
							abort = false;
						}else
						{
							// finally, commit the transaction
							// assume synchronized data processor (which is not the focus of the project)
							
							trans.commit();
							server_stub.release_lock(trans);
							System.out.println("\tTransaction " + trans.getTransId() + " completes");
						}

					}
				}
			}catch(Exception e){
				System.out.println("run() error: " + e);
			}
		}

	}
		
	public synchronized void blocked()
	{
		try{
			blocked = true;
			
			while(blocked)
			{
				System.out.print("   ");
				print_timestamp();
				System.out.print(" Client " + clientId + " blocked, ");
				System.out.println("wait for events...");
				wait();
			}
		}catch(InterruptedException e){}
	}
	
	public synchronized void unblock()
	{
		blocked = false;
		
		notifyAll();
	}
	
	public synchronized void abort()
	{
		abort = true;
		unblock();
	}
	
	private void print_timestamp()
	{
		Date now = new Date();
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(now);
		System.out.print("[" + timestamp + "]");
	}
	
}
