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
import se.kth.maandree.jash.Properties.LineRule;
import se.kth.maandree.jash.Properties.Property;

import java.util.*;
import java.io.*;


/**
 * This is the main class of the program
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Jash
{
    /**
     * The is the main entry point of the program
     * 
     * @param  args  Startup arguments
     */
    public static void main(final String... args)
    {
	System.out.print("\033[?1049h");
	System.out.print("\033%G");
	System.out.flush();
	final String stty = Properties.getProperty(Property.STTY);
	Properties.execSystemProperty(LineRule.BREAK, "stty -icanon -echo -isig -ixon -ixoff".split(" "));
	try
	{
	    Properties.dir = System.getProperty("user.dir");
	    final String user = Properties.getProperty(Property.USER);
	    final String host = Properties.getProperty(Property.HOST);
	    Properties.env.put("SHELL", "jash");
	    Properties.env.put("USER", user);
	    Properties.env.put("HOST", host);
	    
	    final LineReaderInterface lineReader = new LineReader();
	    
	    for (;;)
	    {
		if (Properties.env.get("TERM").equals("xterm"))
		    System.out.print("\033[94m" + user + "\033[39m@" + 
				     "\033[34m" + host + "\033[39m." +  
				     "\033[36m" + Properties.getProperty(Property.TTY) + "\033[39m: " + 
				     "\033[35m" + Properties.getProperty(Property.PDIR) + "\033[39m " + 
				     "\033[90m(" + Properties.getProperty(Property.TIME) + ")\033[39m\n" +
				     (Properties.getProperty(Property.UID).equals("0")
				        ? "\033[91m# \033[39m"
				        : "\033[1;34m! \033[21;39m"));
		else
		    System.out.print("\033[1;34m" + user + "\033[21;39m@" + 
				     "\033[34m" + host + "\033[39m." +  
				     "\033[36m" + Properties.getProperty(Property.TTY) + "\033[39m: " + 
				     "\033[35m" + Properties.getProperty(Property.PDIR) + "\033[39m " + 
				     "\033[1;30m(" + Properties.getProperty(Property.TIME) + ")\033[21;39m\n" +
				     (Properties.getProperty(Property.UID).equals("0")
				        ? "\033[1;31m# \033[21;39m"
				        : "\033[1;34m! \033[21;39m"));
		 
		try
		{
		    final int width = Integer.parseInt(Properties.getProperty(Property.COLS));
		    
		    final String command = lineReader.read(2, width);
		    if (command == null)
			return;
		    if (command.startsWith("?"))
			System.out.println(Properties.env.get(command.substring(1)));
		    else
		    {
			Properties.execSystemProperty(LineRule.BREAK, "stty icanon echo isig".split(" "));
			exec(command);
			Properties.execSystemProperty(LineRule.BREAK, "stty -icanon -echo -isig -ixon -ixoff".split(" "));
		    }
		}
		catch (final IOException err)
		{
		    throw new IOError(err);
		}
	    }
	}
	finally
	{
	    Properties.execSystemProperty(LineRule.BREAK, ("stty " + stty).split(" "));
	    System.out.print("\033[?1049l");
	    System.out.flush();
	}
    }
    
    @requires("java-runtime>=7")
    public static int exec(final String... cmds)
    {
	try
	{
	    final ProcessBuilder procBuilder = new ProcessBuilder(cmds);
	    
	    procBuilder.inheritIO();
	    procBuilder.directory(new File(Properties.dir));
	    
	    final Map<String, String> penv = procBuilder.environment();
	    final Set<String> vars = Properties.env.keySet();
	    for (final String var : vars)
		penv.put(var, Properties.env.get(var));
	    
	    try
	    {
		penv.put("SHLVL", Integer.toString(Integer.parseInt(Properties.env.get("SHLVL")) + 1));
	    }
	    catch (final Throwable err)
	    {
		penv.put("SHLVL", "1");
	    }
	    
	    final Process process = procBuilder.start();
	    
	    process.waitFor();
	    return process.exitValue();
	}
	catch (final Throwable err)
	{
	    return 254;
	}
    }
    
}

