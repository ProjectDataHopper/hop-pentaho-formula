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

package org.projectdatahopper.hop.formula.function;

import java.io.Serializable;
import org.projectdatahopper.hop.formula.EvaluationException;
import org.projectdatahopper.hop.formula.FormulaContext;
import org.projectdatahopper.hop.formula.typing.DataType;
import org.projectdatahopper.hop.formula.typing.TypedValue;

/**
 * Interface implemented by all formula functions.
 */
public interface Function extends Serializable {
  String getCanonicalName();

  TypedValue evaluate(FormulaContext context, ParameterCallback parameters) throws EvaluationException;

  default DataType getReturnType() {
    return DataType.ANY;
  }
}
