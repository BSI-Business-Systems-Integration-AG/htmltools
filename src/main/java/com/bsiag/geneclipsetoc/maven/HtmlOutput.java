/**
 *
 */
package com.bsiag.geneclipsetoc.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author jbr
 */
public class HtmlOutput {
  static final String OUTPUT_SUB_FOLDER = "outputSubFolder";
  static final String OUTPUT_ZIP_FILE_NAME = "outputZipFileName";
  static final String CSS_REPLACEMENTS = "cssReplacements";
  static final String PAGES_LIST_FILE = "pagesListFile";

  @Parameter(property = OUTPUT_SUB_FOLDER)
  private String outputSubFolder;

  @Parameter(property = OUTPUT_ZIP_FILE_NAME)
  private String outputZipFileName; //Creates a Zip of the subfolder, only possible if the subfolder is set.

  @Parameter(property = CSS_REPLACEMENTS)
  private List<CssReplacement> cssReplacements;

  @Parameter(property = PAGES_LIST_FILE)
  protected File pagesListFile;

  public String getOutputSubFolder() {
    return outputSubFolder;
  }

  public String getOutputZipFileName() {
    return outputZipFileName;
  }

  public List<CssReplacement> getCssReplacements() {
    return cssReplacements;
  }

  public File getPagesListFile() {
    return pagesListFile;
  }
}
