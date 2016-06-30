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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.io.Files;

public class ZipUtility {

  public static void zipFolder(File srcFolder, File destZipFile) {
    ZipOutputStream zip = null;
    FileOutputStream fileWriter = null;

    try {
      fileWriter = new FileOutputStream(destZipFile);
      zip = new ZipOutputStream(fileWriter);

      addFolderToZip("", srcFolder.getCanonicalPath(), zip);
      zip.flush();
      zip.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void addFileToZip(String path, String srcFile, ZipOutputStream zip) {

    File folder = new File(srcFile);
    if (folder.isDirectory()) {
      addFolderToZip(path, srcFile, zip);
    }
    else {
      byte[] buf = new byte[1024];
      int len;
      try (FileInputStream in = new FileInputStream(srcFile)) {
        zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
        while ((len = in.read(buf)) > 0) {
          zip.write(buf, 0, len);
        }
        in.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
    File folder = new File(srcFolder);

    for (String fileName : folder.list()) {
      if (path.equals("")) {
        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
      }
      else {
        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
      }
    }
  }

  public static void unzip(InputStream zipInputStream, File outputFolder) {
    byte[] buffer = new byte[1024];

    try {
      //create output directory is not exists
      File folder = outputFolder;
      if (!folder.exists()) {
        folder.mkdir();
      }

      //get the zip file content
      ZipInputStream zis = new ZipInputStream(zipInputStream);
      //get the zipped file list entry
      ZipEntry ze = zis.getNextEntry();

      while (ze != null) {
        String fileName = ze.getName();
        File newFile = new File(outputFolder + File.separator + fileName);

        if (ze.isDirectory()) {
          newFile.mkdirs();
        }
        else {
          //create all non exists folders
          //else you will hit FileNotFoundException for compressed folder
          Files.createParentDirs(newFile);

          FileOutputStream fos = new FileOutputStream(newFile);

          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }

          fos.close();
        }
        ze = zis.getNextEntry();
      }

      zis.closeEntry();
      zis.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
