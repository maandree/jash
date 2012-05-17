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

import java.io.*;


/**
 * Output stream that wraps the output into lines
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class LineWrappingOutputStream extends OutputStream
{
    /**
     * Constructor
     * 
     * @param  next    The next stream in the pipe
     * @param  startX  The position of the first allowed coloumn on the first allowed row
     * @param  width   The width of the terminal
     */
    public LineWrappingOutputStream(final OutputStream next, final int startX, final int width)
    {
	this.next    = next;
	this.storedX = this.x = this.startX = startX;
	this.width   = width;
	this.storedY = this.y = 0;
    }
    
    
    
    /**
     * The next stream in the pipe
     */
    private final OutputStream next;
    
    /**
     * The position of the first allowed coloumn on the first allowed row
     */
    private final int startX;
    
    /**
     * The width of the terminal
     */
    private final int width;
    
    /**
     * The current column on the current row
     */
    private int x;
    
    /**
     * The current row relative to the first allowed row
     */
    private int y;
    
    /**
     * Stored previously current column on the current row
     */
    private int storedX;
    
    /**
     * Stored previously current row relative to the first allowed row
     */
    private int storedY;
    
    /**
     * Whether the last character was the first in an escape sequence
     */
    private boolean esc = false;
    
    /**
     * Whether the stream is in a CSI escape sequence
     */
    private boolean csi = false;
    
    /**
     * Numerical value in the current CSI escape sequence, but the negative of it, <code>1</code> if not singleton or fully numerical
     */
    private int csiNum = 0;
    
    /**
     * The current CSI escape sequence
     */
    private byte[] csiBuf = {(byte)'\033', (byte)'[', 0, 0, 0, 0, 0, 0};
    
    /**
     * Pointer for {@link #csiBuf}
     */
    private int csiPtr = 2;
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int b) throws IOException
    {
	if (this.csi)
	{
	    if (this.csiPtr == this.csiBuf.length)
	    {
		final byte[] nbuf = new byte[this.csiPtr + 8];
		System.arraycopy(this.csiBuf, 0, nbuf, 0, this.csiPtr);
		this.csiBuf = nbuf;
	    }
	    this.csiBuf[this.csiPtr++] = (byte)b;
	    
	    if (('0' <= b) && (b <= '9'))
	    {
		if (this.csiNum <= 0)
		    this.csiNum = (this.csiNum * 10) - (b & 15);
	    }
	    else if ((('a' <= b) && (b <= 'z')) || (('A' <= b) && (b <= 'Z')))
	    {
		this.csiNum = -this.csiNum;
		if (this.csiNum <= 0)
		    this.csiNum = 1;
		this.csi = false;
		final int xx = this.x;
		switch (b)
		{
		    case 'C': // RIGHT / END
			if (this.csiNum >= this.width)
			{
			    final int dy = this.csiNum / this.width;
			    this.csiNum %= this.width;
			    this.y += dy;
			    this.next.write(("\033[" + dy + "B").getBytes("UTF-8"));
			}
			this.x += this.csiNum;
			if (this.x >= this.width)
			{
			    this.x -= this.width;
			    this.y++;
			    this.next.write(("\033[B\033[" + this.width + "D\033[" + this.x + "C").getBytes("UTF-8"));
			}
			else
			    this.next.write(("\033[" + this.csiNum + "C").getBytes("UTF-8"));
			break;
			
		    case 'D': // LEFT / HOME
			if (this.csiNum >= this.width)
			{
			    final int dy = this.csiNum / this.width;
			    this.csiNum %= this.width;
			    this.y -= dy;
			    this.next.write(("\033[" + dy + "A").getBytes("UTF-8"));
			}
			this.x -= this.csiNum;
			if (this.x < 0)
			{
			    this.x += this.width;
			    this.y--;
			    this.next.write(("\033[A\033[" + (this.width - xx) + "D\033[" + this.x + "C").getBytes("UTF-8"));
			}
			else
			    this.next.write(("\033[" + this.csiNum + "D").getBytes("UTF-8"));
			break;
		    
		    case 'A': //UP
			this.next.write(this.csiBuf, 0, this.csiPtr);
			this.y -= this.csiNum;
			break;
			
		    case 'B': //DOWN
			this.next.write(this.csiBuf, 0, this.csiPtr);
			this.y += this.csiNum;
			break;
			
		    default:
			this.next.write(this.csiBuf, 0, this.csiPtr);
			break;
		}
	    }
	    else
		this.csiNum = 1;
	}
	else if (this.esc)
	{
	    this.esc = false;
	    if (b == '[')
	    {
		this.csi = true;
		this.csiNum = 0;
		this.csiPtr = 2;
	    }
	    else if (b == '7')
	    {
		this.storedX = this.x;
		this.storedY = this.y;
		this.next.write('\033');
		this.next.write(b);
	    }
	    else if (b == '8')
	    {
		this.x = this.storedX;
		this.y = this.storedY;
		this.next.write('\033');
		this.next.write(b);
	    }
	    else
	    {
		this.next.write('\033');
		this.write(b);
	    }
	}
	else if (b == '\033')
	{
	    this.esc = true;
	}
	else if (b == '\n')
	{
	    this.next.write(("\n\033[" + this.x + "D").getBytes("UTF-8"));
	    this.x = 0;
	    this.y++;
	}
	else
	{
	    this.next.write(b);
	    if (++this.x == this.width)
		this.write('\n');
	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException
    {
	this.next.flush();
    }
    
}

