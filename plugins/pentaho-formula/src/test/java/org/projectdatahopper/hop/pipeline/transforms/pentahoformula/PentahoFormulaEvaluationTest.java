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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.net.URL;
import java.net.URLClassLoader;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaBoolean;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.LibFormulaErrorValue;

class PentahoFormulaEvaluationTest {

  @BeforeEach
  void boot() {
    LibFormulaRuntime.ensureBooted();
  }

  @Test
  void arithmeticAndFieldRefs() throws Exception {
    Object result = evaluate(qtyPrice(), new Object[] {2L, 4.0d}, "[qty]*[price]");
    assertEquals(8.0d, ((Number) result).doubleValue(), 0.0001);
  }

  @Test
  void sampleBasicFormulas() throws Exception {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaInteger("one"));
    rowMeta.addValueMeta(new ValueMetaInteger("three"));
    rowMeta.addValueMeta(new ValueMetaInteger("ten"));
    Object[] row = new Object[] {1L, 3L, 10L};
    assertEquals(8.0d, ((Number) evaluate(rowMeta, row, "[one]+([ten]-[three])")).doubleValue(), 0.0001);
    assertEquals(30.0d, ((Number) evaluate(rowMeta, row, "[one]*[three]*[ten]")).doubleValue(), 0.0001);
  }

  @Test
  void parsesOperatorsWhenThreadContextClassLoaderCannotSeeLibformula() throws Exception {
    ClassLoader previous = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], null));
      LibFormulaRuntime.ensureBooted();
      RowMeta rowMeta = new RowMeta();
      rowMeta.addValueMeta(new ValueMetaInteger("one"));
      rowMeta.addValueMeta(new ValueMetaInteger("three"));
      rowMeta.addValueMeta(new ValueMetaInteger("ten"));
      Object result = evaluate(rowMeta, new Object[] {1L, 3L, 10L}, "[one]+([ten]-[three])");
      assertEquals(8.0d, ((Number) result).doubleValue(), 0.0001);
    } finally {
      Thread.currentThread().setContextClassLoader(previous);
    }
  }

  @Test
  void openFormulaIfUsesSemicolon() throws Exception {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaBoolean("flag"));
    Object result = evaluate(rowMeta, new Object[] {Boolean.TRUE}, "IF([flag];\"Y\";\"N\")");
    assertEquals("Y", result);
  }

  @Test
  void missingFieldIsLibformulaError() throws Exception {
    Object result = evaluate(qtyPrice(), new Object[] {1L, 1.0d}, "[missing]");
    assertInstanceOf(LibFormulaErrorValue.class, result);
  }

  private static RowMeta qtyPrice() {
    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta(new ValueMetaInteger("qty"));
    rowMeta.addValueMeta(new ValueMetaNumber("price"));
    return rowMeta;
  }

  private static Object evaluate(RowMeta rowMeta, Object[] row, String formulaText) throws Exception {
    RowFormulaContext context = new RowFormulaContext(rowMeta);
    context.setRowData(row);
    Formula formula = new Formula(formulaText);
    formula.initialize(context);
    return formula.evaluate();
  }
}
