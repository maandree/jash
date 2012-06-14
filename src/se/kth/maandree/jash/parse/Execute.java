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
 * A program execution link
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Execute
{
    /**
     * Constructor
     * 
     * @param  arguments  The arguments to execute
     * @param  redirects  Channel redirections
     * @param  blocking   Whether to wait for program to exit
     * @param  chaining   Execution chaining
     */
    public Execute(final Argument[] arguments, final Redirect[] redirects, final boolean blocking, final Chain chaining)
    {
	this.arguments = arguments;
	this.redirects = redirects;
	this.blocking = blocking;
	this.chaining = chaining;
    }
    
    
    
    /**
     * The arguments to execute
     */
    public final Argument[] arguments;
    
    /**
     * Channel redirections
     */
    public final Redirect[] redirects;
    
    /**
     * Whether to wait for program to exit
     */
    public final boolean blocking;
    
    /**
     * Execution chaining
     */
    public final Chain chaining;
    
}

