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
package se.kth.maandree.jash.lineread;

import java.io.*;


/**
 * Line input reader interface
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public interface LineReaderInterface
{
    /**
     * Reads and returns one line from stdin, a line feed is always echoed to stdout when the line has been entered
     * 
     * @param   x      The position of the cursor on the x axis as calculated, a terminal may be able to provide
     *                 the actual position to stdin if you print <code>\e[?6m</code> to stdout. The only reason for
     *                 an incorrect position is if a program didn't echo a line feed as its last characters.
     * @param   width  The width of the terminal
     * @return         The line entered by the user, <code>null</code> if it was interrupted by a signal
     * 
     * @throws  IOException  On I/O exception, this is fatal
     */
    public String read(final int x, final int width) throws IOException;
    
}

