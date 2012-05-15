/**
 * jash - Just another shell
 * 
 * Copyright (C) 2012  Mattias Andrée (maandree@kth.se)
 * 
 * jash is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jash is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jash.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.kth.maandree.jash;

import java.io.*;


/**
 * Line input reader class
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class LineReader implements LineReaderInterface
{
    //Has default constructor
    
    
    
    /**
     * {@inheritDoc}
     */
    public String read(final int x, final int width) throws IOException
    {
	byte[] buf = new byte[64];
	int ptr = 0;
	int cl = 0;
	int ci = 0;
	
	for (int d;;)
	{
	    if ((d = System.in.read()) == 'D' - '@')
	    {
		System.out.println("exit");
		return null;
	    }
	    
	    if (ptr == buf.length)
	    {
		final byte[] nbuf = new byte[ptr << 1];
		System.arraycopy(buf, 0, nbuf, 0, ptr);
		buf = nbuf;
	    }
	    
	    System.out.write(d);
	    
	    if ((d & 0x80) == 0x00)
	    {
		System.out.flush();
		ci = 0;
		cl = 1;
	    }
	    else if ((d & 0xC0) == 0xC0)
	    {
		ci = 1;
		cl = 0;
		int t = d;
		while ((t & 0x80) == 0x80)
	        {
		    cl++;
		    t <<= 1;
		}
	    }
	    else
		if (++ci == cl)
		{
		    ci = cl = 0;
		    System.out.flush();
		}
	    
	    if (d == '\n')
		break;
	    buf[ptr++] = (byte)d;
	}
	
	return new String(buf, 0, ptr, "UTF-8");
    }
    
}

