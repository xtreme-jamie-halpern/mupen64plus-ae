/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2012 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU General Public License for more details. You should have received a copy of the GNU
 * General Public License along with Mupen64PlusAE. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: littleguy77
 */
package paulscode.android.mupen64plusae.input.provider;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.InputDevice;
import android.view.InputDevice.MotionRange;
import android.view.MotionEvent;
import android.view.View;

@TargetApi( 12 )
public class AxisProvider extends AbstractProvider implements View.OnGenericMotionListener
{
    private int[] mInputCodes;
    
    private static final int DEFAULT_NUM_INPUTS = 128;
    
    public static AxisProvider create( View view )
    {
        // Use a factory method for API safety
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1 )
            return new AxisProvider( view );
        else
            return null;
    }
    
    private AxisProvider( View view )
    {
        // By default, provide data from all possible axes
        mInputCodes = new int[DEFAULT_NUM_INPUTS];
        for( int i = 0; i < mInputCodes.length; i++ )
            mInputCodes[i] = -( i + 1 );
        
        // Connect the input source
        view.setOnGenericMotionListener( this );
        
        // Request focus for proper listening
        view.requestFocus();
    }
    
    public void setInputCodeFilter( int[] inputCodeFilter )
    {
        mInputCodes = inputCodeFilter.clone();
    }
    
    @Override
    public boolean onGenericMotion( View v, MotionEvent event )
    {
        InputDevice device = event.getDevice();
        
        // Read all the requested axes
        float[] strengths = new float[mInputCodes.length];
        for( int i = 0; i < mInputCodes.length; i++ )
        {
            int inputCode = mInputCodes[i];
            
            // Compute the axis code from the input code
            int axisCode = inputToAxisCode( inputCode );
            
            // Get the analog value using the native Android API
            float strength = event.getAxisValue( axisCode );
            MotionRange motionRange = device.getMotionRange( axisCode );
            if( motionRange != null )
                strength = 2f * ( strength - motionRange.getMin() ) / motionRange.getRange() - 1f;
            
            // If the strength points in the correct direction, record it
            boolean direction1 = inputToAxisDirection( inputCode );
            boolean direction2 = strength > 0;
            if( direction1 == direction2 )
                strengths[i] = Math.abs( strength );
            else
                strengths[i] = 0;
        }
        
        // Notify listeners of input data
        notifyListeners( mInputCodes, strengths );
        
        return true;
    }
}