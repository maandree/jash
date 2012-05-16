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
import java.util.HashMap;
import java.util.ArrayDeque;


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
     * Data needed for reading input
     * 
     * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
     */
    final class ReadData
    {
	/**
	 * The initial size of the buffers
	 */
	public static final int BUFSIZE = 64;
	
	
	
	//Has default constructor
	
	
	
	/**
	 * The character before the cursor
	 */
	public int[] bp = new int[BUFSIZE];
	
	/**
	 * The characters after the cursor
	 */
	public int[] ap = new int[BUFSIZE];
	
	/**
	 * The number of characters before the cursor
	 */
	public int before = 0;
	
	/**
	 * The number of characters after the cursor
	 */
	public int after = 0;
	
	/**
	 * A byte polled from the input by mistake, <code>-1</code> if none
	 */
	public int stored = -1;
	
	/**
	 * The text entered by the user
	 */
	public String returnValue = null;
	
	/**
	 * <p>Do what ever you what ever, just do mess up some other state's data without permission.</p>
	 * <p>
	 *   Raw {@link Object}s are recommened for unshared data
	 * </p>
	 */
	public final HashMap<Object, Object> privateUse = new HashMap<Object, Object>();
	
	/**
	 * Stack with states in use, the last on is the currently active one
	 */
	@requires("java-runtime>=6")
	public final ArrayDeque<State> stateStack = new ArrayDeque<State>(); //Oooh how I wish this was multi-popable...
    }
    
    
    /**
     * Reading state interface
     * 
     * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
     */
    interface State
    {
	/**
	 * Reads one character
	 * 
	 * @param   c         The character
	 * @param   readData  Data needed for reading input, update the values while you read
	 * @return            Whether the reading is complete, e.g. when the user presses Enter
	 * 
	 * @throws  IOException  Writing problem?
	 */
	public boolean read(final int c, final ReadData readData) throws IOException;
    }
    
    
    /**
     * Base reading state, it handles all single character input sequences
     * 
     * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
     */
    public class BaseState implements State
    {
	//Has default constructor
	
	/**
	 * {@inheritDoc}
	 */
	public boolean read(final int c, final ReadData readData) throws IOException
	{
	    int[] tmp;
	    
	    switch (c)
	    {
		case '\n':
		    System.out.write(c);
		    System.out.flush();
		    
		    tmp = new int[readData.before + readData.after];
		    System.arraycopy(readData.bp, 0, tmp, 0, readData.before);
		    for (int i = readData.after - 1, j = readData.before; i >= 0; i--, j++)
			tmp[j] = readData.ap[i]; 
			
		    readData.returnValue = decode(tmp, 0, readData.before + readData.after);
		    return true;
		    
		case '\b':
		case 127:
		    if (readData.before > 0)
		    {
			readData.before--;
			System.out.print("\033[D\0337");
			for (int i = readData.after - 1; i >= 0; i--)
			    System.out.write(decode(readData.ap[i]));
			System.out.print(" \0338");
			System.out.flush();
		    }
		    break;
		    
		case '\033':
		    readData.stateStack.offerLast(new ESCState());
		    break;
		    
		default:
		    if (c == 0)
		    {
			//Ignore this (sometimes) bogus input
		    }
		    else if (c == 'D' - '@')
		    {
			System.out.println(); //exit
			readData.returnValue = null;
			return true;
		    }
		    else
		    {
			if (readData.before == readData.bp.length)
			{
			    tmp = new int[readData.bp.length << 1];
			    System.arraycopy(readData.bp, 0, tmp, 0, readData.bp.length);
			    readData.bp = tmp;
			}
			readData.bp[readData.before++] = c;
			System.out.write(decode(c));
			if (readData.after > 0)  //OVERRIDE MODE
			    readData.after--;
			//System.out.print("\0337");  //INSERT MODE
			//for (int i = readData.after - 1; i >= 0; i--)
			//    System.out.write(decode(readData.ap[i]));
			//System.out.print("\0338");
			System.out.flush();
		    }
		    break;
	    }
	    
	    return false;
	}
    }
    
    
    /**
     * Escape sequence reading state
     * 
     * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
     */
    public class ESCState implements State
    {
	//Has default constructor
	
	
	
	/**
	 * The last parsed character in this state
	 */
	private int last = -1;
	
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean read(final int c, final ReadData readData) throws IOException
	{
	    if (last == '[')
		switch (c)
		{
		    case 'A':  //UP
			//TODO history
			break;
			
		    case 'B':  //DOWN
			//TODO history
			break;
			
		    case 'C':  //RIGHT
			if (readData.after > 0)
			{
			    System.out.print("\033[C");
			    System.out.flush();
			    readData.bp[readData.before++] = readData.ap[--readData.after];
			}
			break;
			
		    case 'D':  //LEFT
			if (readData.before > 0)
			{
			    System.out.print("\033[D");
			    System.out.flush();
			    if (readData.after == readData.ap.length)
			    {
				int[] tmp = new int[readData.after << 1];
				System.arraycopy(readData.ap, 0, tmp, 0, readData.after);
				readData.ap = tmp;
			    }
			    readData.ap[readData.after++] = readData.bp[--readData.before];
			}
			break;
			
		    default:
			readData.stored = c;
			readData.stateStack.pollLast();
			break;
		}
	    else
		switch (c)
		{
		    case '[':
			break;
			
		    default:
			readData.stored = c;
			readData.stateStack.pollLast();
			break;
		}
	    
	    this.last = c;
	    return false;
	}
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    public String read(final int x, final int width) throws IOException
    {
	final ReadData readData = new ReadData();
	readData.stateStack.offerLast(new BaseState());
	
	final PrintStream stdout = System.out;
	System.setOut(new PrintStream(new OutputStream()
	        {
		    public void write(final int b) throws IOException
		    {
			if ((0 <= b) && (b < ' ') && (b != '\n') && (b != '\033'))
			{
			    stdout.print("\033[3m");
			    stdout.write(b + '@');
			    stdout.print("\033[23m");
			}
			else
			    stdout.write(b);
		    }
		    
		    public void flush() throws IOException
		    {
			stdout.flush();
		    }
	        }
		));
	
	try
	{
	    for (int c;;)
	    {
		if (readData.stored == -1)
		{
		    c = System.in.read();
		    if (c == -1)
			return null;
		}
		else
		{
		    c = readData.stored;
		    readData.stored = -1;
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
				readData.stored = t;
				break;
			    }
			    c <<= 6;
			    c |= t & 0x7F;
			}
		    }
	    
		if (readData.stateStack.peekLast().read(c, readData))
		    return readData.returnValue;
	    }
	}
	finally
	{
	    System.setOut(stdout);
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

