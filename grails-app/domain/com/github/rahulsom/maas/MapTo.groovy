package com.github.rahulsom.maas

import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder

class MapTo implements Serializable {

  String loinc
  String mapTo
  String comment

  int hashCode() {
    def builder = new HashCodeBuilder()
    builder.append loinc
    builder.append mapTo
    builder.toHashCode()
  }

  boolean equals(other) {
    if (other == null) return false
    def builder = new EqualsBuilder()
    builder.append loinc, other.loinc
    builder.append mapTo, other.mapTo
    builder.isEquals()
  }

  static mapping = {
    id composite: ["loinc", "mapTo"]
    version false
  }

  static constraints = {
    loinc maxSize: 10
    mapTo maxSize: 10
    comment nullable: true, maxSize: 65535
  }
}
