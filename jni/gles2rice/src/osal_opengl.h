/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *   Mupen64plus - osal_opengl.h                                           *
 *   Mupen64Plus homepage: http://code.google.com/p/mupen64plus/           *
 *   Copyright (C) 2009 Richard Goedeken                                   *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.          *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

#if !defined(OSAL_OPENGL_H)
#define OSAL_OPENGL_H

#include <SDL_config.h>

#if SDL_VIDEO_OPENGL
#include <SDL_opengl.h>
#define GLSL_VERSION "120"

// TODO: Tentative substitutions (need to examine in more detail)
#define OSAL_GL_ARB_TEXTURE_ENV_ADD         "GL_ARB_texture_env_add"


#elif SDL_VIDEO_OPENGL_ES2
#include <SDL_opengles2.h>
#define GLSL_VERSION "100"

// TODO: Tentative substitutions (need to examine in more detail)
#define OSAL_GL_ARB_TEXTURE_ENV_ADD         "GL_texture_env_add"

// Vertex shader params
#define VS_POSITION                         0
#define VS_COLOR                            1
#define VS_TEXCOORD0                        2
#define VS_TEXCOORD1                        3

// Constant substitutions
//#define GL_ADD_SIGNED_ARB                   GL_ADD_SIGNED
#define GL_CLAMP                            GL_CLAMP_TO_EDGE
//#define GL_COMBINE_RGB_ARB                  GL_COMBINE_RGB
//#define GL_CONSTANT_ARB                     GL_CONSTANT
//#define GL_INTERPOLATE_ARB                  GL_INTERPOLATE
#define GL_MAX_TEXTURE_UNITS_ARB            GL_MAX_TEXTURE_IMAGE_UNITS
#define GL_MIRRORED_REPEAT_ARB              GL_MIRRORED_REPEAT
//#define GL_OPERAND0_RGB_ARB                 GL_OPERAND0_RGB
//#define GL_OPERAND1_RGB_ARB                 GL_OPERAND1_RGB
//#define GL_OPERAND2_RGB_ARB                 GL_OPERAND2_RGB
//#define GL_OPERAND0_RGB_ALPHA_ARB           GL_OPERAND0_ALPHA
//#define GL_OPERAND1_RGB_ALPHA_ARB           GL_OPERAND1_ALPHA
//#define GL_OPERAND2_RGB_ALPHA_ARB           GL_OPERAND2_ALPHA
//#define GL_PREVIOUS_ARB                     GL_PREVIOUS
//#define GL_PRIMARY_COLR_ARB                 GL_PRIMARY_COLOR
//#define GL_SOURCE0_RGB_ARB                  GL_SRC0_RGB
//#define GL_SOURCE1_RGB_ARB                  GL_SRC1_RGB
//#define GL_SOURCE2_RGB_ARB                  GL_SRC2_RGB
//#define GL_SOURCE0_ALPHA_ARB                GL_SRC0_ALPHA
//#define GL_SOURCE1_ALPHA_ARB                GL_SRC1_ALPHA
//#define GL_SOURCE2_ALPHA_ARB                GL_SRC2_ALPHA
//#define GL_SUBTRACT_ARB                     GL_SUBTRACT
#define GL_TEXTURE0_ARB                     GL_TEXTURE0
#define GL_TEXTURE1_ARB                     GL_TEXTURE1
//#define GL_TEXTURE2_ARB                     GL_TEXTURE2
//#define GL_TEXTURE3_ARB                     GL_TEXTURE3
//#define GL_TEXTURE4_ARB                     GL_TEXTURE4
//#define GL_TEXTURE5_ARB                     GL_TEXTURE5
//#define GL_TEXTURE6_ARB                     GL_TEXTURE6
//#define GL_TEXTURE7_ARB                     GL_TEXTURE7

// No-op substitutions (unavailable in GLES2)
#define glLoadIdentity()
#define glMatrixMode(x)
#define glOrtho(a,b,c,d,e,f)
#define glReadBuffer(x)
#define glTexEnvi(x,y,z)
#define glTexEnvfv(x,y,z)
#define glTexCoord2f(u,v)

// Function substitutions
//#define GLdouble                            GLfloat
#define glClearDepth                        glClearDepthf
//#define glColor3f(a,b,c)                    glColor4f(a, b, c, 1.0f)
//#define glColor3fv(a)                       glColor4f(a[0], a[1], a[2], 1.0f)
//#define glColor4fv(a)                       glColor4f(a[0], a[1], a[2], a[3])
#define pglActiveTexture                    glActiveTexture
#define pglActiveTextureARB                 glActiveTexture

#endif // SDL_VIDEO_OPENGL*

#endif // OSAL_OPENGL_H
