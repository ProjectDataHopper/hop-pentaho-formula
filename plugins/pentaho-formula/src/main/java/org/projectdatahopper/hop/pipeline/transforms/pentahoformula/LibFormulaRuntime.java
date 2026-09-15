/*
 * Copyright (C) 2026 Project Data Hopper
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.formula.LibFormulaBoot;

/**
 * libformula loads operators and functions through {@link ObjectUtilities}, which defaults to the
 * thread context classloader. Hop's TCCL is the application classloader and cannot see jars in
 * this plugin's {@code lib/}, so {@code FormulaParser} returns null infix operators and {@code
 * Term.add} NPEs on {@code +}/{@code -}/{@code *}.
 *
 * <p>Same fix as hop-pentaho-reporting: pin ObjectUtilities to this plugin's classloader.
 */
public final class LibFormulaRuntime {
  private static volatile boolean booted;

  private LibFormulaRuntime() {}

  public static void ensureBooted() {
    if (booted && LibFormulaBoot.getInstance().isBootDone()) {
      return;
    }
    synchronized (LibFormulaRuntime.class) {
      ClassLoader pluginLoader = LibFormulaRuntime.class.getClassLoader();
      ObjectUtilities.setClassLoader(pluginLoader);
      ObjectUtilities.setClassLoaderSource(ObjectUtilities.CLASS_CONTEXT);
      LibFormulaBoot.getInstance().start();
      booted = true;
    }
  }
}
