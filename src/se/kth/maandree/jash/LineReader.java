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
     * Small buffer array for {@link #decode(int)}
     */
    private static final byte[] decodeBuf = new byte[8];
    
    
    
    /**
     * {@inheritDoc}
     */
    public String read(final int x, final int width) throws IOException
    {
	final int BUFSIZE = 64;
	
	int[] bp = new int[BUFSIZE];
	int[] ap = new int[BUFSIZE];
	int[] tmp;
	
	int before = 0;
	int after = 0;
	int stored = -1;
	
	for (int c;;)
	{
	    if (stored == -1)
	    {
		c = System.in.read();
		if (c == -1)
		    return null;
	    }
	    else
	    {
		c = stored;
		stored = -1;
	    }
	    
	    if ((c & 0x80) == 0x80)
		if ((c & 0xC0) == 0x80)
		    System.err.println("WTF, this is not UTF-8!");
		else
		{
		    int n = 0;
		    int t = c;
		    while ((t & 0x80) == 0x80)
		    {
			t <<= 1;
			n++;
		    }
		    c = (t & 0xFF) >> n;
		    for (int i = 1; i < n; i++)
		    {
			t = System.in.read();
			if ((t & 0xC0) != 0x80)
			{
			    System.err.println("WTF, this is not UTF-8!");
			    stored = t;
			    break;
			}
			c <<= 6;
			c |= t & 0x7F;
		    }
		}
	    
	    switch (c)
	    {
		case '\n':
		    System.out.write(c);
		    System.out.flush();
			
		    tmp = new int[before + after];
		    System.arraycopy(bp, 0, tmp, 0, before);
		    for (int i = after - 1, j = before; i >= 0; i--, j++)
			tmp[j] = ap[i]; 
			
		    return decode(tmp, 0, before + after);
		    
		case '\b':
		case 127:
		    if (before > 0)
		    {
			before--;
			System.out.print("\033[D\0337");
			for (int i = after - 1; i >= 0; i--)
			    System.out.write(decode(ap[i]));
			System.out.print(" \0338");
			System.out.flush();
		    }
		    break;
		    
		case '\033':
		    break;
		    
		default:
		    if (c < ' ')
		    {
			if (c == 'D' - '@')
			{
			    System.out.println("exit");
			    return null;
			}
		    }
		    else
		    {
			if (before == bp.length)
			{
			    tmp = new int[bp.length << 1];
			    System.arraycopy(bp, 0, tmp, 0, bp.length);
			    bp = tmp;
			}
			bp[before++] = c;
			System.out.write(decode(c));
			if (after > 0)  //OVERRIDE MODE
			    after--;
			//System.out.print("\0337");  //INSERT MODE
			//for (int i = after - 1; i >= 0; i--)
			//    System.out.write(decode(ap[i]));
			//System.out.print("\0338");
			System.out.flush();
		    }
		    break;
	    }
	}
    }
    
    
    
    /**
     * Converts one character from UTF-32 to UTF-8
     * 
     * @param   character  UTF-32 character
     * @return             UTF-8 character
     */
    public static byte[] decode(final int character)
    {
	if (character < 0x80)
	    return new byte[] { (byte)character };
	
	int n = 0, c = character;
	
	while (c != 0)
	{
	    if (n > 0)
		decodeBuf[n - 1] |= 0x80;
	    decodeBuf[n++] = (byte)(c & 0x3F);
	    c >>>= 6;
	}
	
	int cm = 0xFF ^ ((1 << (7 - n)) - 1);
	
	if ((cm & decodeBuf[n - 1]) == 0)
	    n--;
	else
	{
	    decodeBuf[n - 1] |= 0x80;
	    cm |= cm >> 1;
	}
	
	decodeBuf[n++] |= (byte)(cm << 1);
	
	final byte[] rc = new byte[n--];
	for (int i = 0; i <= n; i++)
	    rc[n - i] = decodeBuf[i];
	return rc;
    }
    
    /**
     * Converts a character string from UTF-32 to UTF-16
     * 
     * @param   character  UTF-32 <code>int[]</code> character string
     * @return             UTF-16 {@link String} character string
     */
    public static String decode(final int[] string)
    {
	return decode(string, 0, string.length);
    }
    
    /**
     * Converts a character string from UTF-32 to UTF-16
     * 
     * @param   character  UTF-32 <code>int[]</code> character string
     * @param   off        The offset in the array
     * @param   len        The number of characers
     * @return             UTF-16 {@link String} character string
     */
    public static String decode(final int[] string, final int off, final int len)
    {
	final char[] chars = new char[len << 1];
	int ptr = 0;
	
	for (int i = off, n = off + len, c; i < n; i++)
	    if ((c = string[i]) < 0)
	    {
		System.err.println("WTF, this is not Unicode!");
		chars[ptr++] = 0xFFFD;
	    }
	    else if (c <= 0xFFFF)
		chars[ptr++] = (char)c;
	    else if (c <= 0x10FFFF)
	    {
		chars[ptr++] = (char)(((c >> 10) & 1023) | 0xD800);
		chars[ptr++] = (char)(( c        & 1023) | 0xDC00);
	    }
	    else
	    {
		System.err.println("WTF, this plane those not exist!");
		chars[ptr++] = 0xFFFD;
	    }
	
	return new String(chars, 0, ptr);
    }
    
}

