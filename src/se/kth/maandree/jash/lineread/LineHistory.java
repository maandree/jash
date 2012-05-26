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
 * Line input history
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class LineHistory implements LineHistoryInterface
{
    /**
     * Constructor
     * 
     * @param  file  The history file
     * @param  soft  Soft history limit, in number of lines; after truncation
     * @param  hard  Hard history limit, in number of lines; when to truncate
     */
    public LineHistory(final String file, final int soft, final int hard) throws IOException
    {
	this.file = file;
	this.soft = soft;
	this.hard = hard;
	
	this.lines = new ArrayList<int[]>();
	//TODO populate
	this.ptr = this.lines.size();
    }
    
    
    
    /**
     * The history file
     */
    public final String file;
    
    /**
     * Soft history limit, in number of lines; after truncation
     */
    public int soft;
    
    /**
     * Hard history limit, in number of lines; when to truncate
     */
    public int hard;
    
    /**
     * Line history
     */
    public ArrayList<int[]> lines;
    
    /**
     * Line history pointer
     */
    public int ptr;
    
    
    
    /**
     * {@inheritDoc}
     */
    public synchronized int[] up(final boolean page, final int[] current)
    {
	try
	{
	    //TODO
	}
	catch (final Throwable err)
	{
	    //That's too bad
	}
	return null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public synchronized int[] down(final boolean page, final int[] current)
    {
	try
	{
	    //TODO
	}
	catch (final Throwable err)
	{
	    //That's too bad
	}
	return null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public synchronized void enter(final int[] current)
    {
	try
	{
	    //TODO
	}
	catch (final Throwable err)
	{
	    //That's too bad
	}
    }
    
    
    /**
     * {@inheritDoc}
     */
    public synchronized int[] search(final int[] beginning)
    {
	try
	{
	    //TODO
	}
	catch (final Throwable err)
	{
	    //That's too bad
	}
	return null;
    }
    
}

