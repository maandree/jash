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
 * A program execution argument by execution
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class ExecuteArgument implements Argument
{
    /**
     * Take the executed program's /dev/stdin file path as argument
     */
    public static final int STDIN_FILE = 0;
    
    /**
     * Take the executed program's /dev/stdout file path as argument
     */
    public static final int STDOUT_FILE = 1;
    
    /**
     * Take the executed program's stdout output as argument and split NUL into separate arguments
     */
    public static final int QUOTE_STDOUT_EXACT = 2;
    
    /**
     * Take the executed program's stdout output as argument and split words into separate arguments
     */
    public static final int QUOTE_STDOUT_WORDS = 3;
    
    /**
     * Take the executed program's stdout output as argument and split lines into separate arguments
     */
    public static final int QUOTE_STDOUT_LINES = 4;
    
    /**
     * Parse the executed program's stdout output
     */
    public static final int PARSE_STDOUT = 5;
    
    //The following are possible, but not by the default parser
    
    /**
     * Take the executed program's stderr output as argument and split NUL into separate arguments
     */
    public static final int QUOTE_STDERR_EXACT = 6;
    
    /**
     * Take the executed program's stderr output as argument and split words into separate arguments
     */
    public static final int QUOTE_STDERR_WORDS = 7;
    
    /**
     * Take the executed program's stderr output as argument and split lines into separate arguments
     */
    public static final int QUOTE_STDERR_LINES = 8;
    
    /**
     * Parse the executed program's stderr output
     */
    public static final int PARSE_STDERR = 9;
    
    
    
    /**
     * Constructor
     * 
     * @param  execution  The execution
     * @param  type       How to parse
     */
    public ExecuteArgument(final Execute execution, final int type)
    {
	this.execution = execution;
	this.type = type;
    }
    
    
    
    /**
     * The execution
     */
    public final Execute execution;
    
    /**
     * How to parse
     */
    public final int type;
    
}

