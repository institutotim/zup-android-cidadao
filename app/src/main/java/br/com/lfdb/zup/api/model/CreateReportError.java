package br.com.lfdb.zup.api.model;

import java.io.Serializable;

public class CreateReportError implements Serializable {
  private String error;
  private String type;

  public CreateReportError() {
  }

  public CreateReportError(String error, String type) {
    this.error = error;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
