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
