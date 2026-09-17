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

package org.projectdatahopper.hop.formula.function.userdefined;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.projectdatahopper.hop.formula.common.FormulaTestBase;

import java.math.BigDecimal;

public class ArrayLeftFunctionTest extends FormulaTestBase {
  @Test
  public void testDefault() throws Exception {
    runDefaultTest();
  }

  @Override
  public Object[][] createDataTest() {
    return new Object[][]
      {
        { "NORMALIZEARRAY(ARRAYLEFT([.B3:.B7]; 2))", new Object[]
          { "7", new BigDecimal( 2 ), } },
        { "NORMALIZEARRAY(ARRAYLEFT([.B3:.B7]; 0))", new Object[] {} },
        { "NORMALIZEARRAY(ARRAYLEFT([.B3:.B7]; 10))", new Object[]
          { "7", new BigDecimal( 2 ), new BigDecimal( 3 ), Boolean.TRUE, "Hello" } },
      };
  }

}
