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
 * Output stream that make control character italic
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class AnyCharacterOutputStream extends OutputStream
{
    /**
     * Constructor
     * 
     * @param  next  The next stream in the pipe
     */
    public AnyCharacterOutputStream(final OutputStream next)
    {
	this.next = next;
	
	try
        {
	    this.italic = "\033[3m?\033[23m".getBytes("UTF-8");
	}
	catch (final UnsupportedEncodingException err)
        {
	    System.err.println("Cannot possible work without UTF-8 support!");
	    throw new IOError(err);
	}
    }
    
    
    
    /**
     * The next stream in the pipe
     */
    private final OutputStream next;
    
    /**
     * Italic character byte array, edit index 4 for your character
     */
    private final byte[] italic;
    
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int b) throws IOException
    {
	if ((0 <= b) && (b < ' ') && (b != '\n') && (b != '\033'))
	{
	    italic[4] = (byte)(b + '@');
	    next.write(italic);
	}
	else
	    next.write(b);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException
    {
	next.flush();
    }
    
}

