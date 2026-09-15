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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class OpenFormulaSeparatorRestorerTest {

  @Test
  void restoresFunctionArgumentCommas() {
    assertEquals("IF([flag];\"Y\";\"N\")", OpenFormulaSeparatorRestorer.restoreSemicolons("IF([flag],\"Y\",\"N\")"));
  }

  @Test
  void leavesCommasInsideQuotes() {
    assertEquals(
        "IF([flag];\"a,b\";\"c\")", OpenFormulaSeparatorRestorer.restoreSemicolons("IF([flag],\"a,b\",\"c\")"));
  }

  @Test
  void leavesCommasInsideFieldReferences() {
    assertEquals("[odd,name]", OpenFormulaSeparatorRestorer.restoreSemicolons("[odd,name]"));
  }

  @Test
  void nullSafe() {
    assertNull(OpenFormulaSeparatorRestorer.restoreSemicolons(null));
  }
}
