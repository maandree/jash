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
package se.kth.maandree.jash.parse;


/**
 * Standard I/O channal redirection
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Redirect
{
    /**
     * From or to executed program's /dev/stdin
     */
    public static final int STDIN = 0;
    
    /**
     * From or to executed program's /dev/stdout
     */
    public static final int STDOUT = 1;
    
    /**
     * From or to executed program's /dev/stderr
     */
    public static final int STDERR = 2;
    
    /**
     * From both executed program's /dev/stdin and executed program's /dev/stdout
     */
    public static final int STDIN_OUT = 3;
    
    /**
     * To another file
     */
    public static final int FILE = -1;
    
    
    
    /**
     * Constructor
     * 
     * @param  from       The channel to redirect
     * @param  to         The channel to which to redirect
     * @param  file       The file to which to redirect, if any
     * @param  appending  Whether to do appending writting
     */
    public Redirect(final int from, final int to, final String file, final boolean appending)
    {
	this.from = from;
	this.to = to;
	this.file = file;
	this.appending = appending;
    }
    
    
    
    /**
     * The channel to redirect
     */
    public final int from;
    
    /**
     * The channel to which to redirect
     */
    public final int to;
    
    /**
     * The file to which to redirect, if any
     */
    public final String file;
    
    /**
     * Whether to do appending writting
     */
    public final boolean appending;
    
}

