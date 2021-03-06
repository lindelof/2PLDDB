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

public class Constants {
	
	// operation types
	public static final int OP_READ = 1;
	public static final int OP_WRITE = 2;
	public static final int OP_MATH = 3;
	
	// lock types
	public static final int LOCK_READ = 11;
	public static final int LOCK_WRITE = 12;
	public static final int LOCK_READ_AND_WRITE = 13;
	
	// transaction id
	public static int TID_OFFSET = 100;
	
	public static int NOT_FOUND = -1;
	public static final int UNINIT_STATUS = -1;
	
	public static int PORT_NUMBER = 52365;
	public static String CLIENT_PREFIX = "2pl_client_";	
	
	public static int DELAY_MILLISEC = 100;
	public static int DEADLOCK_CHECK_PERIOD = 3000;
	


}
