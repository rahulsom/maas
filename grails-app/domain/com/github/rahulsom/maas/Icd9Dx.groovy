package com.github.rahulsom.maas

/**
 * Represents an ICD-9 diagnosis
 */
class Icd9Dx {

  static searchable = true

  String id
  String longName
  String shortName

  static constraints = {
  }
}
