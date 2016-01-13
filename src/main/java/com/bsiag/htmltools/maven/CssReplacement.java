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
