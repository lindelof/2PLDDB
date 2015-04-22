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
package g2pl.comm;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Comm_Site extends Remote{
	
	public void unblock() throws RemoteException;

	public void abort() throws RemoteException;
}
