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
 * This is the main class of the program
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
@requires({"coreutils", "java-runtime>=7"})
public class Jash
{
    public static final String stdin;
    public static final String stdout;
    public static final String stderr;
    public static final HashMap<String, String> env;
    public static String dir = null;
    
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
	    System.err.println("Cannot possibly work without known standard channels.");
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
     * The is the main entry point of the program
     * 
     * @param  args  Startup arguments
     */
    public static void main(final String... args)
    {
	System.out.print("\033[?1049h");
	final String stty = getProperty(STTY);
	execSystemProperty(BREAK, "stty -icanon -echo -isig -ixon -ixoff".split(" "));
	try
	{
	    dir = System.getProperty("user.dir");
	    final String user = getProperty(USER);
	    final String host = getProperty(HOST);
	    env.put("SHELL", "jash");
	    try
	    {
		env.put("SHLVL", Integer.toString(Integer.parseInt(env.get("SHLVL")) + 1));
	    }
	    catch (final Throwable err)
	    {
		env.put("SHLVL", "1");
	    }
	    env.put("USER", user);
	    env.put("HOST", host);
	    
	    for (;;)
	    {
		if (env.get("TERM").equals("xterm"))
		    System.out.print("\033[94m" + user + "\033[39m@" + 
				     "\033[34m" + host + "\033[39m." +  
				     "\033[36m" + getProperty(TTY) + "\033[39m: " + 
				     "\033[35m" + getProperty(PDIR) + "\033[39m " + 
				     "\033[90m(" + getProperty(TIME) + ")\033[39m\n" +
				     (getProperty(UID).equals("0")
				        ? "\033[91m# \033[39m"
				        : "\033[1;34m! \033[21;39m"));
		else
		    System.out.print("\033[1;34m" + user + "\033[21;39m@" + 
				     "\033[34m" + host + "\033[39m." +  
				     "\033[36m" + getProperty(TTY) + "\033[39m: " + 
				     "\033[35m" + getProperty(PDIR) + "\033[39m " + 
				     "\033[1;30m(" + getProperty(TIME) + ")\033[21;39m\n" +
				     (getProperty(UID).equals("0")
				        ? "\033[1;31m# \033[21;39m"
				        : "\033[1;34m! \033[21;39m"));
		
		byte[] buf = new byte[64];
		int ptr = 0;
		
		try
		{
		    for (int d; (d = System.in.read()) != '\n'; )
		    {
			if (d == 'D' - '@')
			    return;
			
			System.out.write(d);
			if ((d & 0xC0) != 0x80)
			    System.out.flush();
		    
			if (ptr == buf.length)
			{
			    final byte[] nbuf = new byte[ptr << 1];
			    System.arraycopy(buf, 0, nbuf, 0, ptr);
			    buf = nbuf;
			}
			buf[ptr++] = (byte)d;
		    }
		    System.out.println();
		    
		    final String command = new String(buf, 0, ptr, "UTF-8");
		    if (command.startsWith("?"))
			System.out.println(env.get(command.substring(1)));
		    else
			exec(command);
		}
		catch (final IOException err)
		{
		    throw new IOError(err);
		}
	    }
	}
	finally
	{
	    execSystemProperty(BREAK, ("stty " + stty).split(" "));
	    System.out.print("\033[?1049l");
	}
    }
    
    public static final int IGNORE = 1;
    public static final int BREAK = 2;
    public static final int READ = 3;
    public static final int SPACE = 4;
    
    public static final int USER = 1;
    public static final int UID  = 2;
    public static final int HOST = 3;
    public static final int TTY  = 4;
    public static final int HOME = 5;
    public static final int DIR  = 6;
    public static final int PDIR = 7;
    public static final int COLS = 8;
    public static final int ROWS = 9;
    public static final int STTY = 10;
    public static final int TIME = 11;
    
    
    public static String getProperty(final int property)
    {
	switch (property)
	{
	    case USER:  return execSystemProperty(BREAK, "whoami");
	    case UID:   return execSystemProperty(BREAK, "id", "-u");
	    case HOST:  return execSystemProperty(BREAK, "uname", "-n");
	    case TTY:   return stdout.substring(stdout.lastIndexOf('/') + 1);
	    case HOME:  return System.getProperty("user.home");
	    case DIR:   return dir;
	    case COLS:
	    {
		String[] data = (" " + execSystemProperty(IGNORE, "stty", "-a")).split(";");
		for (final String p : data)
		    if (p.startsWith(" columns "))
			return p.substring(" columns ".length());
		return null;
	    }
	    case ROWS:
	    {
		String[] data = execSystemProperty(IGNORE, "stty", "-a").split(";");
		for (final String p : data)
		    if (p.startsWith(" rows "))
			return p.substring(" rows ".length());
		return null;
	    }
	    case PDIR:
	    {
		final String home = getProperty(HOME);
		final String dir  = getProperty(DIR);
		if (dir.startsWith(home))
		    return "~" + dir.substring(home.length());
		return dir;
	    }
	    case STTY:
	    {
		String[] data = execSystemProperty(SPACE, "stty", "-a").split(";");
		String rc = data[data.length - 1];
		while (rc.startsWith(" "))  rc = rc.substring(1);
		while (rc  .endsWith(" "))  rc = rc.substring(0, rc.length() - 1);
		while (rc.contains("  "))   rc = rc.replace("  ", " ");
		return rc;
	    }
	    case TIME:  return execSystemProperty(BREAK, "date");
	    default:
		assert false : "No such property!";
		return null;
	}
    }
    
    public static int exec(final String... cmds)
    {
	try
	{
	    final ProcessBuilder procBuilder = new ProcessBuilder(cmds);
	    
	    procBuilder.inheritIO();
	    procBuilder.directory(new File(dir));
	    
	    final Map<String, String> penv = procBuilder.environment();
	    final Set<String> vars = env.keySet();
	    for (final String var : vars)
		env.put(var, env.get(var));
	    
	    final Process process = procBuilder.start();
	    
	    process.waitFor();
	    return process.exitValue();
	}
	catch (final Throwable err)
	{
	    return 254;
	}
    }
    
    public static String execSystemProperty(final int linerule, final String... cmds)
    {
	try
	{
	    byte[] buf = new byte[64];
	    int ptr = 0;
	    
	    final ProcessBuilder procBuilder = new ProcessBuilder(cmds);
	    procBuilder.redirectInput(ProcessBuilder.Redirect.from(new File(stdout)));
	    final Process process = procBuilder.start();
	    final InputStream stream = process.getInputStream();
	    
	    for (int d; (d = stream.read()) != -1; )
	    {
		if (d == '\n')
		    if (linerule == BREAK)
			break;
		    else if (linerule == IGNORE)
			continue;
		    else if (linerule == SPACE)
			d = ' ';
		
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
		return null;
	    
	    return new String(buf, 0, ptr, "UTF-8");
	}
	catch (final Throwable err)
	{
	    System.err.println(err);
	    return null;
	}
    }
    
}

