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

public class DocEntry {
  private String name;
  private String htmlSubPath;
  private String pdfSubPath;
  private String zipSubPath;
  private File folder;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHtmlSubPath() {
    return htmlSubPath;
  }

  public void setHtmlSubPath(String htmlSubPath) {
    this.htmlSubPath = htmlSubPath;
  }

  public String getPdfSubPath() {
    return pdfSubPath;
  }

  public void setPdfSubPath(String pdfSubPath) {
    this.pdfSubPath = pdfSubPath;
  }

  public String getZipSubPath() {
    return zipSubPath;
  }

  public void setZipSubPath(String zipSubPath) {
    this.zipSubPath = zipSubPath;
  }

  public File getFolder() {
    return folder;
  }

  public void setFolder(File folder) {
    this.folder = folder;
  }
}
