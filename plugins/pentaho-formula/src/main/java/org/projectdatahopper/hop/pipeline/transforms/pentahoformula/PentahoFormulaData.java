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

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;
import org.pentaho.reporting.libraries.formula.EvaluationException;
import org.pentaho.reporting.libraries.formula.Formula;
import org.pentaho.reporting.libraries.formula.parser.ParseException;

@SuppressWarnings("java:S1104")
public class PentahoFormulaData extends BaseTransformData implements ITransformData {
  public static final int RETURN_TYPE_STRING = 0;
  public static final int RETURN_TYPE_NUMBER = 1;
  public static final int RETURN_TYPE_INTEGER = 2;
  public static final int RETURN_TYPE_LONG = 3;
  public static final int RETURN_TYPE_DATE = 4;
  public static final int RETURN_TYPE_BIGDECIMAL = 5;
  public static final int RETURN_TYPE_BYTE_ARRAY = 6;
  public static final int RETURN_TYPE_BOOLEAN = 7;
  public static final int RETURN_TYPE_TIMESTAMP = 9;

  public RowFormulaContext context;
  public Formula[] formulas;
  public IRowMeta outputRowMeta;
  public int[] returnType;
  public int[] replaceIndex;

  public Formula createFormula(String formulaText) throws EvaluationException, ParseException {
    LibFormulaRuntime.ensureBooted();
    Formula result = new Formula(formulaText);
    result.initialize(context);
    return result;
  }
}
