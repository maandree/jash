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
import javax.lang.model.element.*;
import javax.annotation.processing.*;

import static javax.lang.model.SourceVersion.RELEASE_7;


/**
 * This class is used to process annotation types (@interface)
 *
 * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
*/
@SupportedSourceVersion(RELEASE_7)
@SupportedAnnotationTypes(
    {
        "se.kth.maandree.jash.requires",
        "javax.annotation.processing.SupportedAnnotationTypes",
        "javax.annotation.processing.SupportedSourceVersion"
    })
public class ATProcessor extends AbstractProcessor
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv)
    {
	final ArrayList<String> reqs = new ArrayList<String>();
	
	System.out.print("\033[1;30m");
        checkElements(roundEnv.getRootElements(), reqs);
	System.out.print("\033[21;39m");
	
	if (reqs.isEmpty() == false)
	{
	    int cnames = 0;
	    final String[] names = new String[reqs.size()];
	    final HashSet<String> name = new HashSet<String>();
	    final HashMap<String, String> low = new HashMap<String, String>();
	    final HashMap<String, String> high = new HashMap<String, String>();
	    
	    for (final String req : reqs)
	    {
		String r = req;
		r = r.replace("=<", "<");
		r = r.replace("<=", "<");
		r = r.replace(">=", ">");
		r = r.replace("=>", ">");
		
		String p = r;
		p = p.replace("<", "\0<\0");
		p = p.replace("=", "\0>\0"); //yes, >
		p = p.replace(">", "\0>\0");
		
		final String[] parts = p.split("\0");
		if (name.contains(parts[parts.length == 5 ? 3 : 0]) == false)
		    name.add(names[cnames++] = parts[parts.length == 5 ? 3 : 0]);
		
		String nhigh = null;
		String nlow = null;
		
		if (parts.length == 3)
		{
		    if (parts[1].equals(">"))
			nlow = parts[2];
		    else if (parts[1].equals("<"))
			nhigh = parts[2];
		    else
		    {
			System.out.println("\033[31mUnparsable: " + req + "\033[39m");
			continue;
		    }
		}
		else if (parts.length == 5)
		{
		    if (parts[1].equals(">"))
			nhigh = parts[0];
		    else if (parts[1].equals("<"))
			nlow = parts[0];
		    else
		    {
			System.out.println("\033[31mUnparsable: " + req + "\033[39m");
			continue;
		    }
		    
		    if (parts[3].equals(">"))
			nlow = parts[4];
		    else if (parts[3].equals("<"))
			nhigh = parts[4];
		    else
		    {
			System.out.println("\033[31mUnparsable: " + req + "\033[39m");
			continue;
		    }
		    
		    if ((nlow == null) || (nhigh == null) || isGreater(nlow, nhigh))
		    {
			nlow = nhigh = null;
			System.out.println("\033[31mUnparsable: " + req + "\033[39m");
			continue;
		    }
		}
		else if (parts.length != 1)
		{
		    System.out.println("\033[31mUnparsable: " + req + "\033[39m");
		    continue;
		}
		
		if ((nlow != null) && isLess(nlow, low.get(parts[parts.length == 5 ? 3 : 0])))
		    low.put(parts[parts.length == 5 ? 3 : 0], nlow);
		
		if ((nhigh != null) && isGreater(nhigh, high.get(parts[parts.length == 5 ? 3 : 0])))
		    high.put(parts[parts.length == 5 ? 3 : 0], nhigh);
	    }
	    
	    Arrays.sort(names, 0, cnames);
	    
	    System.out.println("\033[34mDependencies:");
	    
	    for (final String pkg : names)
	    {
		if (pkg == null)
		    break;
		
		final String plow = low.get(pkg);
		final String phigh = high.get(pkg);
		
		if ((plow != null) && (phigh != null))  System.out.println("  " + plow + " <= " + pkg + " <= " + phigh);
		else if (plow != null)                  System.out.println("  " + pkg + " >= " + plow);
		else if (phigh != null)                 System.out.println("  " + pkg + " <= " + phigh);
		else                                    System.out.println("  " + pkg);
	    }
	    
	    System.out.println("\033[39m");
	}
	
        return true;
    }
    
    private boolean isGreater(final String a, final String b)
    {
	if (b == null)
	    return true;
	
	final String[] A, B;
	
	{
	    final String x = a;
	    final char[] cs = new char[x.length() << 1];
	    int ptr = 0;
	    char c = '1';
	    for (int i = 0, n = x.length(); i < n; i++)
	    {
		if ((c != '.') && ((c < '0') || ('9' < c)))
		    cs[ptr++] = '.';
		
		c = x.charAt(i);
		
		if ((c != '.') && ((c < '0') || ('9' < c)))
		    cs[ptr++] = '.';
		cs[ptr++] = c;
	    }
	    A = (new String(cs, 0, ptr)).split(".");
	}
	{
	    final String x = b;
	    final char[] cs = new char[x.length() << 1];
	    int ptr = 0;
	    char c = '1';
	    for (int i = 0, n = x.length(); i < n; i++)
	    {
		if ((c != '.') && ((c < '0') || ('9' < c)))
		    cs[ptr++] = '.';
		
		c = x.charAt(i);
		
		if ((c != '.') && ((c < '0') || ('9' < c)))
		    cs[ptr++] = '.';
	 	cs[ptr++] = c;
	    }
	    B = (new String(cs, 0, ptr)).split(".");
	}
	
	for (int i = 0, n = A.length < B.length ? A.length : B.length; i < n; i++)
        {
	    if (A[i].equals(B[i]))
		continue;
	    
	    //Code by exception, it is not good, but I can't bother the check the names for the wanted methods
	    try
	    {
		return Integer.parseInt(A[i]) > Integer.parseInt(B[i]);
	    }
	    catch (final Throwable err)
	    {
		try
		{
		    Integer.parseInt(A[i]);
		}
		catch (final Throwable ierr)
		{
		    return true;
		}
		try
		{
		    Integer.parseInt(B[i]);
		}
		catch (final Throwable ierr)
		{
		    return false;
		}
		return A[i].compareTo(B[i]) > 0;
	    }
	}
	
	return A.length > B.length;
    }
    
    private boolean isLess(final String a, final String b)
    {
	if (b == null)
	    return true;
	return isGreater(b, a);
    }
    
    private void checkElements(Set<? extends Element> elems, final ArrayList<String> reqout)
    { 
        for (final Element elem : elems)
        {
            final requires reqs = elem.getAnnotation(requires.class);
            if (reqs != null)
            {
		System.out.println(elem + " requires:");
		for (final String req : reqs.value())
		{
		    System.out.println("  " + req);
		    reqout.add(req);
		}
            }
            checkElements(elem.getEnclosedElements(), elem + ".", reqout);
        }
    }

    private void checkElements(List<? extends Element> elems, String parent, final ArrayList<String> reqout)
    {
        for (final Element elem : elems)
        {
            requires reqs = elem.getAnnotation(requires.class);
            if (reqs != null)
            {
		System.out.println(elem + " requires:");
		for (final String req : reqs.value())
		{
		    System.out.println("  " + req);
		    reqout.add(req);
		}
            }
            checkElements(elem.getEnclosedElements(), parent + elem + ".", reqout);
        }
    }
}

