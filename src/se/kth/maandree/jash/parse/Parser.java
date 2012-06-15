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
 * Default command parser
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public class Parser implements ParserInterface
{
    //Has default constructor
    
    
    
    /**
     * {@inheritDoc}
     */
    public Execute parse(final String cmd)
    {
	final StringBuilder buf = new StringBuilder();
	
	// <>
	// >
	// >>
	// <
	// 2>
	// 2>>
	// &0
	// &1
	// &2
	
	// >()
	// <()
	// ``
	// $()
	// $
	// ${}
	// §()
	// §
	// §{}
	// @
	// @{}
	
	// ; blocking cat
	// & nonblocking (cat)
	// # end!
	// | blocking pipe
	// &| nonblocking pipe
	// & | nonblocking pipe
	// && on success, blocking
	// || on failure, blocking
	
	char last = ' ';
	boolean esc = false;
	int quote = 0;
	for (int i = 0, n = cmd.length(); i < n; i++)
	{
	    final char c = cmd.charAt(i);
	    if (c == '\0')
		continue; //sic!
	    
	    if (quote == 1)
		if ((c == '\\') && !esc)
		{   esc = true;
		    last = c;
		    continue;
		}
		else if ((c == '"') && !esc)  quote = 0;
		else                          buf.append(c);
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
		else if (c == ' ')   buf.append('\0');
		else if (c == '"')   quote = 1;
		else if (c == '\'')  quote = 2;
		else
		    buf.append(c);
	    
	    last = c;
	    esc = false;
	}
	
	final String rc = buf.toString();
	return rc.endsWith(" ") ? rc.substring(0, rc.length() - 1) : rc;
    }
 
    
}

