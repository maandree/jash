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
package se.kth.maandree.jash.lineread;



/**
 * UTF-8 and UTF-16 encoder
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Encoder
{
    /**
     * Non-constructor
     */
    private Encoder()
    {
	assert false : "You man not create instnaces of this class [Encoder].";
    }
    
    
    
    /**
     * Small buffer array for {@link #encode(int)}
     */
    private static final byte[] encodeBuf = new byte[8];
    
    
    
    /**
     * Converts one character from UTF-32 to UTF-8
     * 
     * @param   character  UTF-32 character
     * @return             UTF-8 character
     */
    public static byte[] encode(final int character)
    {
	synchronized (encodeBuf)
	{
	    if (character < 0x80)
		return new byte[] { (byte)character };
	    
	    int n = 0, c = character;
	    
	    while (c != 0)
	    {
		if (n > 0)
		    encodeBuf[n - 1] |= 0x80;
		encodeBuf[n++] = (byte)(c & 0x3F);
		c >>>= 6;
	    }
	    
	    int cm = 0xFF ^ ((1 << (7 - n)) - 1);
	    
	    if ((cm & encodeBuf[n - 1]) == 0)
		n--;
	    else
	    {
		encodeBuf[n - 1] |= 0x80;
		cm |= cm >> 1;
	    }
	    
	    encodeBuf[n++] |= (byte)(cm << 1);
	    
	    final byte[] rc = new byte[n--];
	    for (int i = 0; i <= n; i++)
		rc[n - i] = encodeBuf[i];
	    return rc;
	}
    }
    
    
    /**
     * Converts a character string from UTF-32 to UTF-16
     * 
     * @param   character  UTF-32 <code>int[]</code> character string
     * @return             UTF-16 {@link String} character string
     */
    public static String encode(final int[] string)
    {
	return encode(string, 0, string.length);
    }
    
    
    /**
     * Converts a character string from UTF-32 to UTF-16
     * 
     * @param   character  UTF-32 <code>int[]</code> character string
     * @param   off        The offset in the array
     * @param   len        The number of characers
     * @return             UTF-16 {@link String} character string
     */
    public static String encode(final int[] string, final int off, final int len)
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

