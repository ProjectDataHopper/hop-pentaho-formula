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

package org.projectdatahopper.hop.pipeline.transforms.pentahoformula.imp;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPoint;
import org.apache.hop.core.extension.IExtensionPoint;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.vfs.HopVfs;
import org.apache.hop.core.xml.XmlFormatter;
import org.apache.hop.core.xml.XmlHandler;
import org.apache.hop.imp.HopImportBase;
import org.w3c.dom.Document;

/**
 * After Kettle import writes .hpl files, remap PDI Formula transforms to {@code PentahoFormula}.
 *
 * <p>Hooked on {@code HopImportRewriteMetadata} so both GUI import and {@code hop-import} run it.
 */
@ExtensionPoint(
    id = "PentahoFormulaKettleImport",
    extensionPointId = "HopImportRewriteMetadata",
    description = "Remap imported PDI Formula steps to PentahoFormula (libformula)",
    classLoaderGroup = "pentaho-formula")
public class PentahoFormulaKettleImportXp implements IExtensionPoint<HopImportBase> {

  @Override
  public void callExtensionPoint(ILogChannel log, IVariables variables, HopImportBase hopImport)
      throws HopException {
    if (hopImport == null) {
      return;
    }
    FileObject inputFolder = hopImport.getInputFolder();
    String outputFolderName = hopImport.getOutputFolderName();
    HashMap<String, DOMSource> migrated = hopImport.getMigratedFilesMap();
    if (inputFolder == null || outputFolderName == null || migrated == null || migrated.isEmpty()) {
      return;
    }
    int files = 0;
    int transforms = 0;
    try {
      String inputUri = inputFolder.getName().getURI();
      for (Map.Entry<String, DOMSource> entry : migrated.entrySet()) {
        if (entry.getValue() == null || entry.getKey() == null) {
          continue;
        }
        String filename = entry.getKey();
        if (!filename.toLowerCase().endsWith(".ktr")) {
          continue;
        }
        String targetFilename =
            filename.replace(inputUri, outputFolderName).replaceAll("\\.ktr$", ".hpl");
        FileObject file = HopVfs.getFileObject(targetFilename);
        if (!file.exists() || !file.isFile()) {
          continue;
        }
        Document document = XmlHandler.loadXmlFile(file);
        int remapped = PentahoFormulaImportRewriter.rewrite(document);
        if (remapped == 0) {
          continue;
        }
        writeDocument(file, document);
        files++;
        transforms += remapped;
      }
    } catch (HopException e) {
      throw e;
    } catch (Exception e) {
      throw new HopException("Error remapping imported PDI Formula transforms to PentahoFormula", e);
    }
    if (transforms > 0 && log != null) {
      log.logBasic(
          "Pentaho Formula import: remapped "
              + transforms
              + " Formula transform(s) in "
              + files
              + " pipeline(s) to PentahoFormula");
    }
  }

  private static void writeDocument(FileObject file, Document document) throws Exception {
    Transformer transformer = XmlHandler.createSecureTransformerFactory().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
    transformer.transform(new DOMSource(document), new StreamResult(buffer));
    String xml = XmlFormatter.format(buffer.toString(StandardCharsets.UTF_8));
    try (OutputStream out = HopVfs.getOutputStream(file, false)) {
      out.write(xml.getBytes(StandardCharsets.UTF_8));
    }
  }
}
