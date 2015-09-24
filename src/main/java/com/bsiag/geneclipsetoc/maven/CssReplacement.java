/**
 *
 */
package com.bsiag.geneclipsetoc.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author jbr
 */
public class CssReplacement {
  static final String CSS_REPLACEMENT = "cssReplacement";
  static final String ORIGINAL_FILE_NAME = "originalFileName";
  static final String REPLACEMENT_FILE = "replacementFile";

  @Parameter(property = ORIGINAL_FILE_NAME)
  private String originalFileName;

  @Parameter(property = REPLACEMENT_FILE)
  private File replacementFile;

  public String getOriginalFileName() {
    return originalFileName;
  }

  public File getReplacementFile() {
    return replacementFile;
  }
}
