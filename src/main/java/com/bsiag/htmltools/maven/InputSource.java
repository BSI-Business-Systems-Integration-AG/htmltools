/*******************************************************************************
 * Copyright (c) 2015 Jeremie Bresson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 ******************************************************************************/
package com.bsiag.htmltools.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author jbr
 */
public class InputSource {
  static final String INPUT_SOURCE = "inputSource";

  static final String INPUT_FOLDER = "inputFolder";
  static final String OUTPUT_SUB_FOLDER = "outputSubFolder";
  static final String PDF_OUTPUT = "pdfOutput";
  static final String HTML_OUTPUT = "htmlOutput";

  @Parameter(property = INPUT_FOLDER, required = true)
  private File inputFolder;

  @Parameter(property = "pagesListFile")
  protected File pagesListFile;

  @Parameter(property = OUTPUT_SUB_FOLDER)
  private String outputSubFolder;

  @Parameter(property = PDF_OUTPUT)
  private PdfOutput pdfOutput;

  @Parameter(property = HTML_OUTPUT)
  private HtmlOutput htmlOutput;

  public File getInputFolder() {
    return inputFolder;
  }

  public File getPagesListFile() {
    return pagesListFile;
  }

  public String getOutputSubFolder() {
    return outputSubFolder;
  }

  public PdfOutput getPdfOutput() {
    return pdfOutput;
  }

  public HtmlOutput getHtmlOutput() {
    return htmlOutput;
  }
}
