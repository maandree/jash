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

import java.util.*;


/**
 * Default command parser
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Parser implements ParserInterface
{
    //Has default constructor
    
    
    
    /**
     * Command pointer
     */
    private int i = 0;
    
    
    
    /**
     * {@inheritDoc}
     */
    public Execute parse(final String command)
    {
	synchronized (this)
	{
	    this.i = 0;
	    return parse(command, 0);
	}
    }
    
    
    /**
     * Implementation for {@link #parse(String)}
     * 
     * @param   command   See {@link #parse(String)}
     * @param   quoteEnd  End of current quote, NUL of none
     * @return            See {@link #parse(String)}
     */
    private Execute parse(final String command, final char quoteEnd)
    {
	String cmd = command + ' ';
	StringBuilder buf = new StringBuilder();
	final ArrayList<Argument> arguments = new ArrayList<Argument>();
	final ArrayList<Redirect> redirects = new ArrayList<Redirect>();
	
	// $()
	// $
	// ${}
	// §()
	// §
	// §{}
	// @()
	// @
	// @{}
	
	// ; blocking cat
	// & nonblocking (cat)
	// # end!
	// | nonblocking pipe
	// &| nonblocking pipe
	// & | nonblocking pipe
	// ;| blocking pipe
	// ; | blocking pipe
	// && on success, blocking
	// || on failure, blocking
	
	int wordtype = 0, _wordtype = 0;
	// arg = 0
	// <   =  1,  <>  =  2,  >  =  3,  >>  =  4,  2>  =  5,  2>>  =  6
	// <&  =  7,  <>& =  8,  >& =  9,  >>& = 10,  2>& = 11,  2>>& = 12
	// >() = 13,  <() = 14
	
	int quote = 0;
	// norm = 0
	// ""   = 1   ''    = 2
	// ()   = 3   "()"  = 4
	// {}   = 5   "{}"  = 6
	
	char last = ' ';
	boolean esc = false;
	boolean wordend = false;
	for (int n = cmd.length(); i < n; i++)
	{
	    char c = cmd.charAt(i);
	    if (c == '\0')
		continue; //sic!
	    if ((c == quoteEnd) && (quote == 0))
		break;
	    
	    if ((quote == 0) && (c == '\t'))
		c = ' ';
	    
	    if (quote == 1)
		if ((c == '\\') && !esc)
		{   esc = true;
		    last = c;
		    continue;
		}
		else if (esc)
		    buf.append(c);
		else if (c == '"')  quote = 0;
		else if (c == '`')  ; //FIXME
		else
		    buf.append(c);
	    else if (quote == 2)
		if (c == '\'')  quote = 0;
		else            buf.append(c);
	    else if ((c != ' ') || (last != ' '))
		if ((c == '\\') && !esc)
		{   esc = true;
		    last = c;
		    continue;
		}
		else if (esc)
		    buf.append(c);
		else if ((c == '#') && (last == ' '))
		    break;
		else if (c == ' ')   wordend = true;
		else if (c == '"')   quote = 1;
		else if (c == '\'')  quote = 2;
		else if (c == '`')   ; //FIXME
		else if ((c == '&') && (1 <= wordtype) && (wordtype <= 6) && (last == ' '))
		{
		    wordtype += 6;
		    buf.append(c);
		}
		else if (c == '<')
		{
		    wordend = true;
		    _wordtype = 1;
		    if (cmd.charAt(i + 1) == '>')
		    {   _wordtype = 2;
			i++;
		    }
		    else if (cmd.charAt(i + 1) == '(')
		    {   wordend = false;
			_wordtype = 14;
			i++;
			//FIXME what about buf
			//FIXME parse(command, ')');
			//FIXME if command.charAt(i) ≠ −1, and ')' is missing
		    }
		}
		else if (c == '>')
		{
		    wordend = true;
		    _wordtype = 3;
		    if (cmd.charAt(i + 1) == '>')
		    {   _wordtype = 4;
			i++;
		    }
		    else if (cmd.charAt(i + 1) == '<')
		    {   _wordtype = 2;
			i++;
		    }
		    else if (cmd.charAt(i + 1) == '(')
		    {   wordend = false;
			_wordtype = 13;
			i++;
			//FIXME what about buf
			//FIXME parse(command, ')');
			//FIXME if command.charAt(i) ≠ −1, and ')' is missing
		    }
		}
		else if ((c == '2') && (last == ' '))
		    if ((c = cmd.charAt(++i)) == '>')
		    {
			wordend = true;
			_wordtype = 5;
			if (cmd.charAt(i + 1) == '>')
			{   _wordtype = 6;
			    i++;
			}
		    }
		    else
		    {   buf.append('2');
			i--;
		    }
		else if ((c == '1') && (last == ' '))
		    if ((c = cmd.charAt(++i)) == '>')
		    {
			wordend = true;
			_wordtype = 3;
			if (cmd.charAt(i + 1) == '>')
			{   _wordtype = 4;
			    i++;
			}
		    }
		    else
		    {   buf.append('1');
			i--;
		    }
		else
		    buf.append(c);
	    
	    last = c;
	    esc = false;
	    
	    if (wordend)
	    {
		wordend = false;
		int red = buf.toString().equals("&0") ? Redirect.STDIN  : 
		          buf.toString().equals("&1") ? Redirect.STDOUT : 
		          buf.toString().equals("&2") ? Redirect.STDERR : 
		          Redirect.FILE;
		if ((red == Redirect.FILE) && (7 <= wordtype) && (wordtype <= 12))
		    wordtype -= 6;
		switch (wordtype)
		{
		    case 0:   arguments.add(new LiteralArgument(buf.toString(), LiteralArgument.EXACT));              break; // arg
		    case 1:   redirects.add(new Redirect(Redirect.STDIN, Redirect.FILE, buf.toString(), false));      break; // <
		    case 2:   redirects.add(new Redirect(Redirect.STDIN_OUT, Redirect.FILE, buf.toString(), false));  break; // <>
		    case 3:   redirects.add(new Redirect(Redirect.STDOUT, Redirect.FILE, buf.toString(), false));     break; // >
		    case 4:   redirects.add(new Redirect(Redirect.STDOUT, Redirect.FILE, buf.toString(), true));      break; // >>
		    case 5:   redirects.add(new Redirect(Redirect.STDERR, Redirect.FILE, buf.toString(), false));     break; // 2>
		    case 6:   redirects.add(new Redirect(Redirect.STDERR, Redirect.FILE, buf.toString(), true));      break; // 2>>
		    case 7:   redirects.add(new Redirect(Redirect.STDIN, red, null, false));                          break; // <&
		    case 8:   redirects.add(new Redirect(Redirect.STDIN_OUT, red, null, false));                      break; // <>&
		    case 9:   redirects.add(new Redirect(Redirect.STDOUT, red, null, false));                         break; // >&
		    case 10:  redirects.add(new Redirect(Redirect.STDOUT, red, null, true));                          break; // >>&
		    case 11:  redirects.add(new Redirect(Redirect.STDERR, red, null, false));                         break; // 2>&
		    case 12:  redirects.add(new Redirect(Redirect.STDERR, red, null, true));                          break; // 2>>&
		}
		buf = new StringBuilder();
		wordtype = _wordtype;
		_wordtype = 0;
		last = ' ';
		arguments.add(null);
	    }
	}
	
	final ArrayList<Argument> $args = new ArrayList<Argument>();
	final ArrayList<Argument> _args = new ArrayList<Argument>();
	arguments.add(null);
	
	for (final Argument argument : arguments)
	    if (argument == null)
	    {
		if (_args.size() == 1)
		    $args.add(_args.get(0));
		else if (_args.size() > 1)
		{
		    final Argument[] composite = new Argument[_args.size()];
		    _args.toArray(composite);
		    $args.add(new CompositeArgument(composite));
		}
		_args.clear();
	    }
	    else
		_args.add(argument);
	
	final Argument[] args = new Argument[$args    .size()];  $args    .toArray(args);
	final Redirect[] reds = new Redirect[redirects.size()];  redirects.toArray(reds);
	return new Execute(args, reds, true, null, 0);
    }
    
}

