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
import se.kth.maandree.jash.*;

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
     * Line history interface
     */
    private LineHistoryInterface history = null;
    
    
    
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
	
	
	
	/**
	 * Returns an integer array from read data
	 * 
	 * @return  The integer array
	 */
	public int[] get()
	{
	    final int[] rc = new int[this.before + this.after];
	    System.arraycopy(this.bp, 0, rc, 0, this.before);
	    for (int i = this.after - 1, j = this.before; i >= 0; i--, j++)
		rc[j] = this.ap[i];
	    return rc;
	}
	
	/**
	 * Sets read data from an integer array
	 * 
	 * @param  data  The integer array
	 */
	public void set(final int[] data)
	{
	    if (data == null)
		return;
	    
	    if (this.bp.length < data.length)
		this.bp = new int[(data.length | (BUFSIZE - 1)) + 1];
	    
	    System.arraycopy(data, 0, bp, 0, this.before = data.length);
	    this.after = 0;
	}
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
		    
		    if (LineReader.this.history != null)
			readData.set(LineReader.this.history.enter(readData.get()));
		    
		    readData.returnValue = Encoder.encode(tmp, 0, readData.before + readData.after);
		    return true;
		    
		case '\b':
		case 127:
		    if (readData.before > 0)
		    {
			readData.before--;
			System.out.print("\033[D\0337");
			for (int i = readData.after - 1; i >= 0; i--)
			    System.out.write(Encoder.encode(readData.ap[i]));
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
			System.out.println((char)c);
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
			System.out.write(Encoder.encode(c));
			if (readData.privateUse.containsKey("insert")) //insert
			{
			    System.out.print("\0337");
			    for (int i = readData.after - 1; i >= 0; i--)
			        System.out.write(Encoder.encode(readData.ap[i]));
			    System.out.print("\0338");
			}
			else //override
			    if (readData.after > 0)
				readData.after--;
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
	    if (last == 'O')
	    {
		switch (c)
		{
		    case 'H': //HOME
			if (readData.before > 0)
			{
			    System.out.print("\033[" + readData.before + "D");
			    System.out.flush();
			    while (readData.before != 0)
			    {
				if (readData.after == readData.ap.length)
				{
				    int[] tmp = new int[readData.after << 1];
				    System.arraycopy(readData.ap, 0, tmp, 0, readData.after);
				    readData.ap = tmp;
				}
				readData.ap[readData.after++] = readData.bp[--readData.before];
			    }
			}
			break;
			
		    case 'F': //END
			if (readData.after > 0)
			{
			    System.out.print("\033[" + readData.after + "C");
			    System.out.flush();
			    while (readData.after != 0)
			    {
				if (readData.before == readData.bp.length)
				{
				    int[] tmp = new int[readData.before << 1];
				    System.arraycopy(readData.bp, 0, tmp, 0, readData.before);
				    readData.bp = tmp;
				}
				readData.bp[readData.before++] = readData.ap[--readData.after];
			    }
			}
			break;
		}
		readData.stateStack.pollLast();
	    }
	    else if (('1' <= last) && (last <= '6'))
	    {
		if (c == '~')
		    switch (last)
		    {
			case '1':  //HOME
			    if (readData.before > 0)
			    {
				System.out.print("\033[" + readData.before + "D");
				System.out.flush();
				while (readData.before != 0)
				{
				    if (readData.after == readData.ap.length)
				    {
					int[] tmp = new int[readData.after << 1];
					System.arraycopy(readData.ap, 0, tmp, 0, readData.after);
					readData.ap = tmp;
				    }
				    readData.ap[readData.after++] = readData.bp[--readData.before];
				}
			    }
			    break;
			    
			case '2':  //INS
			    if (readData.privateUse.containsKey("insert"))
				readData.privateUse.remove("insert");
			    else
				readData.privateUse.put("insert", null);
			    break;
			    
			case '3':  //DEL
			    if (readData.after > 0)
			    {
				for (int i = --readData.after - 1; i >= 0; i--)
				    System.out.write(readData.ap[i]);
				System.out.print(" \033[" + (readData.after + 1) + "D");
				System.out.flush();
			    }
			    break;
			    
			case '4':  //END
			    if (readData.after > 0)
			    {
				System.out.print("\033[" + readData.after + "C");
				System.out.flush();
				while (readData.after != 0)
				{
				    if (readData.before == readData.bp.length)
				    {
					int[] tmp = new int[readData.before << 1];
					System.arraycopy(readData.bp, 0, tmp, 0, readData.before);
					readData.bp = tmp;
				    }
				    readData.bp[readData.before++] = readData.ap[--readData.after];
				}
			    }
			    break;
			    
		        case '5':  //PAGE UP
			    if (LineReader.this.history != null)
				readData.set(LineReader.this.history.up(true, readData.get()));
			    break;
			    
		        case '6':  //PAGE DOWN
			    if (LineReader.this.history != null)
				readData.set(LineReader.this.history.down(true, readData.get()));
			    break;
		    }
		
		readData.stateStack.pollLast();
	    }
	    else if (last == '[')
	    {
		switch (c)
		{
		    case 'A':  //UP
			if (LineReader.this.history != null)
			    readData.set(LineReader.this.history.up(false, readData.get()));
			break;
			
		    case 'B':  //DOWN
			if (LineReader.this.history != null)
			    readData.set(LineReader.this.history.down(false, readData.get()));
			break;
			
		    case 'C':  //RIGHT
			if (readData.after > 0)
			{
			    System.out.print("\033[C");
			    System.out.flush();
			    if (readData.before == readData.bp.length)
			    {
				int[] tmp = new int[readData.before << 1];
				System.arraycopy(readData.bp, 0, tmp, 0, readData.before);
				readData.bp = tmp;
			    }
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
			
		    case '1':  //HOME
		    case '2':  //INS
		    case '3':  //DEL
		    case '4':  //END
		    case '5':  //PAGE UP
		    case '6':  //PAGE DOWN
			this.last = c;
			return false; //no poping
		}
		readData.stateStack.pollLast(); //pop
	    }
	    else
		switch (c)
		{
		    case '[': //everything
		    case 'O': //HOME / END
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
	readData.privateUse.put("insert", null);
	
	final PrintStream stdout = System.out;
	System.setOut(new PrintStream(
			new AnyCharacterOutputStream(
			  new LineWrappingOutputStream(
			    stdout, x, width
		      ) ) ) );
	
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
     * {@inheritDoc}
     */
    public void setHistory(final LineHistoryInterface history)
    {
	this.history = history;
    }
    
}

