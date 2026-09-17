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

package org.projectdatahopper.hop.formula.function.information;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.projectdatahopper.hop.formula.common.FormulaTestBase;
import org.projectdatahopper.hop.formula.error.FormulaErrorValue;

/**
 * @author Cedric Pronzato
 */
public class IsOddFunctionTest extends FormulaTestBase {
  @Test
  public void testDefault() throws Exception {
    runDefaultTest();
  }

  @Override
  public Object[][] createDataTest() {
    return new Object[][]
      {
        { "ISODD(3)", Boolean.TRUE },
        { "ISODD(5)", Boolean.TRUE },
        { "ISODD(3.1)", Boolean.TRUE },
        { "ISODD(3.5)", Boolean.TRUE },
        { "ISODD(3.9)", Boolean.TRUE },
        { "ISODD(4)", Boolean.FALSE },
        { "ISODD(4.9)", Boolean.FALSE },
        { "ISODD(-3)", Boolean.TRUE },
        { "ISODD(-3.1)", Boolean.TRUE },
        { "ISODD(-3.5)", Boolean.TRUE },
        { "ISODD(-3.9)", Boolean.TRUE },
        { "ISODD(-4)", Boolean.FALSE },
        { "ISODD(NA())", FormulaErrorValue.ERROR_NA_VALUE },
        { "ISODD(0)", Boolean.FALSE },
        { "ISODD(1)", Boolean.TRUE },
        { "ISODD(2)", Boolean.FALSE },
        { "ISODD(2.9)", Boolean.FALSE }, };
  }

}
