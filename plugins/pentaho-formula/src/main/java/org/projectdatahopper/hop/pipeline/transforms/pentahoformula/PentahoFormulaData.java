/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula;

import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;
import org.projectdatahopper.hop.formula.EvaluationException;
import org.projectdatahopper.hop.formula.Formula;
import org.projectdatahopper.hop.formula.parser.ParseException;

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
    Formula result = new Formula(formulaText);
    result.initialize(context);
    return result;
  }
}
