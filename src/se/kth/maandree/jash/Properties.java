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

import java.util.*;
import java.io.*;


/**
 * Property handling class
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Properties
{
    /**
     * The stdin file's file name
     */
    public static final String stdin;
    
    /**
     * The stdout file's file name
     */
    public static final String stdout;
    
    /**
     * The stderr file's file name
     */
    public static final String stderr;
    
    /**
     * Environment variables
     */
    public static final HashMap<String, String> env;
    
    /**
     * The current working directory
     */
    public static String dir = null;
    
    
    
    /**
     * Class initialiser
     */
    static
    {
	String _stdin  = null;
	String _stdout = null;
	String _stderr = null;
	
	try
	{
	    _stdin  = (new File("/dev/stdin" )).getCanonicalPath();
	    _stdout = (new File("/dev/stdout")).getCanonicalPath();
	    _stderr = (new File("/dev/stderr")).getCanonicalPath();
	}
	catch (final Throwable err)
        {
	    System.err.println("Cannot possibly work without knowning standard channels.");
	    System.exit(-1);
	}
	
	stdin  = _stdin;
	stdout = _stdout;
	stderr = _stderr;
	
	
	final Map<String, String> _env = System.getenv(); //unmodifiable
	env = new HashMap<String, String>();
	final Set<String> vars = _env.keySet();
	for (final String var : vars)
	    env.put(var, _env.get(var));
    }
    
    
    
    /**
     * Rules on how to parse line breaks
     */
    public static enum LineRule
    {
	/**
	 * Ignore all line breaks
	 */
	IGNORE,
	
	/**
	 * Stop parsing at first line break
	 */
	BREAK,
	
	/**
	 * Parse line breaks as any other character
	 */
	READ,
	
	/**
	 * Convert line breaks to blank spaces
	 */
	SPACE,
    }
    
    /**
     * Properties fetchable by {@link Properties#getProperty(Property)}
     */
    public static enum Property
    {
	/**
	 * The user's login name
	 */
	USER,
	
	/**
	 * The user's ID
	 */
	UID,
	
	/**
	 * The machines name
	 */
	HOST,
	
	/**
	 * The TTY file
	 */
	TTY,
	
	/**
	 * The user's home directory
	 */
	HOME,
	
	/**
	 * The current working directory
	 */
	DIR,
	
	/**
	 * The current working directory, but with ~ instead of the home directory
	 */
	PDIR,
	
	/**
	 * The number of columns in the terminal
	 */
	COLS,
	
	/**
	 * The number of rows in the terminal
	 */
	ROWS,
	
	/**
	 * The current boolean settings for the TTY
	 */
	STTY,
	
	/**
	 * The current time
	 */
	TIME,
    }
    
    
    
    /**
     * Gets a property
     * 
     * @param   property  The property
     * @return            The property value
     */
    @requires("coreutils")
    public static String getProperty(final Property property)
    {
	switch (property)
	{
	    case USER:  return execSystemProperty(LineRule.BREAK, "whoami");
	    case UID:   return execSystemProperty(LineRule.BREAK, "id", "-u");
	    case HOST:  return execSystemProperty(LineRule.BREAK, "uname", "-n");
	    case TTY:   return stdout.substring(stdout.lastIndexOf('/') + 1);
	    case HOME:  return System.getProperty("user.home");
	    case DIR:   return dir;
	    case COLS:
	    {
		String[] data = (" " + execSystemProperty(LineRule.IGNORE, "stty", "-a")).split(";");
		for (final String p : data)
		    if (p.startsWith(" columns "))
			return p.substring(" columns ".length());
		return null;
	    }
	    case ROWS:
	    {
		String[] data = execSystemProperty(LineRule.IGNORE, "stty", "-a").split(";");
		for (final String p : data)
		    if (p.startsWith(" rows "))
			return p.substring(" rows ".length());
		return null;
	    }
	    case PDIR:
	    {
		final String home = getProperty(Property.HOME);
		final String dir  = getProperty(Property.DIR);
		if (dir.startsWith(home))
		    return "~" + dir.substring(home.length());
		return dir;
	    }
	    case STTY:
	    {
		String[] data = execSystemProperty(LineRule.SPACE, "stty", "-a").split(";");
		String rc = data[data.length - 1];
		while (rc.startsWith(" "))  rc = rc.substring(1);
		while (rc  .endsWith(" "))  rc = rc.substring(0, rc.length() - 1);
		while (rc.contains("  "))   rc = rc.replace("  ", " ");
		return rc;
	    }
	    case TIME:  return execSystemProperty(LineRule.BREAK, "date");
	    
	    default:
		assert false : "No such property!";
		return null;
	}
    }
    
    /**
     * Gets or sets system properties by invoking another program
     * 
     * @param  lineRule  What to do with line breaks
     * @param  cmd       The command to run
     */
    @requires("java-runtime>=7")
    public static String execSystemProperty(final LineRule lineRule, final String... cmd)
    {
	try
	{
	    byte[] buf = new byte[64];
	    int ptr = 0;
	    
	    final ProcessBuilder procBuilder = new ProcessBuilder(cmd);
	    procBuilder.redirectInput(ProcessBuilder.Redirect.from(new File(stdout)));
	    final Process process = procBuilder.start();
	    final InputStream stream = process.getInputStream();
	    
	    for (int d; (d = stream.read()) != -1; )
	    {
		if (d == '\n')
		    if      (lineRule == LineRule.BREAK)   break;
		    else if (lineRule == LineRule.IGNORE)  continue;
		    else if (lineRule == LineRule.SPACE)   d = ' ';
		
		if (ptr == buf.length)
		{
		    final byte[] nbuf = new byte[ptr + 64];
		    System.arraycopy(buf, 0, nbuf, 0, ptr);
		    buf = nbuf;
		}
		buf[ptr++] = (byte)d;
	    }
	    
	    process.waitFor();
	    if (process.exitValue() != 0)
	    {
		System.err.println("jash: error: " + cmd[0] + " exited with error code " + process.exitValue());
		return null;
	    }
	    
	    return new String(buf, 0, ptr, "UTF-8");
	}
	catch (final Throwable err)
	{
	    System.err.println("jash: error: failed to execute a system program: " + err.toString());
	    return null;
	}
    }
    
}

