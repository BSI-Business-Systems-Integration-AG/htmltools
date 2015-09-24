package com.bsiag.geneclipsetoc.internal;

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
