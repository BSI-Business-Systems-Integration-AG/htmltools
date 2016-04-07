/*******************************************************************************
 * Copyright (c) 2016 Jeremie Bresson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 ******************************************************************************/
package com.bsiag.htmltools.internal;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ParamPublishHtmlFiles {

  private File inFolder;
  private List<File> inFiles;
  private File outFolder;
  private Map<String, File> cssReplacement;
  private boolean fixXrefLinks;
  private boolean fixExternalLinks;

  public File getInFolder() {
    return inFolder;
  }

  public void setInFolder(File inFolder) {
    this.inFolder = inFolder;
  }

  public List<File> getInFiles() {
    return inFiles;
  }

  public void setInFiles(List<File> inFiles) {
    this.inFiles = inFiles;
  }

  public File getOutFolder() {
    return outFolder;
  }

  public void setOutFolder(File outFolder) {
    this.outFolder = outFolder;
  }

  public Map<String, File> getCssReplacement() {
    return cssReplacement;
  }

  public void setCssReplacement(Map<String, File> cssReplacement) {
    this.cssReplacement = cssReplacement;
  }

  public boolean isFixXrefLinks() {
    return fixXrefLinks;
  }

  public void setFixXrefLinks(boolean fixXrefLinks) {
    this.fixXrefLinks = fixXrefLinks;
  }

  public boolean isFixExternalLinks() {
    return fixExternalLinks;
  }

  public void setFixExternalLinks(boolean fixExternalLinks) {
    this.fixExternalLinks = fixExternalLinks;
  }
}
