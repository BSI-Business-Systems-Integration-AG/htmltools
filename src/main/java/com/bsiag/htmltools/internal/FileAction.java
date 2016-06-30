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
import java.io.IOException;

import com.google.common.io.Files;

/**
 * @author jbr
 */
public class FileAction {
  public static enum ActionType {
    COPY, REMOVE
  }

  private ActionType type;
  private final File file;
  private final File from;

  private FileAction(ActionType type, File file, File fromFile) {
    this.type = type;
    this.file = file;
    this.from = fromFile;
  }

  public static FileAction copy(File from, File to) {
    return new FileAction(ActionType.COPY, to, from);
  }

  public static FileAction remove(File file) {
    return new FileAction(ActionType.REMOVE, file, null);
  }

  public boolean isCreateFile() {
    return type == ActionType.COPY;
  }

  public File getParentFile() {
    return file.getParentFile();
  }

  public void doAction() throws IOException {
    switch (type) {
      case COPY:
        if ((file.exists() && file.isFile()) || from.isFile()) {
          Files.createParentDirs(file);
          Files.copy(from, file);
        }
        else if ((file.exists() && file.isDirectory()) || from.isDirectory()) {
          if (file.exists()) {
            PublishUtility.deleteFilesRec(file);
          }
          Files.createParentDirs(file);
          PublishUtility.copyRec(from, file);
        }
        break;
      case REMOVE:
        if (file.exists()) {
          if (file.isFile()) {
            file.delete();
          }
          else {
            PublishUtility.deleteFilesRec(file);
          }
        }
        break;
      default:
        throw new IllegalStateException("unknown type: " + type);
    }
  }

  @Override
  public String toString() {
    switch (type) {
      case COPY:
        return "FileAction [" + type.name() + ": from=" + from + ", to=" + file + "]";
      case REMOVE:
        return "FileAction [" + type.name() + ": file=" + file + "]";
      default:
        throw new IllegalStateException("unknown type: " + type);
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((file == null) ? 0 : file.hashCode());
    result = prime * result + ((from == null) ? 0 : from.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FileAction other = (FileAction) obj;
    if (file == null) {
      if (other.file != null) return false;
    }
    else if (!file.equals(other.file)) return false;
    if (from == null) {
      if (other.from != null) return false;
    }
    else if (!from.equals(other.from)) return false;
    if (type != other.type) return false;
    return true;
  }

  public FileAction createActionWithParentFolder() {
    return new FileAction(type, file.getParentFile(), from == null ? null : from.getParentFile());
  }

  public FileAction createActionWithSubFolder(String folderName) {
    return new FileAction(type, new File(file, folderName), from == null ? null : new File(from, folderName));
  }
}
