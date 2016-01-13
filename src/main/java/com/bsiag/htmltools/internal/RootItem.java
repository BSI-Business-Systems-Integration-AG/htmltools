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
 package com.bsiag.htmltools.internal;

public class RootItem {

  private final String title;
  private final String fileName;

  public RootItem(String title, String fileName) {
    this.title = title;
    this.fileName = fileName;
  }

  public String getTitle() {
    return this.title;
  }

  public String getFileName() {
    return this.fileName;
  }
}
