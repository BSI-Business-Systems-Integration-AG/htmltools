/**
 *
 */
package com.bsiag.geneclipsetoc.maven;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author jbr
 */
public class PdfOutput {
  @Parameter(property = "outputSubFolder")
  private String outputSubFolder;

  public String getOutputSubFolder() {
    return outputSubFolder;
  }
}
