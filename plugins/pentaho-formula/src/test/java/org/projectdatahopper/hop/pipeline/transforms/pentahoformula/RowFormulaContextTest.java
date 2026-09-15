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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.apache.hop.core.row.value.ValueMetaString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.reporting.libraries.formula.EvaluationException;

class RowFormulaContextTest {

  @BeforeEach
  void boot() {
    LibFormulaRuntime.ensureBooted();
  }

  @Test
  void resolvesNumericAndStringFields() throws Exception {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaInteger("qty"));
    rowMeta.addValueMeta(new ValueMetaNumber("price"));
    rowMeta.addValueMeta(new ValueMetaString("name"));
    RowFormulaContext context = new RowFormulaContext(rowMeta);
    context.setRowData(new Object[] {3L, 1.5d, "box"});
    assertEquals(3L, context.resolveReference("qty"));
    assertEquals(1.5d, context.resolveReference("price"));
    assertEquals("box", context.resolveReference("name"));
  }

  @Test
  void missingFieldThrows() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaInteger("qty"));
    RowFormulaContext context = new RowFormulaContext(rowMeta);
    context.setRowData(new Object[] {1L});
    assertThrows(EvaluationException.class, () -> context.resolveReference("missing"));
  }
}
