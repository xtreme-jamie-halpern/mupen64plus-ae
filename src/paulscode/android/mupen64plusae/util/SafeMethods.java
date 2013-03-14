/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2013 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Mupen64PlusAE. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: various
 */
package paulscode.android.mupen64plusae.util;

import android.app.Activity;
import android.os.Handler;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * A boilerplate class for safely doing things, with simple exception handling.
 */
public class SafeMethods
{
    /**
     * Safely converts a string into a boolean.
     * 
     * @param val String containing the boolean to convert.
     * @param fail Value to use if unable to convert val to a boolean.
     * @return The converted boolean, or the specified value if unsuccessful.
     */
    public static boolean toBoolean( String val, boolean fail )
    {
        if( TextUtils.isEmpty( val ) )
            return fail; // Not a boolean
        
        try
        {
            // Convert to boolean
            return Boolean.parseBoolean( val );
        }
        catch( NumberFormatException nfe )
        {
        }
        // Conversion failed
        return fail;
    }

    /**
     * Safely converts a string into an integer.
     * 
     * @param val String containing the number to convert.
     * @param fail Value to use if unable to convert val to an integer.
     * @return The converted integer, or the specified value if unsuccessful.
     */
    public static int toInt( String val, int fail )
    {
        if( TextUtils.isEmpty( val ) )
            return fail; // Not a number
        
        try
        {
            // Convert to integer
            return Integer.parseInt( val );
        }
        catch( NumberFormatException nfe )
        {
        }
        // Conversion failed
        return fail;
    }

    /**
     * Safely converts a string into a float.
     * 
     * @param val String containing the number to convert.
     * @param fail Value to use if unable to convert val to a float.
     * @return The converted float, or the specified value if unsuccessful.
     */
    public static float toFloat( String val, float fail )
    {
        if( TextUtils.isEmpty( val ) )
            return fail; // Not a number
        
        try
        {
            // Convert to float
            return Float.parseFloat( val );
        }
        catch( NumberFormatException nfe )
        {
        }
        // Conversion failed
        return fail;
    }
    
    /**
     * Safely sleep.
     * 
     * @param milliseconds The sleep duration.
     */
    public static void sleep( int milliseconds )
    {
        try
        {
            Thread.sleep( milliseconds );
        }
        catch( InterruptedException e )
        {
        }
    }
    
    /**
     * Safely wait for a thread to die.
     * 
     * @param thread Thread to join.
     * @param milliseconds Time to wait, in milliseconds (0 to wait indefinitely).
     */
    public static void join( Thread thread, int milliseconds )
    {
        if( thread == null || milliseconds < 0 )
            return;

        try
        {
            thread.join( milliseconds );
        }
        catch( InterruptedException e )
        {
        }
    }
    
    /**
     * Safely (?) exit the app.
     * 
     * @param message The message to be toasted to the user.
     * @param activity The activity to run the toast from.
     * @param milliseconds Time delay before exiting (so toast can be seen).
     */
    public static void exit( String message, Activity activity, int milliseconds )
    {
        Notifier.showToast( activity, message );
        final Handler handler = new Handler();
        handler.postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                System.exit( 0 );
            }
        }, milliseconds );
    }
    
    /**
     * Safely executes a command and its arguments in a separate native process
     * 
     * @param cmd Array containing the command and its arguments.
     * @param wait Whether or not to wait for the command to finish executing.
     * @return Array containing the output (if any), or null if wait was false.
     */
    public static String[] exec( String[] cmd, boolean wait )
    {
        try
        {
            Process process = Runtime.getRuntime().exec( cmd );
            if( wait )
            {
                process.waitFor();
                LinkedList<String> output = new LinkedList<String>();
                BufferedReader buffer = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
                String line;
                while( ( line = buffer.readLine() ) != null )
                {
                    output.add( line );
                }
                if( output.size() > 0 )
                    return output.toArray( new String[ output.size() ] );
            }
        }
        catch( IOException ioe )
        {}
        catch( InterruptedException ie )
        {}
        return null;
    }
}
