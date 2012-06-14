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
 * A program execution chain
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Chain
{
    /**
     * A normal appending execution independent of exit value
     */
    public static final int CONCATINATE = 0;
    
    /**
     * Pipe line output to input of new execute
     */
    public static final int PIPE = 0;
    
    /**
     * A normal appending execution if exit value is 0
     */
    public static final int ON_SUCCESS = 0;
    
    /**
     * A normal appending execution if exit value is not 0
     */
    public static final int ON_FAILURE = 0;
    
    
    
    /**
     * Constructor
     */
    public Chain(final Execute next, final int type)
    {
	this.next = next;
	this.type = type;
    }
    
    
    
    /**
     * The next execution
     */
    public final Execute next;
    
    /**
     * The chain type
     */
    public final int type;
    
}

