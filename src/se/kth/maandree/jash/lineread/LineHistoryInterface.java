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
 * Line input history interface
 * 
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
 */
public interface LineHistoryInterface
{
    /**
     * Up arrow, or page up key, pressed
     * 
     * @param   page     Whether page up was pressed
     * @param   current  The current line
     * @return           New data, <code>null</code> if this current should be kept
     */
    public int[] up(final boolean page, final int[] current);
    
    /**
     * Down arrow, or page down key, pressed
     * 
     * @param   page     Whether page down was pressed
     * @param   current  The current line
     * @return           New data, <code>null</code> if this current should be kept
     */
    public int[] down(final boolean page, final int[] current);
    
    /**
     * Enter key pressed
     * 
     * @param  current  The current line
     */
    public void enter(final int[] current);
    
    /**
     * Finds the last line beginning in a specific way
     * 
     * @param   beginning  The beginning of the line to find, <code>null</code> for the latest line
     * @return             The last match line, <code>null</code> if none found
     */
    public int[] search(final int[] beginning);
    
}

