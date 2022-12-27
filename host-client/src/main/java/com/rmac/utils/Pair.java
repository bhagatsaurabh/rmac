package com.rmac.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pair<X, Y> {

  private X first;
  private Y second;
  public Pair(X first, Y second) {
    this.first = first;
    this.second = second;
  }
}
