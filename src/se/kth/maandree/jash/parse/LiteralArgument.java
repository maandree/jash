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
 * A literal program execution argument
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class LiteralArgument implements Argument
{
    /**
     * The literal is exact, it does not contain NUL
     */
    public static final int EXACT = 0;
    
    /**
     * Read a variable and split NUL into separate arguments
     */
    public static final int VARIABLE_EXACT = 1;
    
    /**
     * Read a variable and split words into separate arguments
     */
    public static final int VARIABLE_WORDS = 2;
    
    /**
     * Read a variable and split lines into separate arguments
     */
    public static final int VARIABLE_LINES = 3;
    
    /**
     * Read a file and parse it
     */
    public static final int PARSE_FILE = 4;
    
    
    
    /**
     * Constructor
     * 
     * @param  literal  The literal
     * @param  type     How to parse
     */
    public LiteralArgument(final String literal, final int type)
    {
	this.literal = literal;
	this.type = type;
    }
    
    
    
    /**
     * The literal
     */
    public final String literal;
    
    /**
     * How to parse
     */
    public final int type;
    
}

